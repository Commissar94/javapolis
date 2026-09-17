package javapolis.service;

import javapolis.model.TopicStructure;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ColiseumCourseTest {
    private final TopicService service = new TopicService();

    private TopicStructure course() {
        return service.getTopicStructure().stream()
                .filter(topic -> topic.getPath().equals("Колизей"))
                .findFirst().orElseThrow();
    }

    @Test
    void chaptersFollowTheLearningSequenceAndKeepExistingPaths() {
        List<TopicStructure> chapters = course().getChildren();
        assertEquals(25, chapters.size());
        for (int i = 0; i < chapters.size(); i++) {
            assertTrue(chapters.get(i).getPath().startsWith("Колизей/" + (i + 1) + "_"),
                    chapters.get(i).getPath());
        }
        assertEquals("Колизей/1_О курсе.md", chapters.get(0).getPath());
        assertEquals("Колизей/2_Литералы.md", chapters.get(1).getPath());
        assertEquals("Колизей/3_Примитивы.md", chapters.get(2).getPath());
    }

    @Test
    void allChaptersRenderAndEachPracticeHasAHiddenCompleteSolution() throws Exception {
        int practices = 0;
        for (TopicStructure topic : course().getChildren()) {
            String html = service.getTopicContent(topic.getPath());
            assertTrue(html.contains("<h1>"), topic.getPath());
            assertTrue(html.contains("/university/"), "Missing navigation: " + topic.getPath());
            assertFalse(html.contains("{collapsible="), topic.getPath());
            assertFalse(html.contains("{/collapsible}"), topic.getPath());
            assertFalse(html.contains("{quiz}"), topic.getPath());
            assertFalse(html.contains("{style="), topic.getPath());
            if (topic.getName().startsWith("Этап ")) {
                practices++;
                assertTrue(html.contains("class=\"collapsible-content\""), topic.getPath());
                assertTrue(html.contains("public class Main"), topic.getPath());
                assertTrue(html.contains("class=\"language-java\""), topic.getPath());
            }
        }
        assertEquals(5, practices);
    }

    @Test
    void orderingHandlesFoldersNumbersAndOrdinaryNamesConsistently() {
        List<TopicStructure> topics = new ArrayList<>(List.of(
                new TopicStructure("ten", "file", "course/10_Ten.md"),
                new TopicStructure("two", "file", "course/2_Two.md"),
                new TopicStructure("plain", "file", "course/Appendix.md"),
                new TopicStructure("folder", "folder", "course/Z"),
                new TopicStructure("one", "file", "course/01_One.md"),
                new TopicStructure("unprefixed", "file", "course/1a.md")));
        topics.sort(TopicService::compareTopics);
        assertEquals(List.of("course/Z", "course/01_One.md", "course/2_Two.md",
                "course/10_Ten.md", "course/1a.md", "course/Appendix.md"),
                topics.stream().map(TopicStructure::getPath).toList());
    }
}
