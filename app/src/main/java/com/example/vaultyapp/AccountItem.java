package com.example.vaultyapp;

public class AccountItem {
    private String appname;
    private String username;
    private String password;
    private String content;

    public AccountItem() {}

    public AccountItem(String appname, String username, String password, String content) {
        this.appname = appname;
        this.username = username;
        this.password = password;
        this.content = content;
    }

    public String getAppname() { return appname; }
    public void setAppname(String appname) { this.appname = appname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}