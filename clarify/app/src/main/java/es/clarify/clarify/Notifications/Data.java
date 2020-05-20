package es.clarify.clarify.Notifications;

import java.util.List;

public class Data {
    private String user;
    private int icon;
    private String body;
    private String title;
    private String sented;
    private String photo;
    private String putExtra;

    public Data() {
    }

    public Data(String user, int icon, String body, String title, String sented, String putExtra, String photo) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sented = sented;
        this.photo = photo;
        this.putExtra = putExtra;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSented() {
        return sented;
    }

    public void setSented(String sented) {
        this.sented = sented;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPutExtra() {
        return putExtra;
    }

    public void setPutExtra(String putExtra) {
        this.putExtra = putExtra;
    }
}
