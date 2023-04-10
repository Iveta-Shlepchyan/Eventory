package event.app.eventory.models;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;



public class CardModel implements Serializable{

    private String name;
    private String location;
    private String img_url;
    private String description;
    private String duration;
    private String genre;
    private String min_age;
    private ArrayList<Integer> prices;
    private ArrayList<String> tags;
    private ArrayList<String> more_images;
    private ArrayList<Date> dates;
    private SerializableGeoPoint geoPoint;
    private String link;

    private boolean liked = false;


    public CardModel(String name, String location, String img_url, String description, String duration, String genre, String min_age, ArrayList<Integer> prices, ArrayList<String> tags, ArrayList<String> more_images, ArrayList<Date> dates, String link, GeoPoint geoPoint) {
        this.name = name;
        this.location = location;
        this.img_url = img_url;
        this.description = description;
        this.duration = duration;
        this.genre = genre;
        this.min_age = min_age;
        this.prices = prices;
        this.tags = tags;
        this.more_images = more_images;
        this.dates = dates;
        this.link = link;
        if(geoPoint != null)
            this.geoPoint = new SerializableGeoPoint(geoPoint);
    }


    public CardModel(){}


    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint.getGeoPoint();
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = new SerializableGeoPoint(geoPoint);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getMin_age() {
        return min_age;
    }

    public void setMin_age(String min_age) {
        this.min_age = min_age;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ArrayList<Integer> getPrices() {
        return prices;
    }

    public void setPrices(ArrayList<Integer> prices) {
        this.prices = prices;
    }

    public ArrayList<String> getMore_images() {
        return more_images;
    }

    public void setMore_images(ArrayList<String> more_images) {
        this.more_images = more_images;
    }


    public ArrayList<Date> getDates() {
        return dates;
    }

    public void setDates(ArrayList<Date> dates) {
        this.dates = dates;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

}

