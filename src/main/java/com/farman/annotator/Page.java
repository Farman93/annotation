package com.farman.annotator;


import java.util.ArrayList;
import java.util.List;

public class Page {
    private String title;
    private String text;
    private String plainText;
    private int id;
    private List<Link> links = new ArrayList<>();
    private List<Annotator.Data> annotations = new ArrayList<>();

    public List<Annotator.Data> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotator.Data> annotations) {
        this.annotations = annotations;
    }

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
