package es.clarify.clarify.Objects;


public class ScannedTag {
    String id;
    String brand;
    String model;
    Boolean lote;
    String color;
    String expiration_date;
    String reference;
    String image;

    public ScannedTag() {
    }

    public ScannedTag(String id, String brand, String model, Boolean lote, String color, String expiration_date, String reference, String image) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.lote = lote;
        this.color = color;
        this.expiration_date = expiration_date;
        this.reference = reference;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Boolean getLote() {
        return lote;
    }

    public void setLote(Boolean lote) {
        this.lote = lote;
    }
}
