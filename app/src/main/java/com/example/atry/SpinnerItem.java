package com.example.atry;

public class SpinnerItem {

    private String tagName;
    private int tagId;

    public SpinnerItem(String tagName, int tagId) {
        this.tagName = tagName;
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }
}
