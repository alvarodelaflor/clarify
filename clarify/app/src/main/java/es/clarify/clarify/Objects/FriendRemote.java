package es.clarify.clarify.Objects;

import java.util.Objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FriendRemote {

    private String name;
    private String email;
    private String uid;
    private Boolean status;
    private String photo;
    private String idShoppingCart;

    public FriendRemote() {
    }

    public FriendRemote(String name, String email, String uid, Boolean status, String photo, String idShoppingCart) {
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

    @Override
    public String toString() {
        return "FriendRemote{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", uid='" + uid + '\'' +
                ", status=" + status +
                ", photo='" + photo + '\'' +
                ", idShoppingCart='" + idShoppingCart + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendRemote that = (FriendRemote) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(email, that.email) &&
                Objects.equals(uid, that.uid) &&
                Objects.equals(status, that.status) &&
                Objects.equals(photo, that.photo) &&
                Objects.equals(idShoppingCart, that.idShoppingCart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, uid, status, photo, idShoppingCart);
    }
}
