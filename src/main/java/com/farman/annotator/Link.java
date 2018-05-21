package com.farman.annotator;

public class Link {
    private String title;
    private String titleTokenized;
    private float index;

    public Link(String title, String titleTokenized, int index) {
        this.title = title;
        this.titleTokenized = titleTokenized;
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getIndex() {
        return index;
    }

    public void setIndex(float index) {
        this.index = index;
    }

    public String getTitleTokenized() {
        return titleTokenized;
    }

    public void setTitleTokenized(String titleTokenized) {
        this.titleTokenized = titleTokenized;
    }
}
