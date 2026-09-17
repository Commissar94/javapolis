package javapolis.course;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javapolis.course.CourseJava.*;
import static org.junit.jupiter.api.Assertions.*;

class ColiseumExamplesTest {
    private static final Pattern JAVA_BLOCK = Pattern.compile("^```java\\n(.*?)^```\\s*$",
            Pattern.MULTILINE | Pattern.DOTALL);
    private static final Map<String, String> EXAMPLES = new LinkedHashMap<>();
    private static CourseJava java;
    private static JsonNode chapters;
    private static Path compiledExamples;

    @TempDir
    static Path temporaryDirectory;

    @BeforeAll
    static void compilePublishedExamples() throws Exception {
        java = new CourseJava(temporaryDirectory);
        chapters = new ObjectMapper().readTree(COURSE.resolve("course.json").toFile()).get("chapters");
        Map<String, String> sources = new LinkedHashMap<>();
        for (JsonNode chapter : chapters) {
            int number = chapter.get("number").asInt();
            List<String> blocks = blocks(Files.readString(TOPICS.resolve(chapter.get("filename").asText())));
            for (int index = 1; index <= blocks.size(); index++) {
                String code = blocks.get(index - 1);
                String name = exampleName(number, index);
                EXAMPLES.put(name, code);
                // This lesson deliberately demonstrates a compile-time error.
                if (number == 4 && index == 2) {
                    continue;
                }
                String source;
                if (code.contains("public class Main")) {
                    source = code.replace("public class Main", "public class " + name);
                } else if (code.stripLeading().startsWith("static ")) {
                    source = "public class " + name + " {\n" + code + "\n}";
                } else {
                    source = "public class " + name + " { public static void main(String[] args) {\n"
                            + code + "\n}}";
                }
                sources.put(name, source);
            }
        }
        compiledExamples = java.compile(sources);
    }

    @Test
    void courseHas25ChaptersAnd40CompilableExamples() {
        assertEquals(25, chapters.size());
        assertEquals(IntStream.rangeClosed(1, 25).boxed().toList(),
                IntStream.range(0, chapters.size()).map(i -> chapters.get(i).get("number").asInt()).boxed().toList());
        assertEquals(41, EXAMPLES.size()); // 40 valid blocks and one deliberate error.
    }

    @Test
    void headingsNavigationImagesAndEmbeddedSolutionsRemainValid() throws Exception {
        int practices = 0;
        for (JsonNode chapter : chapters) {
            Path file = TOPICS.resolve(chapter.get("filename").asText());
            String text = Files.readString(file);
            assertTrue(text.startsWith("# "), file.toString());
            assertEquals(0, Pattern.compile("```").matcher(text).results().count() % 2, file.toString());
            var links = Pattern.compile("\\]\\((/university/[^)]+)\\)").matcher(text);
            while (links.find()) {
                // '+' is a literal character in a path, not form-encoded whitespace.
                String relative = URLDecoder.decode(links.group(1).substring("/university/".length())
                        .replace("+", "%2B"), UTF_8);
                assertTrue(Files.isRegularFile(TOPICS.getParent().resolve(relative + ".md")), links.group(1));
            }
            var images = Pattern.compile("!\\[[^\\]]*\\]\\(([^)]+)\\)").matcher(text);
            while (images.find()) {
                String target = images.group(1);
                assertTrue(Files.isRegularFile(file.getParent().resolve(target))
                        || Files.isRegularFile(ROOT.resolve("src/main/resources/images").resolve(target)), target);
            }
            if (chapter.get("kind").asText().equals("practice")) {
                practices++;
                String solution = java.solution(chapter.get("stage").asInt()).strip();
                assertTrue(blocks(text).stream().map(String::strip).anyMatch(solution::equals),
                        "Embedded solution differs from Main.java: " + file);
                assertEquals(2, Pattern.compile(Pattern.quote("{collapsible=\"true\"}"))
                        .matcher(text).results().count(), "Hint and solution required: " + file);
            }
        }
        assertEquals(5, practices);
    }

    @Test
    void uninitializedLocalVariableFailsCompilationForTheDocumentedReason() throws Exception {
        String code = EXAMPLES.get(exampleName(4, 2));
        var result = java.tryCompile(Map.of("Invalid",
                "class Invalid { public static void main(String[] args) {\n" + code + "\n}}"));
        assertFalse(result.success(), "The uninitialized-variable example must fail");
        assertTrue(result.diagnosticCodes().contains("compiler.err.var.might.not.have.been.initialized"),
                result.diagnostics());
    }

    @ParameterizedTest(name = "Chapter {0}, example {1}: documented output")
    @MethodSource("expectedOutputs")
    void examplesPrintTheDocumentedOutput(int chapter, int block, String expected) throws Exception {
        assertEquals(expected, java.run(compiledExamples, exampleName(chapter, block), ""));
    }

    static Stream<Arguments> expectedOutputs() {
        return Stream.of(
                Arguments.of(1, 1, "Арена открыта!"),
                Arguments.of(2, 1, "100\n100\nC\nСпартак"),
                Arguments.of(4, 1, "100\n80"), Arguments.of(4, 3, "100"), Arguments.of(4, 4, "100\n70"),
                Arguments.of(5, 1, "Гладиатор: Спартак\nЗдоровье: 100\nАрена открыта!"),
                Arguments.of(5, 2, "Урон: 105\nУрон: 15"),
                Arguments.of(5, 3, "Гладиатор по прозвищу \"Компилятор\"\nПервый бой\nВторой бой"),
                Arguments.of(7, 1, "87"), Arguments.of(7, 2, "3\n3.4\n3.4"),
                Arguments.of(8, 1, "true"), Arguments.of(8, 2, "true\nfalse\nfalse\ntrue"),
                Arguments.of(9, 1, "1"), Arguments.of(9, 2, "Противник повержен."),
                Arguments.of(9, 3, "Пора подумать о лечении."), Arguments.of(9, 4, "0"),
                Arguments.of(10, 1, "5\n100"),
                Arguments.of(12, 1, "Тренировочный удар: 1\nТренировочный удар: 2\nТренировочный удар: 3"),
                Arguments.of(14, 1, "Противник повержен.\nБой завершён."), Arguments.of(14, 2, "1\n3"),
                Arguments.of(17, 1, "true\nfalse"), Arguments.of(17, 2, "true"), Arguments.of(17, 3, "Спартак"),
                Arguments.of(18, 1, "1\n6\n100"), Arguments.of(20, 1, "Бой: 1\nБой: 2\nБой: 3"),
                Arguments.of(20, 2, "Бой 1, раунд 1\nБой 1, раунд 2\nБой 2, раунд 1\nБой 2, раунд 2"),
                Arguments.of(21, 1, "13"), Arguments.of(21, 2, "Добро пожаловать на арену!"),
                Arguments.of(22, 1, "50\n70"), Arguments.of(22, 3, "100\n3"));
    }

    @Test
    void scannerReadsTheWholeName() throws Exception {
        containsLines(java.run(compiledExamples, exampleName(16, 1), "Спартак Младший\n"),
                "На арену выходит: Спартак Младший");
    }

    private static List<String> blocks(String text) {
        return JAVA_BLOCK.matcher(text).results().map(match -> match.group(1)).toList();
    }

    private static String exampleName(int chapter, int block) {
        return "Example%02d_%02d".formatted(chapter, block);
    }
}
