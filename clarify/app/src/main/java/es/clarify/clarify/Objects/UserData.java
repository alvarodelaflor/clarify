package es.clarify.clarify.Objects;

public class UserData {

    private String name;
    private String email;
    private String photo;
    private String uid;
    private String phoneNumber;
    private String token;

    public UserData(String name, String email, String photo, String uid, String phoneNumber, String token) {
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.uid = uid;
        this.phoneNumber = phoneNumber;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
