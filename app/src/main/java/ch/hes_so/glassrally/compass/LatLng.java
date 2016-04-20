package ch.hes_so.glassrally.compass;

public class LatLng {

    private double latitude;
    private double longitude;

    public LatLng()
    {
        this.latitude = 0;
        this.longitude = 0;
    }

    public LatLng(double lat, double lng)
    {
        this.latitude = lat;
        this.longitude = lng;
    }

    public LatLng(LatLng other)
    {
        this.latitude = other.latitude;
        this.longitude = other.longitude;
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
}


