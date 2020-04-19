package es.clarify.clarify.Objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserDataLocal extends RealmObject {

    @PrimaryKey
    private Integer id;
    private String name;
    private String email;
    private String photo;
    private String uid;
    private String phoneNumber;

    public UserDataLocal(String name, String email, String photo, String uid) {
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.uid = uid;
    }

    public UserDataLocal() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

}
