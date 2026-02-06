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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
            .escapeHtml(false)
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
        try {
            ClassPathResource topicsDir = new ClassPathResource("topics");
            File root = topicsDir.getFile();
            return scanDirectory(root, "");
        } catch (IOException e) {
            System.err.println("Failed to scan topics directory: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<TopicStructure> scanDirectory(File directory, String relativePath) {
        List<TopicStructure> result = new ArrayList<>();
        File[] files = directory.listFiles();
        
        if (files == null) return result;

        for (File file : files) {
            String name = file.getName();
            String currentRelativePath = relativePath.isEmpty() ? name : relativePath + "/" + name;
            
            if (file.isDirectory()) {
                TopicStructure folder = new TopicStructure(name, "folder", currentRelativePath);
                folder.setChildren(scanDirectory(file, currentRelativePath));
                // По умолчанию разворачиваем папки
                folder.setExpanded(true);
                if (!folder.getChildren().isEmpty()) {
                    result.add(folder);
                }
            } else if (name.endsWith(".md")) {
                String topicName = name.substring(0, name.length() - 3);
                result.add(new TopicStructure(topicName, "file", currentRelativePath));
            }
        }
        
        // Сортируем: сначала папки, потом файлы, по имени
        result.sort((a, b) -> {
            if (a.getType().equals(b.getType())) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
            return a.getType().equals("folder") ? -1 : 1;
        });
        
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
            html = processImagePaths(html, topicPath);
            
            return html;
        }
    }

    public ClassPathResource getImageResource(String path) {
        // Сначала пробуем найти в папке topics (относительно топика)
        ClassPathResource topicsResource = new ClassPathResource("topics/" + path);
        if (topicsResource.exists()) {
            return topicsResource;
        }
        
        // Затем пробуем найти в общей папке images
        ClassPathResource imagesResource = new ClassPathResource("images/" + path);
        if (imagesResource.exists()) {
            return imagesResource;
        }

        // Если путь содержит имя файла, пробуем найти только имя файла в папке images
        String fileName = path;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            fileName = path.substring(lastSlash + 1);
        }
        ClassPathResource fileNameResource = new ClassPathResource("images/" + fileName);
        if (fileNameResource.exists()) {
            return fileNameResource;
        }
        
        return topicsResource; // Возвращаем несуществующий ресурс из topics по умолчанию
    }
    
    private String processImagePaths(String html, String topicPath) {
        // Извлекаем директорию из пути к топику
        String directory = "";
        int lastSlash = topicPath.lastIndexOf('/');
        if (lastSlash != -1) {
            directory = topicPath.substring(0, lastSlash + 1);
        }

        // Заменяем относительные пути (не начинающиеся с / или http или https)
        Pattern pattern = Pattern.compile("src=\"(?!(/|http:|https:))([^\"]+)\"");
        Matcher matcher = pattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String relativePath = matcher.group(2);
            String fullPath = directory + relativePath;
            // Кодируем путь для URL
            String encodedPath = java.net.URLEncoder.encode(fullPath, StandardCharsets.UTF_8);
            matcher.appendReplacement(sb, "src=\"/api/university/image?path=" + encodedPath + "\"");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private String processStyleTags(String markdown) {
        String result = markdown.replaceAll(
            "\\{style=\"tip\"\\}(.*?)\\{/style\\}",
            "<div class=\"tip\"><i class=\"fas fa-lightbulb\"></i><div class=\"admonition-content\">\n\n$1\n\n</div></div>"
        );
        
        result = result.replaceAll(
            "\\{style=\"note\"\\}(.*?)\\{/style\\}",
            "<div class=\"tip\"><i class=\"fas fa-lightbulb\"></i><div class=\"admonition-content\">\n\n$1\n\n</div></div>"
        );
        
        result = result.replaceAll(
            "\\{style=\"warning\"\\}(.*?)\\{/style\\}",
            "<div class=\"warning\"><i class=\"fas fa-exclamation-triangle\"></i><div class=\"admonition-content\">\n\n$1\n\n</div></div>"
        );
        
        result = result.replaceAll(
            "\\{style=\"info\"\\}(.*?)\\{/style\\}",
            "<div class=\"info\"><i class=\"fas fa-info-circle\"></i><div class=\"admonition-content\">\n\n$1\n\n</div></div>"
        );
        
        result = result.replaceAll(
            "\\{style=\"success\"\\}(.*?)\\{/style\\}",
            "<div class=\"success\"><i class=\"fas fa-check-circle\"></i><div class=\"admonition-content\">\n\n$1\n\n</div></div>"
        );

        if (result.equals(markdown)) {
            result = markdown.replaceAll("\\{style=\"tip\"\\}", "<div class=\"tip\"><i class=\"fas fa-lightbulb\"></i><div class=\"admonition-content\">\n\n");
            result = result.replaceAll("\\{style=\"note\"\\}", "<div class=\"tip\"><i class=\"fas fa-lightbulb\"></i><div class=\"admonition-content\">\n\n");
            result = result.replaceAll("\\{style=\"warning\"\\}", "<div class=\"warning\"><i class=\"fas fa-exclamation-triangle\"></i><div class=\"admonition-content\">\n\n");
            result = result.replaceAll("\\{style=\"info\"\\}", "<div class=\"info\"><i class=\"fas fa-info-circle\"></i><div class=\"admonition-content\">\n\n");
            result = result.replaceAll("\\{style=\"success\"\\}", "<div class=\"success\"><i class=\"fas fa-check-circle\"></i><div class=\"admonition-content\">\n\n");
            result = result.replaceAll("\\{/style\\}", "\n\n</div></div>");
        }
        return result;
    }

    public String processCollapsibleBlocks(String markdown) {
        // Ищем блоки {collapsible="true"}...{/collapsible}
        
        String result = markdown;
        boolean found;
        do {
            found = false;
            
            // Сначала ищем блоки с явным разделением заголовка: 
            // {collapsible="true"} Header {/collapsible} Body {/collapsible}
            Pattern p = Pattern.compile("\\{collapsible=\"true\"\\}(.*?)\\{/collapsible\\}(.*?)\\{/collapsible\\}", Pattern.DOTALL);
            Matcher m = p.matcher(result);
            if (m.find()) {
                String header = m.group(1).trim();
                String body = m.group(2).trim();
                
                String replacement = "<div class=\"collapsible\"><div class=\"collapsible-header\">" +
                                   header + "</div><div class=\"collapsible-content\">" +
                                   "\n\n" + body + "\n\n" + "</div></div>";
                result = result.substring(0, m.start()) + replacement + result.substring(m.end());
                found = true;
                continue;
            }
            
            // Если не нашли двойной, ищем обычный одиночный (заголовок - первая строка)
            Pattern p2 = Pattern.compile("\\{collapsible=\"true\"\\}(.*?)\\{/collapsible\\}", Pattern.DOTALL);
            Matcher m2 = p2.matcher(result);
            if (m2.find()) {
                String fullContent = m2.group(1).trim();
                String header;
                String body;
                
                String[] lines = fullContent.split("\\R", 2);
                header = lines[0].trim();
                body = lines.length > 1 ? lines[1].trim() : "";
                
                String replacement = "<div class=\"collapsible\"><div class=\"collapsible-header\">" +
                                   header + "</div><div class=\"collapsible-content\">" +
                                   "\n\n" + body + "\n\n" + "</div></div>";
                result = result.substring(0, m2.start()) + replacement + result.substring(m2.end());
                found = true;
            }
            
        } while (found);
        
        return result;
    }
}
