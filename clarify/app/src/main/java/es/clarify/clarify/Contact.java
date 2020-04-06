package es.clarify.clarify;

public class Contact {

    private String name;
    private String phone;
    private int photo;

    public Contact(String name, String phone, int photo) {
        this.name = name;
        this.phone = phone;
        this.photo = photo;
    }

    public Contact() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getPhoto() {
        return this.photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }
}
