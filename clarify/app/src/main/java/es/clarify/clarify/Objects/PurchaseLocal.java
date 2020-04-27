package es.clarify.clarify.Objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PurchaseLocal extends RealmObject {

    @PrimaryKey
    private int id;
    private int idFirebase;
    private int idScannedTag;
    private String idShoppingCart;
    private String name;

    public PurchaseLocal() {
    }

    public PurchaseLocal(int id, int idFirebase, int idScannedTag, String idShoppingCart, String name) {
        this.id = id;
        this.idFirebase = idFirebase;
        this.idScannedTag = idScannedTag;
        this.idShoppingCart = idShoppingCart;
        this.name = name;
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
}
