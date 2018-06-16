package altice.jrojas.android__sharing_app.classes;

/**
 * Created by jaime on 6/15/2018.
 */

public class ArticleLocation {
    private double latitude;
    private double longitude;
    private String country;
    private String city;

    public ArticleLocation() {}

    public ArticleLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = "";
        this.city = "";
    }

    public ArticleLocation(String country, String city) {
        this.latitude = 0;
        this.longitude = 0;
        this.country = country;
        this.city = city;
    }

    public ArticleLocation(double latitude, double longitude, String country, String city) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return this.city  + ", " + this.country;
    }

    public String getLatLong() {
        return String.valueOf(this.latitude) + "," + String.valueOf(this.longitude);
    }
}
