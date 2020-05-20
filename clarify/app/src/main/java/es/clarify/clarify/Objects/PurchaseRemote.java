package es.clarify.clarify.Objects;

import java.util.List;

public class PurchaseRemote {

    private Integer idFirebase;
    private int idScannedTag;
    private String idShoppingCart;
    private String name;
    private Boolean check;
    private FriendRemote lastUpdate;

    public PurchaseRemote() {
    }

    public PurchaseRemote(int idFirebase, int idScannedTag, String idShoppingCart, String name, Boolean check, FriendRemote lastUpdate) {

        this.idFirebase = idFirebase;
        this.idScannedTag = idScannedTag;
        this.idShoppingCart = idShoppingCart;
        this.name = name;
        this.check = check;
        this.lastUpdate = lastUpdate;
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

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public FriendRemote getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(FriendRemote lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}

