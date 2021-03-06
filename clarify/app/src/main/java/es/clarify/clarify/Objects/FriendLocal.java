package es.clarify.clarify.Objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FriendLocal extends RealmObject {

    private String name;
    private String email;
    @PrimaryKey
    private String uid;
    private Boolean status;
    private String photo;
    private String idShoppingCart;

    public FriendLocal() {
    }

    public FriendLocal(String name, String email, String uid, Boolean status, String photo, String idShoppingCart) {
        this.name = name;
        this.email = email;
        this.uid = uid;
        this.status = status;
        this.photo = photo;
        this.idShoppingCart = idShoppingCart;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getIdShoppingCart() {
        return idShoppingCart;
    }

    public void setIdShoppingCart(String idShoppingCart) {
        this.idShoppingCart = idShoppingCart;
    }
}
