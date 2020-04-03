package es.clarify.clarify.Objects;

public class UserData {

    private String name;
    private String email;
    private String photo;
    private String uid;
    private String phoneNumber;

    public UserData(String name, String email, String photo, String uid, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.uid = uid;
        this.phoneNumber = phoneNumber;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
