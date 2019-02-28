package com.example.pdfuploder;

public class Upload {
    public String name;
    public String url;

    public Upload(){

    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Upload(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
