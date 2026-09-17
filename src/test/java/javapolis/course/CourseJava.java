package javapolis.course;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

/** Compiles published examples and runs each program in its own JVM. */
final class CourseJava {
    static final Path ROOT = Path.of(System.getProperty("basedir", ".")).toAbsolutePath();
    static final Path COURSE = ROOT.resolve("courses/coliseum");
    static final Path TOPICS = ROOT.resolve("src/main/resources/topics/Колизей");

    private final Path temporaryDirectory;

    CourseJava(Path temporaryDirectory) {
        this.temporaryDirectory = temporaryDirectory;
    }

    String solution(int stage) throws IOException {
        return Files.readString(COURSE.resolve("solutions/stage-%02d/Main.java".formatted(stage)));
    }

    Path compile(String source) throws IOException {
        return compile(Map.of("Main", source));
    }

    Path compile(Map<String, String> sources) throws IOException {
        Compilation result = tryCompile(sources);
        assertTrue(result.success(), () -> "Compilation failed: " + result.diagnostics());
        return result.directory();
    }

    Compilation tryCompile(Map<String, String> sources) throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Run course tests with a JDK, not a JRE");
        Path directory = Files.createTempDirectory(temporaryDirectory, "java-");
        List<Path> files = new ArrayList<>();
        for (var entry : sources.entrySet()) {
            Path file = directory.resolve(entry.getKey() + ".java");
            Files.writeString(file, entry.getValue());
            files.add(file);
        }
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var messages = new StringWriter();
        try (var manager = compiler.getStandardFileManager(diagnostics, null, UTF_8)) {
            boolean success = compiler.getTask(messages, manager, diagnostics,
                    List.of("--release", "21", "-encoding", "UTF-8", "-proc:none",
                            "-classpath", directory.toString(), "-d", directory.toString()),
                    null, manager.getJavaFileObjectsFromPaths(files)).call();
            return new Compilation(directory, success,
                    diagnostics.getDiagnostics().stream().map(Diagnostic::getCode).toList(),
                    diagnostics.getDiagnostics() + "\n" + messages);
        }
    }

    String run(Path directory, String input) throws IOException, InterruptedException {
        return run(directory, "Main", input);
    }

    String run(Path directory, String className, String input) throws IOException, InterruptedException {
        Path stdin = Files.createTempFile(directory, "stdin-", ".txt");
        Path stdout = Files.createTempFile(directory, "stdout-", ".txt");
        Path stderr = Files.createTempFile(directory, "stderr-", ".txt");
        Files.writeString(stdin, input);
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        // Files avoid blocked pipes; closing stdin also exercises EOF correctly.
        Process process = new ProcessBuilder(java.toString(), "-Dfile.encoding=UTF-8",
                "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
                "-cp", directory.toString(), className)
                .redirectInput(stdin.toFile()).redirectOutput(stdout.toFile())
                .redirectError(stderr.toFile()).start();
        try {
            assertTrue(process.waitFor(5, TimeUnit.SECONDS), "Program timed out: " + className);
            String errors = Files.readString(stderr);
            String output = Files.readString(stdout).replace("\r\n", "\n").strip();
            assertEquals(0, process.exitValue(), () -> "Program failed: " + className
                    + "\n" + errors + "\n" + output);
            assertTrue(errors.isBlank(), () -> "Unexpected stderr: " + errors);
            return output;
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }
    }

    static String replace(String source, String before, String after) {
        assertTrue(source.contains(before), () -> "Update the test fixture: missing " + before);
        return source.replace(before, after);
    }

    static void containsLines(String output, String... expected) {
        List<String> lines = output.lines().toList();
        for (String line : expected) {
            assertTrue(lines.contains(line), () -> "Missing line: " + line + "\n" + output);
        }
    }

    static long countLines(String output, String prefix) {
        return output.lines().filter(line -> line.startsWith(prefix)).count();
    }

    static int value(String output, String label) {
        return Integer.parseInt(output.lines().filter(line -> line.startsWith(label + ": "))
                .reduce((first, last) -> last).orElseThrow(() -> new AssertionError(output))
                .substring(label.length() + 2));
    }

    record Compilation(Path directory, boolean success, List<String> diagnosticCodes, String diagnostics) {}
}
