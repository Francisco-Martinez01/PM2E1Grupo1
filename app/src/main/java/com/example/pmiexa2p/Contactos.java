package com.example.pmiexa2p;

public class Contactos {
    private String name;
    private String phone;
    private String latitude;
    private String longitude;
    private String image;
    private int id;


    public Contactos(int id, String name, String phone, String latitude, String longitude, String image) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }
    public String getImage() {
        return image;
    }
    public int getId() {
        return id;
    }
}
