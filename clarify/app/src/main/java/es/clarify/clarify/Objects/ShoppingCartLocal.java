package es.clarify.clarify.Objects;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ShoppingCart extends RealmObject {

    @PrimaryKey
    private int id;
    private int idFirebase;
    private Date lastUpdate;
    private Boolean own;
    private RealmList<PurchaseLocal> purcharse;
    private RealmList<String> allowUsers;

    public ShoppingCart() {
    }

    public ShoppingCart(int id, int idFirebase, Date lastUpdate, Boolean own, RealmList<PurchaseLocal> purchase, RealmList<String> allowUsers) {
        this.id = id;
        this.idFirebase = idFirebase;
        this.lastUpdate = lastUpdate;
        this.own = own;
        this.purcharse = purchase;
        this.allowUsers = allowUsers;
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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Boolean getOwn() {
        return own;
    }

    public void setOwn(Boolean own) {
        this.own = own;
    }

    public RealmList<String> getAllowUsers() {
        return allowUsers;
    }

    public void setAllowUsers(RealmList<String> allowUsers) {
        this.allowUsers = allowUsers;
    }
}
