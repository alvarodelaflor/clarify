package es.clarify.clarify.Objects;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ShoppingCartLocal extends RealmObject {

    @PrimaryKey
    private String id;
    private Date lastUpdate;
    private Boolean own;
    private RealmList<PurchaseLocal> purcharse;
    private RealmList<String> allowUsers;

    public ShoppingCartLocal() {
    }

    public ShoppingCartLocal(String id, Date lastUpdate, Boolean own, RealmList<PurchaseLocal> purchase, RealmList<String> allowUsers) {
        this.id = id;
        this.lastUpdate = lastUpdate;
        this.own = own;
        this.purcharse = purchase;
        this.allowUsers = allowUsers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RealmList<PurchaseLocal> getPurcharse() {
        return purcharse;
    }

    public void setPurcharse(RealmList<PurchaseLocal> purcharse) {
        this.purcharse = purcharse;
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
