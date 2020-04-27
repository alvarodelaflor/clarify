package es.clarify.clarify.Objects;

public class PurchaseRemote {

    private int idFirebase;
    private int idScannedTag;
    private String idShoppingCart;
    private String name;

    public PurchaseRemote() {
    }

    public PurchaseRemote(int idFirebase, int idScannedTag, String idShoppingCart, String name) {

        this.idFirebase = idFirebase;
        this.idScannedTag = idScannedTag;
        this.idShoppingCart = idShoppingCart;
        this.name = name;
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
