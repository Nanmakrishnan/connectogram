package com.example.connectogram.notifications;

public class Data   {
    private  String user,notificationType;
    private String body;


    private  Integer icon;



    private String title;
    private String send;
    public Data() {
    }

    public Data(String user, String notificationType, String body, Integer icon, String title, String send) {
        this.user = user;
        this.notificationType = notificationType;
        this.body = body;
        this.icon = icon;
        this.title = title;
        this.send = send;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSend() {
        return send;
    }

    public void setSend(String send) {
        this.send = send;
    }
}
