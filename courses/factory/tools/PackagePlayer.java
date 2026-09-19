import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Run from the repository root: java courses/factory/tools/PackagePlayer.java [output.zip]. */
public class PackagePlayer {
    private static final Pattern TOPIC_LINK = Pattern.compile("\\]\\(/university/([^)]+)\\)");

    public static void main(String[] args) throws IOException {
        Path root = Path.of("").toAbsolutePath();
        Path starter = root.resolve("courses/factory/starter");
        Path topics = root.resolve("src/main/resources/topics");
        if (!Files.isRegularFile(starter.resolve("pom.xml"))) {
            throw new IllegalArgumentException("Запускай упаковщик из корня репозитория JavaPolis");
        }
        Path output = args.length == 0 ? root.resolve("target/factory-player.zip") : Path.of(args[0]).toAbsolutePath();
        Map<String, byte[]> entries = new TreeMap<>();
        try (var files = Files.walk(starter)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                String relative = unix(starter.relativize(file));
                if (relative.startsWith("src/") || relative.startsWith("solutions/")
                        || relative.startsWith(".mvn/") || relative.equals("pom.xml")
                        || relative.equals("mvnw") || relative.equals("mvnw.cmd")
                        || relative.equals("README.md") || relative.equals(".gitignore")
                        || relative.equals(".gitattributes")) {
                    entries.put(relative, Files.readAllBytes(file));
                }
            }
        }
        for (String folder : new String[]{"Завод", "Колизей"}) {
            try (var files = Files.walk(topics.resolve(folder))) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    addLesson(entries, topics, file);
                }
            }
        }
        addLesson(entries, topics, topics.resolve("classes.md"));
        addLesson(entries, topics, topics.resolve("advanced/collections.md"));
        for (String asset : new String[]{"bts.gif", "litterally_1.gif"}) {
            entries.put("lessons/Колизей/" + asset,
                    Files.readAllBytes(root.resolve("src/main/resources/images").resolve(asset)));
        }
        StringBuilder index = new StringBuilder("# Завод 1 — главы курса\n\nОткрывай Markdown в IDE или редакторе с предпросмотром.\n"
                + "Блоки `{collapsible}` — оформление сайта; в локальном Markdown решения видны как обычный текст.\n\n");
        try (var files = Files.list(topics.resolve("Завод"))) {
            for (Path file : files.sorted((a, b) -> Integer.compare(number(a), number(b))).toList()) {
                String filename = file.getFileName().toString();
                index.append("- [").append(filename.replace(".md", ""))
                        .append("](<Завод/").append(filename).append(">)\n");
            }
        }
        index.append("\nОбщие главы: [Классы](classes.md), [Коллекции](advanced/collections.md).\n");
        entries.put("lessons/README.md", index.toString().getBytes(StandardCharsets.UTF_8));
        entries.put("course.json", Files.readAllBytes(root.resolve("courses/factory/course.json")));
        Files.createDirectories(output.getParent());
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(output), StandardCharsets.UTF_8)) {
            for (var item : entries.entrySet()) {
                ZipEntry entry = new ZipEntry("factory-player/" + item.getKey());
                // A positive fixed timestamp also lets Maven detect resources in a fresh extraction.
                entry.setTime(946684800000L);
                zip.putNextEntry(entry);
                zip.write(item.getValue());
                zip.closeEntry();
            }
        }
        System.out.println(output + " — " + entries.size() + " файлов, " + Files.size(output) + " байт");
    }

    private static void addLesson(Map<String, byte[]> entries, Path topics, Path file) throws IOException {
        String text = Files.readString(file);
        var matcher = TOPIC_LINK.matcher(text);
        String transformed = matcher.replaceAll(match -> {
            String topic = URLDecoder.decode(match.group(1).replace("+", "%2B"), StandardCharsets.UTF_8);
            Path target = topics.resolve(topic + ".md");
            String relative = unix(file.getParent().relativize(target));
            return java.util.regex.Matcher.quoteReplacement("](<" + relative + ">)");
        });
        entries.put("lessons/" + unix(topics.relativize(file)), transformed.getBytes(StandardCharsets.UTF_8));
    }

    private static String unix(Path path) { return path.toString().replace('\\', '/'); }
    private static int number(Path path) { return Integer.parseInt(path.getFileName().toString().split("_")[0]); }
}
