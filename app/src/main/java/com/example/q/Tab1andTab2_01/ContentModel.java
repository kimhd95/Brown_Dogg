package com.example.q.Tab1andTab2_01;

public class ContentModel {
    private String title, author, text;

    public ContentModel(String title, String author, String text) {
        this.title = title;
        this.author = author;
        this.text = text;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
