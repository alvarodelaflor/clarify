package es.clarify.clarify.Objects;

import java.util.Date;
import java.util.List;

public class ShoppingCartRemote {

    private String idFirebase;
    private Date lastUpdate;
    private Boolean own;
    private List<PurchaseRemote> purcharse;
    private List<FriendRemote> allowUsers;

    public ShoppingCartRemote() {
    }

    public ShoppingCartRemote(String idFirebase, Date lastUpdate, Boolean own, List<PurchaseRemote> purcharse, List<FriendRemote> allowUsers) {
        this.idFirebase = idFirebase;
        this.lastUpdate = lastUpdate;
        this.own = own;
        this.purcharse = purcharse;
        this.allowUsers = allowUsers;
    }

    public String getIdFirebase() {
        return idFirebase;
    }

    public void setIdFirebase(String idFirebase) {
        this.idFirebase = idFirebase;
    }

    public List<PurchaseRemote> getPurcharse() {
        return purcharse;
    }

    public void setPurcharse(List<PurchaseRemote> purcharse) {
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

    public List<FriendRemote> getAllowUsers() {
        return allowUsers;
    }

    public void setAllowUsers(List<FriendRemote> allowUsers) {
        this.allowUsers = allowUsers;
    }
}
