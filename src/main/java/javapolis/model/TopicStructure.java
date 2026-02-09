package javapolis.model;

import java.util.List;
import java.util.ArrayList;

public class TopicStructure {
    private String name;
    private String type; // "folder" или "file"
    private String path; // путь к файлу или папке
    private List<TopicStructure> children;
    private boolean expanded; // для папок - развернута ли по умолчанию
    private boolean completed; // пройдена ли тема

    public TopicStructure() {
        this.children = new ArrayList<>();
        this.expanded = false;
        this.completed = false;
    }

    public TopicStructure(String name, String type, String path) {
        this();
        this.name = name;
        this.type = type;
        this.path = path;
    }

    public TopicStructure(String name, String type, String path, boolean completed) {
        this(name, type, path);
        this.completed = completed;
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<TopicStructure> getChildren() {
        return children;
    }

    public void setChildren(List<TopicStructure> children) {
        this.children = children;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void addChild(TopicStructure child) {
        this.children.add(child);
    }

    public boolean isFolder() {
        return "folder".equals(type);
    }

    public boolean isFile() {
        return "file".equals(type);
    }
}
