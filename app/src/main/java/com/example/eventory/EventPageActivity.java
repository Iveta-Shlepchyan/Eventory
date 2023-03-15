package com.example.eventory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventory.adapters.DateAdapter;
import com.example.eventory.adapters.ImageAdapter;
import com.example.eventory.adapters.TagAdapter;
import com.example.eventory.models.CardModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventPageActivity extends AppCompatActivity {

    ImageView eventImage;
    TextView eventName, eventDescription,eventDateTime, eventDuration, eventGenre,
            eventMinAge, eventPlace, eventPrice;
    ImageButton backBtn, likeBtn, shareBtn;
    Button ticketBtn;
    RecyclerView moreImagesRec, dateRec, tagsRec;
    SupportMapFragment map;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        long startTime = System.currentTimeMillis();

        eventImage =findViewById(R.id.eventImage);
        eventName = findViewById(R.id.eventName);
        eventDescription = findViewById(R.id.eventDescription);
        eventDateTime = findViewById(R.id.eventDateTime);
        eventDuration = findViewById(R.id.eventDuration);
        eventGenre = findViewById(R.id.eventGenre);
        eventMinAge = findViewById(R.id.eventMinAge);
        eventPlace = findViewById(R.id.eventPlace);
        eventPrice = findViewById(R.id.eventMinPrice);

        backBtn = findViewById(R.id.backBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        ticketBtn = findViewById(R.id.ticketBtn);

        moreImagesRec = findViewById(R.id.more_images_recycler);
        dateRec = findViewById(R.id.date_time_list);
        tagsRec = findViewById(R.id.tags_recycler);


        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapLocation);

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM, EEEE HH:mm", Locale.ENGLISH);


        Intent i = getIntent();
        CardModel event = (CardModel) getIntent().getSerializableExtra("info");

        Glide.with(this).load(event.getImg_url()).into(eventImage);
        eventName.setText(event.getName());

        if(event.getDates()!=null && !event.getDates().isEmpty()) {
            String date = dateFormat.format(event.getDates().get(0));
            setTextAndVisibility(eventDateTime, date);
        }

        setTextAndVisibility(eventDescription, event.getDescription());
        setTextAndVisibility(eventDuration, event.getDuration());
        setTextAndVisibility(eventGenre, event.getGenre());
        setTextAndVisibility(eventMinAge, event.getMin_age());
        setTextAndVisibility(eventPrice, priceToString(event.getPrices()));
        setTextAndVisibility(eventPlace, event.getLocation());
        if(event.isLiked()) likeBtn.setBackgroundResource(R.drawable.ic_heart_card_pressed);


        tagsRec.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        TagAdapter tagAdapter = new TagAdapter(this, event.getTags(), false);
        tagsRec.setAdapter(tagAdapter);
        tagsRec.setHasFixedSize(true);


        if (event.getMore_images().isEmpty()) moreImagesRec.setVisibility(View.GONE);
        else {
            moreImagesRec.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            ImageAdapter imageAdapter = new ImageAdapter(this, event.getMore_images());
            moreImagesRec.setAdapter(imageAdapter);
            moreImagesRec.setHasFixedSize(true);
        }


        if(event.getDates().isEmpty()) dateRec.setVisibility(View.GONE);
        else {
            dateRec.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            DateAdapter dateAdapter = new DateAdapter(this, event.getDates());
            dateRec.setAdapter(dateAdapter);
            dateRec.setHasFixedSize(true);

            dateAdapter.setOnItemClickListener(new DateAdapter.onItemClickListener() {
                @Override
                public void onClick(int position) {
                    String date = dateFormat.format(event.getDates().get(position));
                    eventDateTime.setText(date);

                }
            });
        }

        eventDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent calendarIntent = new Intent(Intent.ACTION_INSERT);
                try {
                    calendarIntent.setType("vnd.android.cursor.item/event");
                    calendarIntent.setData(Uri.parse("content://com.android.calendar/events"));

                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, EEE HH:mm yyyy", Locale.ENGLISH);
                    int year = cal.get(Calendar.YEAR);
                    Date date = null;
                    try {
                        date = sdf.parse(eventDateTime.getText().toString() +" "+year);
                    } catch (ParseException e) {
                        Log.e("Parse", ""+ eventDateTime.getText().toString() +" "+year);
                    }
                    cal.setTime(date);

                    calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
                    calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis()+event.getDuration());
                    calendarIntent.putExtra(CalendarContract.Events.TITLE, event.getName());
                    calendarIntent.putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription());
                    calendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocation());
                    calendarIntent.putExtra(CalendarContract.Events.DURATION, event.getDuration());
                    startActivity(Intent.createChooser(calendarIntent, "Add to calendar"));
                } catch (Exception e) {
                    Log.e("AddToCalendar", "Missing content");
                }
            }
        });



        if (event.getLocation()==null) map.setMenuVisibility(false);
        else map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                googleMap.getUiSettings().setZoomGesturesEnabled(false);



                LatLng marker_position = getLocationFromAddress(event.getLocation());
                googleMap.addMarker(new MarkerOptions().position(marker_position));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker_position, 16));

                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    EventPageActivity.this, R.raw.style_json));

                    if (!success) {
                        Log.e("LocationMap", "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e("LocationMap", "Can't find style.", e);
                }

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {

                    }
                });


            }
        });



        //FIXME toolbar fix (if null remove), like fix
        //FIXME map fragment fix, filter or view all, (maybe image swipe to dismiss & swipe to other image)

        //TODO DONE -- select click fix, parse fix, image zoom, map add, tags rec



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(event.isLiked()){
                    likeBtn.setBackgroundResource(R.drawable.selector_button);
                    for (CardModel likedCard: ContainerActivity.likedCards ) {
                        if (likedCard.getName().equals(event.getName())){
                            ContainerActivity.likedCards.remove(likedCard);
                            break;
                        }
                    }
                }
                else {
                    likeBtn.setBackgroundResource(R.drawable.ic_heart_card_pressed);
                    event.setLiked(true);
                    ContainerActivity.likedCards.add(event);
                }

                Gson gson = new Gson();
                String json = gson.toJson(ContainerActivity.likedCards);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EventPageActivity.this);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("card_models", json);
                editor.commit();


            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO add dynamic link + shareInent
                /*String link = "https://example.eventory.link/event?id=" + event.getName();
                DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(link))
                        .setDynamicLinkDomain("example.eventory.page.link")
                        .buildDynamicLink();*/

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Event Name");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this event I found: Event Name - Event Description - Event Location - Event Date and Time." +
                        " Download the app and join the event :  https://app_link ");
                startActivity(Intent.createChooser(shareIntent, "Share event"));
                }

        });

        ticketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!event.getLink().isEmpty()) {
                    Uri uriUrl = Uri.parse(event.getLink());
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    launchBrowser.addCategory(Intent.CATEGORY_BROWSABLE);
                    startActivity(launchBrowser);
                }
            }
        });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Log.e("Time taken", String.valueOf(duration) + "ms");

    }

    public boolean isServicesOK(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(EventPageActivity.this);
        if(available == ConnectionResult.SUCCESS){
            Log.e("isServicesOK : ", "Google play services is working.");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d("isServicesOK : ", "The error occurred but it is fixable.");
        }
        else Log.d("isServicesOK : ", "shit");
        return false;
    }

    public LatLng getLocationFromAddress( String strAddress) {

        Geocoder coder = new Geocoder(this);
//        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault(), new FileBasedAddressCache(getContext()));
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            Log.e("Geocoder", "getLocationFromAddress failed \n Address : "+ strAddress);
            return new LatLng(40.177200, 44.503490);
        }

        return p1;
    }

    private void setTextAndVisibility(TextView textView, String value) {
        if (value == null) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(textView.getText() + value);
        }
    }

    private String priceToString(ArrayList<Integer> prices){
        switch (prices.size()){
            case 0: return null;
            case 1: return prices.get(0) + "";
            case 2: return prices.get(0) + " - " + prices.get(1);

            default:
                String priceStr = prices.get(0)+"";
                for (int i = 1; i < prices.size(); i++)
                    priceStr +=", "+ prices.get(i);

                return priceStr;
        }
    }


}
