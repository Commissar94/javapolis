package javapolis.service;

import javapolis.model.TopicStructure;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TopicServiceTest {

    @Test
    public void testProcessCollapsibleBlocksDouble() {
        TopicService service = new TopicService();
        String markdown = "{collapsible=\"true\"}\n" +
                "Header Text\n" +
                "{/collapsible}\n" +
                "Body Content\n" +
                "{/collapsible}";
        
        String result = service.processCollapsibleBlocks(markdown);
        System.out.println("Result:\n" + result);
        
        assertTrue(result.contains("class=\"collapsible\""));
        assertTrue(result.contains("class=\"collapsible-header\""));
        assertTrue(result.contains("Header Text"));
        assertTrue(result.contains("class=\"collapsible-content\""));
        assertTrue(result.contains("Body Content"));
    }

    @Test
    public void testProcessCollapsibleBlocksSingle() {
        TopicService service = new TopicService();
        String markdown = "{collapsible=\"true\"}\n" +
                "Header Line\n" +
                "Body Line 1\n" +
                "Body Line 2\n" +
                "{/collapsible}";
        
        String result = service.processCollapsibleBlocks(markdown);
        System.out.println("Result:\n" + result);
        
        assertTrue(result.contains("class=\"collapsible\""));
        assertTrue(result.contains("class=\"collapsible-header\""));
        assertTrue(result.contains("Header Line"));
        assertTrue(result.contains("Body Line 1"));
    }
    @Test
    public void testProcessCollapsibleBlocksMultiple() {
        TopicService service = new TopicService();
        String markdown = "{collapsible=\"true\"}\n" +
                "Header 1\n" +
                "{/collapsible}\n" +
                "Body 1\n" +
                "{/collapsible}\n" +
                "\n" +
                "{collapsible=\"true\"}\n" +
                "Header 2\n" +
                "{/collapsible}\n" +
                "Body 2\n" +
                "{/collapsible}";
        
        String result = service.processCollapsibleBlocks(markdown);
        System.out.println("Result:\n" + result);
        
        // Count occurrences of collapsible class
        int count = 0;
        int lastIndex = 0;
        while ((lastIndex = result.indexOf("class=\"collapsible\"", lastIndex)) != -1) {
            count++;
            lastIndex += "class=\"collapsible\"".length();
        }
        assertEquals(2, count);
    }
    @Test
    public void testProcessAboutMd() {
        TopicService service = new TopicService();
        String markdown = "{style=\"note\"}\n" +
                "В этой статье вы узнаете какое отношение к Java имеет Гослинг\n" +
                "{/style}\n" +
                "\n" +
                "\n" +
                "\n" +
                "{collapsible=\"true\"}\n" +
                "Скучный параграф который все пропускают\n" +
                "{/collapsible}\n" +
                "Java – один из самых популярных языков программирования в мире, который был разработан компанией Sun Microsystems в начале 1990-х годов. За свою более чем двадцатилетнюю историю Java стала одной из основных технологий для создания приложений и веб-сервисов.\n" +
                "\n" +
                "Сегодня Java используется во множестве областей, включая разработку мобильных приложений, веб-разработку, создание корпоративных приложений и игр. Она также является основным языком программирования для разработки Android-приложений.\n" +
                "\n" +
                "Java имеет большое сообщество разработчиков по всему миру, которые активно обмениваются опытом и создают новые инструменты и библиотеки для улучшения работы с языком. Благодаря этому Java остается одним из самых востребованных языков программирования и продолжает развиваться и совершенствоваться.\n" +
                "{/collapsible}";
        
        String result = service.processCollapsibleBlocks(markdown);
        System.out.println("Result:\n" + result);
        
        assertTrue(result.contains("class=\"collapsible\""), "Should contain collapsible class");
        assertTrue(result.contains("class=\"collapsible-header\""), "Should contain header class");
        assertTrue(result.contains("Скучный параграф который все пропускают"), "Should contain header text");
        assertTrue(result.contains("class=\"collapsible-content\""), "Should contain content class");
        assertTrue(result.contains("Sun Microsystems"), "Should contain body text");
    }
    @Test
    public void testGetTopicContent() throws Exception {
        TopicService service = new TopicService();
        // Since getTopicContent reads from classpath, we can only test files that exist.
        // Let's test "about"
        String html = service.getTopicContent("about");
        System.out.println("Full HTML output:\n" + html);
        
        assertTrue(html.contains("<div class=\"collapsible\">"), "HTML should contain collapsible div");
        assertTrue(html.contains("<div class=\"collapsible-header\">"), "HTML should contain collapsible header");
        assertTrue(html.contains("<div class=\"collapsible-content\">"), "HTML should contain collapsible content");
    }

    @Test
    public void testGetTopicStructure() {
        TopicService service = new TopicService();
        List<TopicStructure> structure = service.getTopicStructure();
        assertNotNull(structure);
        assertFalse(structure.isEmpty());
        
        // Проверяем наличие папки basics (она точно есть в ресурсах)
        boolean hasBasics = structure.stream()
            .anyMatch(s -> s.getName().equals("basics") && s.isFolder());
        assertTrue(hasBasics, "Structure should contain 'basics' folder");

        // Проверяем вложенность
        TopicStructure basics = structure.stream()
            .filter(s -> s.getName().equals("basics"))
            .findFirst().get();
        assertFalse(basics.getChildren().isEmpty(), "Basics folder should not be empty");
    }
}
