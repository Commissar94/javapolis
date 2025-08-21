package javapolis.service;

import javapolis.model.TopicStructure;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.ext.gfm.tables.TablesExtension;
import java.util.Arrays;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class TopicService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public TopicService() {
        this.parser = Parser.builder()
            .extensions(Arrays.asList(TablesExtension.create()))
            .build();
        
        this.renderer = HtmlRenderer.builder()
            .extensions(Arrays.asList(TablesExtension.create()))
            .attributeProviderFactory(new AttributeProviderFactory() {
                @Override
                public AttributeProvider create(AttributeProviderContext context) {
                    return new AttributeProvider() {
                        @Override
                        public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
                            if (tagName.equals("img")) {
                                attributes.put("class", "img-fluid");
                                attributes.put("style", "max-width: 100%; height: auto;");
                            }
                        }
                    };
                }
            })
            .build();
    }

    public List<TopicStructure> getTopicStructure() {
        List<TopicStructure> result = new ArrayList<>();
        
        // Файлы в корневой папке
        result.add(new TopicStructure("about", "file", "about.md"));
        result.add(new TopicStructure("primitives", "file", "primitives.md"));
        result.add(new TopicStructure("literals", "file", "literals.md"));
        result.add(new TopicStructure("classes", "file", "classes.md"));
        result.add(new TopicStructure("ide", "file", "ide.md"));
        result.add(new TopicStructure("Tutorial", "file", "Tutorial.md"));
        result.add(new TopicStructure("Default-topic", "file", "Default-topic.md"));
        
        // Папка basics
        TopicStructure basicsFolder = new TopicStructure("basics", "folder", "basics");
        basicsFolder.setExpanded(true);
        basicsFolder.addChild(new TopicStructure("hello-world", "file", "basics/hello-world.md"));
        basicsFolder.addChild(new TopicStructure("variables", "file", "basics/variables.md"));
        result.add(basicsFolder);
        
        // Папка advanced
        TopicStructure advancedFolder = new TopicStructure("advanced", "folder", "advanced");
        advancedFolder.setExpanded(true);
        advancedFolder.addChild(new TopicStructure("collections", "file", "advanced/collections.md"));
        result.add(advancedFolder);
        
        // Папка Задание 1
        TopicStructure task1Folder = new TopicStructure("Задание 1", "folder", "Задание 1");
        task1Folder.setExpanded(true);
        task1Folder.addChild(new TopicStructure("Task-1", "file", "Задание 1/Task-1.md"));
        task1Folder.addChild(new TopicStructure("task-2", "file", "Задание 1/task-2.md"));
        task1Folder.addChild(new TopicStructure("task-3", "file", "Задание 1/task-3.md"));
        task1Folder.addChild(new TopicStructure("task-4", "file", "Задание 1/task-4.md"));
        task1Folder.addChild(new TopicStructure("task-5", "file", "Задание 1/task-5.md"));
        result.add(task1Folder);
        
        return result;
    }
    
    public String getTopicContent(String topicPath) throws IOException {
        System.out.println("=== GET TOPIC CONTENT ===");
        System.out.println("Input topicPath: " + topicPath);
        
        // Убираем расширение .md если оно есть
        if (topicPath.endsWith(".md")) {
            topicPath = topicPath.substring(0, topicPath.length() - 3);
            System.out.println("Removed .md, new topicPath: " + topicPath);
        }
        
        String fullPath = "topics/" + topicPath + ".md";
        System.out.println("Looking for file: " + fullPath);
        
        ClassPathResource resource = new ClassPathResource(fullPath);
        System.out.println("Resource exists: " + resource.exists());
        
        if (!resource.exists()) {
            throw new IOException("Топик не найден: " + topicPath);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            String markdown = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            // Сначала обрабатываем специальные стили в markdown
            markdown = processStyleTags(markdown);
            
            // Обрабатываем {collapsible="true"} теги
            markdown = processCollapsibleBlocks(markdown);
            
            // Конвертируем markdown в HTML
            Node document = parser.parse(markdown);
            String html = renderer.render(document);
            
            // Обрабатываем пути к изображениям
            html = processImagePaths(html);
            
            return html;
        }
    }

    private String processImagePaths(String html) {
        html = html.replaceAll("src=\"([^\"]+)\"", "src=\"/images/$1\"");
        return html;
    }
    
    private String processStyleTags(String markdown) {
        String result = markdown.replaceAll(
            "\\{style=\"tip\"\\}(.*?)\\{/style\\}",
            "<div class=\"tip\"><i class=\"fas fa-lightbulb\"></i>$1</div>"
        );
        
        result = result.replaceAll(
            "\\{style=\"note\"\\}(.*?)\\{/style\\}",
            "<div class=\"tip\"><i class=\"fas fa-lightbulb\"></i>$1</div>"
        );
        
        result = result.replaceAll(
            "\\{style=\"warning\"\\}(.*?)\\{/style\\}",
            "<div class=\"warning\"><i class=\"fas fa-exclamation-triangle\"></i>$1</div>"
        );
        
        result = result.replaceAll(
            "\\{style=\"info\"\\}(.*?)\\{/style\\}",
            "<div class=\"info\"><i class=\"fas fa-info-circle\"></i>$1</div>"
        );
        
        result = result.replaceAll(
            "\\{style=\"success\"\\}(.*?)\\{/style\\}",
            "<div class=\"success\"><i class=\"fas fa-check-circle\"></i>$1</div>"
        );

        if (result.equals(markdown)) {
            result = markdown.replaceAll("\\{style=\"tip\"\\}", "<div class=\"tip\"><i class=\"fas fa-lightbulb\"></i>");
            result = result.replaceAll("\\{/style\\}", "</div>");
            result = result.replaceAll("\\{style=\"note\"\\}", "<div class=\"tip\"><i class=\"fas fa-lightbulb\"></i>");
            result = result.replaceAll("\\{style=\"warning\"\\}", "<div class=\"warning\"><i class=\"fas fa-exclamation-triangle\"></i>");
            result = result.replaceAll("\\{style=\"info\"\\}", "<div class=\"info\"><i class=\"fas fa-info-circle\"></i>");
            result = result.replaceAll("\\{style=\"success\"\\}", "<div class=\"success\"><i class=\"fas fa-check-circle\"></i>");
        }
        return result;
    }

    private String processCollapsibleBlocks(String markdown) {
        Pattern pattern = Pattern.compile("\\{collapsible=\"true\"\\}(.*?)\\{/collapsible\\}(.*?)\\{/collapsible\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(markdown);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String header = matcher.group(1).trim();
            String content = matcher.group(2).trim();

            String replacement = "<div class=\"collapsible\"><div class=\"collapsible-header\">" +
                               header + "</div><div class=\"collapsible-content\">" +
                               content + "</div></div>";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
