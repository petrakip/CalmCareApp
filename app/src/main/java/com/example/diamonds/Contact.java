package com.example.diamonds;

public class Contact {
    private String name;
    private String phone;
    private String imageUri;

    public Contact(String name, String phone, String imageUri) {
        this.name = name;
        this.phone = phone;
        this.imageUri = imageUri;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}