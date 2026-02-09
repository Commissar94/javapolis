package javapolis.service;

import jakarta.annotation.PostConstruct;
import javapolis.model.dto.JudgeRequest;
import javapolis.model.dto.JudgeResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.UUID;

@Service
public class JudgeService {

    private static final String BASE_IMAGE_TAG = "javapolis-judge-base-v6-fixed";
    private static boolean baseImageChecked = false;

    @PostConstruct
    public void init() {
        // Запускаем сборку образа в отдельном потоке, чтобы не блокировать старт приложения
        new Thread(() -> {
            try {
                System.out.println("[INFO] Проверка Docker и базового образа при старте...");
                checkDocker();
                ensureBaseImageBuilt();
                System.out.println("[INFO] Базовый образ готов к работе.");
            } catch (Exception e) {
                System.err.println("[ERROR] Не удалось подготовить базовый образ при старте: " + e.getMessage());
                // Не пробрасываем ошибку дальше, чтобы приложение запустилось даже если Docker выключен
            }
        }).start();
    }

    public JudgeResponse judge(JudgeRequest request) {
        String taskId = request.taskId();
        String language = request.language();
        String code = request.code();

        if (language == null || !language.equalsIgnoreCase("java")) {
            return new JudgeResponse("ERROR", "", "Поддерживается только language=java на данном этапе", 2);
        }
        if (code == null || code.isBlank()) {
            return new JudgeResponse("ERROR", "", "Пустой код решения", 2);
        }
        if (taskId == null || taskId.isBlank()) {
            return new JudgeResponse("ERROR", "", "Не указан taskId", 2);
        }

        Path tempDir = null;
        try {
            checkDocker();
            ensureBaseImageBuilt();

            tempDir = Files.createTempDirectory("judge-" + UUID.randomUUID());
            // 1) Скопировать шаблон judge/docker в temp (нам нужен pom.xml там)
            Path templateDir = Paths.get("judge", "docker");
            copyDirectory(templateDir, tempDir);

            // 2) Убедиться, что существует src/test/java
            Path testJavaDir = tempDir.resolve(Paths.get("src", "test", "java"));
            Files.createDirectories(testJavaDir);

            // 3) Записать пользовательский Solution.java
            Path solutionFile = testJavaDir.resolve("Solution.java");
            Files.writeString(solutionFile, code, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 4) Скопировать тест для задачи из ресурсов в SolutionTest.java
            String resourcePath = "judge/tasks/" + taskId + "/SolutionTest.java";
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                return new JudgeResponse("ERROR", "", "Не найден тест для задачи: " + taskId, 3);
            }
            try (InputStream is = resource.getInputStream()) {
                Path solutionTest = testJavaDir.resolve("SolutionTest.java");
                Files.copy(is, solutionTest, StandardCopyOption.REPLACE_EXISTING);
            }

            // 5) Запустить контейнер с монтированием текущей папки
            // На Windows пути для Docker должны быть абсолютными и желательно с нормализованными слэшами
            String absolutePath = tempDir.toAbsolutePath().toString();
            
            // Используем -v для монтирования. Это ГОРАЗДО быстрее чем docker build.
            ProcessBuilder runPb = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "-v", absolutePath + ":/app",
                    BASE_IMAGE_TAG
            );
            
            Process run = runPb.start();
            int runExit = waitAndCollect(run, Duration.ofMinutes(2));
            String runOut = lastStdout;
            String runErr = lastStderr;

            // Очистка вывода Maven
            runOut = filterMavenOutput(runOut);

            String status = runExit == 0 ? "OK" : "FAIL";
            return new JudgeResponse(status, runOut, runErr, runExit);
        } catch (Exception e) {
            return new JudgeResponse("ERROR", "", stackTraceToString(e), 1);
        } finally {
            if (tempDir != null) {
                try { deleteRecursively(tempDir); } catch (IOException ignored) {}
            }
        }
    }

    private void ensureBaseImageBuilt() throws IOException, InterruptedException {
        if (baseImageChecked) return;
        
        // Проверяем наличие образа
        ProcessBuilder checkPb = new ProcessBuilder("docker", "images", "-q", BASE_IMAGE_TAG);
        Process checkProc = checkPb.start();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        streamPump(checkProc.getInputStream(), bos);
        checkProc.waitFor();
        
        if (bos.toString().trim().isEmpty()) {
            System.out.println("[INFO] Базовый образ для judge не найден. Собираем...");
            Path templateDir = Paths.get("judge", "docker");
            ProcessBuilder buildPb = new ProcessBuilder("docker", "build", "-t", BASE_IMAGE_TAG, ".");
            buildPb.directory(templateDir.toFile());
            Process buildProc = buildPb.start();
            int exitCode = waitAndCollect(buildProc, Duration.ofMinutes(10));
            if (exitCode != 0) {
                throw new IOException("Не удалось собрать базовый образ: " + lastStderr);
            }
        }
        baseImageChecked = true;
    }

    private String filterMavenOutput(String output) {
        if (output == null) return "";
        StringBuilder filtered = new StringBuilder();
        String[] lines = output.split("\\R");
        boolean inTests = false;
        
        for (String line : lines) {
            String trimmed = line.trim();
            // Пропускаем служебные строки Maven [INFO], [WARNING] и т.д., если это не относится к тестам
            if (trimmed.startsWith("[INFO] Tests run:") || trimmed.startsWith("Tests run:")) {
                inTests = true;
            }
            
            if (trimmed.startsWith("[INFO]") || trimmed.startsWith("[WARNING]")) {
                // Если это итоговая статистика тестов или ошибки компиляции - оставляем
                if (trimmed.contains("Tests run:") || trimmed.contains("Compilation failure") || trimmed.contains("ERROR")) {
                    filtered.append(line).append("\n");
                }
                continue;
            }
            
            // Оставляем строки с результатами тестов и ошибки
            filtered.append(line).append("\n");
        }
        return filtered.toString().trim();
    }

    // --- helpers ---
    private void checkDocker() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("docker", "version");
        Process p = pb.start();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        Thread t1 = streamPump(p.getInputStream(), out);
        Thread t2 = streamPump(p.getErrorStream(), err);
        
        boolean finished = p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new IOException("Таймаут при проверке Docker. Возможно, демон завис.");
        }
        
        t1.join(500);
        t2.join(500);
        
        if (p.exitValue() != 0) {
            String errorMsg = err.toString(StandardCharsets.UTF_8);
            if (errorMsg.isBlank()) errorMsg = out.toString(StandardCharsets.UTF_8);
            throw new IOException("Docker не доступен: " + errorMsg.trim());
        }
    }

    private volatile String lastStdout = "";
    private volatile String lastStderr = "";

    private int waitAndCollect(Process p, Duration timeout) throws InterruptedException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        Thread t1 = streamPump(p.getInputStream(), out);
        Thread t2 = streamPump(p.getErrorStream(), err);
        boolean finished = p.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!finished) {
            p.destroyForcibly();
            t1.interrupt(); t2.interrupt();
            lastStdout = out.toString(StandardCharsets.UTF_8);
            lastStderr = err.toString(StandardCharsets.UTF_8);
            return 124; // timeout exit code
        }
        t1.join(1000); t2.join(1000);
        lastStdout = out.toString(StandardCharsets.UTF_8);
        lastStderr = err.toString(StandardCharsets.UTF_8);
        return p.exitValue();
    }

    private Thread streamPump(InputStream is, OutputStream os) {
        Thread t = new Thread(() -> {
            try (is; os) {
                is.transferTo(os);
            } catch (IOException ignored) {}
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(path -> {
            try {
                Path relative = source.relativize(path);
                Path dest = target.resolve(relative);
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
                .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
    }

    private String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}