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
    
    public Map<String, Object> getTopicContent(String topicPath, int page) throws IOException {
        System.out.println("=== GET TOPIC CONTENT ===");
        System.out.println("Input topicPath: " + topicPath + ", page: " + page);
        
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
            
            // Разделяем на страницы по маркеру ===
            // Используем regex для поддержки пробелов вокруг ===
            String[] splitPages = markdown.split("\n===\\s*\n|\n===\\s*$|^===\\s*\n");
            
            // Если после split получилось пустая строка в конце, убираем ее
            List<String> pagesList = new ArrayList<>();
            for (String p : splitPages) {
                if (!p.trim().isEmpty() || splitPages.length == 1) {
                    pagesList.add(p);
                }
            }
            String[] pages = pagesList.toArray(new String[0]);
            
            // Если после split получилось меньше страниц, чем запрошено, берем последнюю
            // Или если страница < 0, берем первую
            int pageIndex = Math.max(0, Math.min(page, pages.length - 1));
            String pageMarkdown = pages[pageIndex];

            // Сначала обрабатываем специальные стили в markdown
            pageMarkdown = processStyleTags(pageMarkdown);
            
            // Обрабатываем {collapsible="true"} теги
            pageMarkdown = processCollapsibleBlocks(pageMarkdown);
            
            // Обрабатываем {quiz} теги
            pageMarkdown = processQuizzes(pageMarkdown);

            // Обрабатываем {code-task} теги
            pageMarkdown = processCodeTasks(pageMarkdown);
            
            // Конвертируем markdown в HTML
            Node document = parser.parse(pageMarkdown);
            String html = renderer.render(document);
            
            // Обрабатываем пути к изображениям
            html = processImagePaths(html, topicPath);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("content", html);
            result.put("currentPage", pageIndex);
            result.put("totalPages", pages.length);
            
            return result;
        }
    }

    public String getTopicContent(String topicPath) throws IOException {
        Map<String, Object> result = getTopicContent(topicPath, 0);
        return (String) result.get("content");
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

    private String processQuizzes(String markdown) {
        Pattern quizPattern = Pattern.compile("\\{quiz\\}(.*?)\\{/quiz\\}", Pattern.DOTALL);
        Matcher matcher = quizPattern.matcher(markdown);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String content = matcher.group(1).trim();
            String[] lines = content.split("\\n");
            
            StringBuilder questionBuilder = new StringBuilder();
            List<String> options = new ArrayList<>();
            List<Boolean> correctList = new ArrayList<>();
            
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("+ ")) {
                    options.add(line.substring(2).trim());
                    correctList.add(true);
                } else if (line.startsWith("- ")) {
                    options.add(line.substring(2).trim());
                    correctList.add(false);
                } else if (!line.isEmpty()) {
                    if (questionBuilder.length() > 0) questionBuilder.append("\n");
                    questionBuilder.append(line);
                }
            }
            
            long correctCount = correctList.stream().filter(b -> b).count();
            String type = correctCount > 1 ? "multiple" : "single";
            
            StringBuilder html = new StringBuilder();
            html.append("<div class=\"quiz-block\" data-type=\"").append(type).append("\">");
            html.append("<div class=\"quiz-header\"><i class=\"fas fa-question-circle\"></i> Проверь себя</div>");
            html.append("<div class=\"quiz-question\">").append(renderMarkdown(questionBuilder.toString())).append("</div>");
            html.append("<div class=\"quiz-options\">");
            
            for (int i = 0; i < options.size(); i++) {
                html.append("<div class=\"quiz-option\" data-correct=\"").append(correctList.get(i)).append("\">");
                html.append("<div class=\"quiz-option-checkbox\"></div>");
                html.append("<div class=\"quiz-option-text\">").append(renderMarkdown(options.get(i))).append("</div>");
                html.append("</div>");
            }
            
            html.append("</div>");
            html.append("<button class=\"quiz-check-btn\">Проверить ответ</button>");
            html.append("<div class=\"quiz-feedback\"></div>");
            html.append("</div>");
            
            matcher.appendReplacement(sb, Matcher.quoteReplacement(html.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private String renderMarkdown(String markdown) {
        Node document = parser.parse(markdown);
        String html = renderer.render(document);
        // Убираем лишние <p> теги, если это одна строка
        if (html.startsWith("<p>") && html.endsWith("</p>\n") && html.indexOf("<p>", 3) == -1) {
            html = html.substring(3, html.length() - 5);
        }
        return html;
    }

    private String processCodeTasks(String markdown) {
        // {code-task id="sum" language="java"}...markdown условия...{/code-task}
        Pattern pattern = Pattern.compile("\\{code-task(.*?)\\}(.*?)\\{/code-task\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(markdown);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String attrs = matcher.group(1);
            String body = matcher.group(2).trim();

            String id = extractAttr(attrs, "id");
            String language = extractAttr(attrs, "language");
            if (language == null || language.isBlank()) language = "java";

            String leftHtml = renderMarkdown(body);

            StringBuilder html = new StringBuilder();
            html.append("<div class=\"code-task\" data-id=\"").append(id == null ? "" : id)
                .append("\" data-language=\"").append(language).append("\">\n");
            html.append("  <div class=\"code-task-left\">").append(leftHtml).append("</div>\n");
            html.append("  <div class=\"code-task-right\">\n");
            html.append("    <div class=\"code-editor-wrapper\">\n");
            html.append("      <textarea class=\"code-editor\" spellcheck=\"false\" placeholder=\"Введите код решения здесь...\"></textarea>\n");
            html.append("    </div>\n");
            html.append("    <div class=\"code-task-actions\">\n");
            html.append("      <button class=\"code-run-btn\"><i class=\"fas fa-play\"></i> Запустить тесты</button>\n");
            html.append("    </div>\n");
            html.append("    <pre class=\"code-output\" style=\"display:none;\"></pre>\n");
            html.append("  </div>\n");
            html.append("</div>");

            matcher.appendReplacement(sb, Matcher.quoteReplacement(html.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String extractAttr(String attrs, String name) {
        if (attrs == null) return null;
        Pattern p = Pattern.compile(name + "=\"(.*?)\"");
        Matcher m = p.matcher(attrs);
        if (m.find()) return m.group(1);
        return null;
    }
}
