package javapolis.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import javapolis.service.TopicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class FactoryCourseTest {
    private static final Path ROOT = CourseJava.ROOT;
    private static final Path TOPICS = ROOT.resolve("src/main/resources/topics");
    private static final Path COURSE = ROOT.resolve("courses/factory");
    private static final Pattern JAVA = Pattern.compile("^```java\\n(.*?)^```\\s*$", Pattern.MULTILINE | Pattern.DOTALL);
    @TempDir Path temporaryDirectory;

    @Test void publishedChaptersRenderInOrderAndContainTheTestedSolutions() throws Exception {
        var manifest = new ObjectMapper().readTree(COURSE.resolve("course.json").toFile());
        var service = new TopicService();
        var published = service.getTopicStructure().stream().filter(t -> t.getPath().equals("Завод"))
                .findFirst().orElseThrow().getChildren();
        assertEquals(17, published.size());
        assertEquals(17, manifest.get("chapters").size());
        int practices = 0;
        for (var chapter : manifest.get("chapters")) {
            int number = chapter.get("number").asInt();
            String filename = chapter.get("filename").asText();
            assertEquals("Завод/" + filename, published.get(number - 1).getPath());
            String source = Files.readString(TOPICS.resolve("Завод").resolve(filename));
            assertTrue(source.startsWith("# "));
            assertEquals(0, Pattern.compile("```").matcher(source).results().count() % 2);
            var navigation = Pattern.compile("\\]\\(/university/([^)]+)\\)").matcher(source);
            int links = 0;
            while (navigation.find()) {
                String target = URLDecoder.decode(navigation.group(1).replace("+", "%2B"), UTF_8);
                assertTrue(Files.isRegularFile(TOPICS.resolve(target + ".md")), target);
                links++;
            }
            assertTrue(links > 0, filename);
            String html = service.getTopicContent("Завод/" + filename);
            assertTrue(html.contains("<h1>"));
            assertFalse(html.contains("{collapsible="));
            assertFalse(html.contains("{/collapsible}"));
            if (chapter.get("kind").asText().equals("practice")) {
                practices++;
                String name = chapter.get("studentClass").asText();
                String solution = Files.readString(COURSE.resolve("starter/solutions/javapolis/factory/student/" + name + ".java")).strip();
                assertTrue(blocks(source).stream().map(String::strip).anyMatch(solution::equals), filename);
                assertTrue(html.contains("class=\"collapsible-content\""));
                assertTrue(source.contains("-Pstage-" + chapter.get("stage").asInt() + " test"));
            }
        }
        assertEquals(5, practices);
        for (var topic : manifest.get("reusedTopics")) assertTrue(Files.isRegularFile(TOPICS.resolve(topic.asText())));
    }

    @Test void standaloneClassAndEnumExamplesPrintTheDocumentedResults() throws Exception {
        var java = new CourseJava(temporaryDirectory);
        String classes = blocks(Files.readString(TOPICS.resolve("classes.md"))).getFirst();
        assertEquals("1\n0", java.run(java.compile(classes), ""));
        String enums = blocks(Files.readString(TOPICS.resolve("Завод/3_Перечисления ресурсов.md"))).get(1);
        assertEquals("Отправляем на пресс", java.run(java.compile(enums), ""));
    }

    @Test void compositionAndMapExamplesWorkWithThePublishedInventory() throws Exception {
        var java = new CourseJava(temporaryDirectory);
        String inventory = Files.readString(COURSE.resolve("starter/solutions/javapolis/factory/student/Inventory.java"))
                .replace("package javapolis.factory.student;", "").replace("import javapolis.factory.model.Resource;", "");
        String resource = Files.readString(COURSE.resolve("starter/src/main/java/javapolis/factory/model/Resource.java"))
                .replace("package javapolis.factory.model;", "");
        String composition = blocks(Files.readString(TOPICS.resolve("Завод/8_Композиция и ссылки.md"))).getFirst();
        Path compiled = java.compile(Map.of("Inventory", inventory, "Resource", resource,
                "Main", "public class Main { public static void main(String[] args) {" + composition + "}}"));
        assertEquals("3\n0", java.run(compiled, ""));
        String map = blocks(Files.readString(TOPICS.resolve("advanced/collections.md"))).stream()
                .filter(s -> s.contains("amounts.getOrDefault")).findFirst().orElseThrow();
        compiled = java.compile(Map.of("Resource", resource, "Main",
                "import java.util.*; public class Main { public static void main(String[] args) {"
                        + "Map<Resource,Integer> amounts = new EnumMap<>(Resource.class);" + map
                        + "System.out.println(ore);System.out.println(total);}}"));
        assertEquals("0\n1", java.run(compiled, ""));
    }

    @Test void publishedJUnitExampleCompilesAndRunsAgainstTheReferenceCode() throws Exception {
        Path example = temporaryDirectory.resolve("MyInventoryTest.java");
        Files.writeString(example, blocks(Files.readString(TOPICS.resolve("Завод/13_JUnit первая проверка.md"))).getFirst());
        List<Path> files = new ArrayList<>(List.of(example,
                COURSE.resolve("starter/solutions/javapolis/factory/student/Inventory.java"),
                COURSE.resolve("starter/src/main/java/javapolis/factory/model/Resource.java")));
        var compiler = ToolProvider.getSystemJavaCompiler();
        try (var manager = compiler.getStandardFileManager(null, null, UTF_8)) {
            assertTrue(compiler.getTask(null, manager, null,
                    List.of("--release", "21", "-encoding", "UTF-8", "-proc:none", "-classpath",
                            System.getProperty("java.class.path"), "-d", temporaryDirectory.toString()),
                    null, manager.getJavaFileObjectsFromPaths(files)).call());
        }
        try (var loader = new URLClassLoader(new java.net.URL[]{temporaryDirectory.toUri().toURL()}, getClass().getClassLoader())) {
            Class<?> test = loader.loadClass("javapolis.factory.MyInventoryTest");
            var constructor = test.getDeclaredConstructor(); constructor.setAccessible(true);
            var method = test.getDeclaredMethod("refusesOverflowWithoutChangingStock"); method.setAccessible(true);
            method.invoke(constructor.newInstance());
        }
    }

    private static List<String> blocks(String source) {
        return JAVA.matcher(source).results().map(m -> m.group(1)).toList();
    }
}
