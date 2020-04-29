package es.clarify.clarify.Objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PurchaseLocal extends RealmObject {

    @PrimaryKey
    private Integer id;
    private Integer idFirebase;
    private int idScannedTag;
    private String idShoppingCart;
    private String name;
    private Boolean check;

    public PurchaseLocal() {
    }

    public PurchaseLocal(int id, int idFirebase, int idScannedTag, String idShoppingCart, String name, Boolean check) {
        this.id = id;
        this.idFirebase = idFirebase;
        this.idScannedTag = idScannedTag;
        this.idShoppingCart = idShoppingCart;
        this.name = name;
        this.check = check;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdFirebase() {
        return idFirebase;
    }

    public void setIdFirebase(int idFirebase) {
        this.idFirebase = idFirebase;
    }

    public int getIdScannedTag() {
        return idScannedTag;
    }

    public void setIdScannedTag(int idScannedTag) {
        this.idScannedTag = idScannedTag;
    }

    public String getIdShoppingCart() {
        return idShoppingCart;
    }

    public void setIdShoppingCart(String idShoppingCart) {
        this.idShoppingCart = idShoppingCart;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdFirebase(Integer idFirebase) {
        this.idFirebase = idFirebase;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }
}
