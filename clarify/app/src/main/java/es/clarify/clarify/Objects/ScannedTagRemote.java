package es.clarify.clarify.Objects;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ScannedTagRemote {
    @PrimaryKey
    private int id;
    private Date storageDate;

    private String idFirebase;
    private String brand;
    private String model;
    private Boolean lote;
    private String color;
    private String expiration_date;
    private String reference;
    private String image;
    private String store;

    public ScannedTagRemote() {
    }

    public ScannedTagRemote(ScannedTagLocal scannedTagLocal) {
        this.id = scannedTagLocal.getId();
        this.storageDate = new Date();
        this.idFirebase = scannedTagLocal.getIdFirebase();
        this.brand = scannedTagLocal.getBrand();
        this.model = scannedTagLocal.getModel();
        this.lote = scannedTagLocal.getLote();
        this.color = scannedTagLocal.getColor();
        this.expiration_date = scannedTagLocal.getExpiration_date();
        this.reference = scannedTagLocal.getReference();
        this.image = scannedTagLocal.getImage();
        this.store = scannedTagLocal.getStore();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdFirebase() {
        return idFirebase;
    }

    public void setIdFirebase(String idFirebase) {
        this.idFirebase = idFirebase;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Boolean getLote() {
        return lote;
    }

    public void setLote(Boolean lote) {
        this.lote = lote;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getExpiration_date() {
        return expiration_date;
    }

    public void setExpiration_date(String expiration_date) {
        this.expiration_date = expiration_date;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getStorageDate() {
        return storageDate;
    }

    public void setStorageDate(Date storageDate) {
        this.storageDate = storageDate;
    }

    public String getStore() { return store; }

    public void setStore(String store) { this.store = store; }
}
