package com.example.eventory.Logic;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventory.ContainerActivity;
import com.example.eventory.models.CardModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebScraping {

    private Document doc;
    private Thread secThread;
    private Runnable runnable;
    private Context context;

    public WebScraping(Context context) {
        this.context = context;
    }

    public void startScraping() {
        runnable = new Runnable() {
            @Override
            public void run() {
                getWeb();
            }
        };
        secThread = new Thread(runnable);
        secThread.start();
    }

    private void getWeb() {

        try {
            doc = Jsoup.connect("https://tomsarkgh.am/en").get();
            Elements hrefs = doc.getElementsByAttributeValueContaining("href", "event/");
            LinkedHashSet<String> links = new LinkedHashSet<String>();
            for (Element href : hrefs) {
                links.add(href.absUrl("href"));
            }
            for (String link : links) {

                CardModel event = getEventInfo(link);
                if(eventIsValid(event)) FirebaseManipulations.addToFirebase(event);

            }
        } catch (IOException e) {
            Log.e("Jsoup/getWeb", "Connection problem: " + e.getMessage());
        }
    }

    private CardModel getEventInfo(String link) {

        Document event = null;
        try {
            event = Jsoup.connect(link).get();
        } catch (IOException e) {
            Log.e("Jsoup", "Connection problem");
        }
        CardModel eventInfo = new CardModel();
        try {
            eventInfo.setLink(link);
            eventInfo.setName(event.getElementById("eventName").text().replaceAll("/", " "));
            eventInfo.setDescription(event.getElementById("eventDesc").text());
            eventInfo.setImg_url(event.getElementById("single_default").absUrl("href"));
            eventInfo.setLocation(event.getElementsByClass("occurrence_venue").get(0).text());
            eventInfo.setMore_images(new ArrayList<String>());
            eventInfo.setTags(new ArrayList<String>());
            eventInfo.setDates(new ArrayList<Date>());
            eventInfo.setPrices(new ArrayList<Integer>());
            eventInfo.setGeoPoint(getLocationFromAddress(eventInfo.getLocation(), context));

            Elements tags_el = event.getElementsByClass("event_type alpha60");
            Elements images = event.getElementsByAttributeValueContaining("href", "/thumbnails/Photo/bigimage/");


            eventInfo.getPrices().addAll(extractPrice(eventInfo.getDescription()));
            //if there is more than one matching tag (ex : "for kids", "free", "comedy")
            //(unfortunately tomsarkgh provide only one tag)
            /*HashSet<String> tags = new HashSet<String>();
              for (Element tag : tags_el) {
                   tags.add(tag.text());
              }*/


            String tag = tags_el.first().text();
            eventInfo.getTags().add(tag);
            if(new ArrayList<>(Arrays.asList("Jazz", "Classical", "Rock")).contains(tag))
                eventInfo.getTags().add(0,"Concert");
            else if(eventInfo.getTags().size()==1 &&
                    !new ArrayList<>(Arrays.asList("Theater","Clubs & pubs","Cinema","Concert","Other")).contains(tag))
                eventInfo.getTags().add(0, "Other");


            for (int i = 1; i < images.size(); i++) {
                eventInfo.getMore_images().add(images.get(i).absUrl("href"));
            }

            HashSet<String> date_times = new HashSet<String>();

            Elements time = event.getElementsByClass("oc_time");
            if (time.size() == 1) {
                String day = event.getElementsByClass("oc_date").text();
                String month = event.getElementsByClass("oc_month").text();
                String weekday = event.getElementsByClass("oc_weekday").text();
                String full_date = day + " " + month + ", " + weekday + "  " + time.text();
                date_times.add(full_date);
            } else {
                Elements full_date = event.getElementsByClass("oc_fulldate");
                time.remove(0);
                for (int i = 0; i < time.size(); i++) {
                    date_times.add(full_date.get(i).text() + "  " + time.get(i).text());
                }
            }

            TreeSet<Date> dateTreeSet = new TreeSet<>();
            for (String dateString: date_times) {
                dateTreeSet.add(stringToDate(dateString));
            }
            eventInfo.getDates().addAll(dateTreeSet);


        }catch (Exception e){
            Log.e("Jsoup/getEventInfo", link+ "  " + e.getMessage());
        }

        return eventInfo;
    }








    private TreeSet<Integer> extractPrice(String description){
        description = description.replaceAll("\\." , "");
        description = description.replaceAll("\\s" , "");

        TreeSet<Integer> prices = new TreeSet<>();
        String regex = "\\d{4}?\\d{0,2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(description);
        while (matcher.find()) {
            int price = Integer.parseInt(matcher.group().replaceAll("[\\.,\\s]", ""));
            if (price % 100 == 0) {
                prices.add(price);
            }
        }
        return prices;
    }

    private Date stringToDate(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("dd MMM, EEE HH:mm", Locale.ENGLISH);
        Date parsedDate = null;

        try {
            parsedDate = format.parse(dateString);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            parsedDate = calendar.getTime();

        } catch (ParseException e) {
            Log.e("stringToDate", "format problem " + e.getMessage() +" " + dateString);
        }
        return parsedDate;
    }

    private boolean eventIsValid(CardModel event){
        boolean valid = true;
        if(event.getName() == null || event.getName().isEmpty()) valid = false;
        if(event.getDates() == null || event.getDates().isEmpty()) valid = false;
        if(event.getTags() == null || event.getTags().isEmpty()) valid = false;
        if(event.getPrices() == null) valid = false;

        return valid;
    }


    private GeoPoint getLocationFromAddress(String strAddress, Context context) {

        Geocoder coder = new Geocoder(context);
        GeoPoint gp = null;
        try {
            List<Address> address = coder.getFromLocationName(strAddress, 1);
            if (!address.isEmpty()) {
                Address location = address.get(0);
                gp = new GeoPoint(location.getLatitude(), location.getLongitude());
            } else {
                Log.e("WS/Geocoder", "Location not found: " + strAddress);
            }
        } catch (IOException ex) {
            Log.e("WS/Geocoder", "getLocationFromAddress failed "+ ex.getMessage());
        }
        return gp;
    }


}



