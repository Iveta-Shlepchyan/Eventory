package event.app.eventory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import event.app.eventory.Logic.Convertor;
import event.app.eventory.Logic.FirebaseManipulations;

import event.app.eventory.R;

import event.app.eventory.adapters.DateAdapter;
import event.app.eventory.adapters.ImageAdapter;
import event.app.eventory.adapters.TagAdapter;
import event.app.eventory.models.CardModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventPageActivity extends AppCompatActivity {

    ImageView eventImage;
    TextView eventName, eventDescription,eventDateTime, eventDuration, eventGenre,
            eventMinAge, eventPlace, eventPrice;
    AppCompatButton backBtn, likeBtn, shareBtn;
    Button ticketBtn;
    RecyclerView moreImagesRec, dateRec, tagsRec;
    SupportMapFragment map;

    CardModel event;

    Drawable like_pressed;
    Drawable like;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        long startTime = System.currentTimeMillis();

        like_pressed = getResources().getDrawable(R.drawable.ic_heart_card_pressed_b);
        like = getResources().getDrawable(R.drawable.ic_heart_card_black_b);

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

        AppBarLayout appBarLayout = findViewById(R.id.main_appbar);



        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                if (verticalOffset + totalScrollRange == 0) {
                    // Collapsed
                    backBtn.setBackground(null);
                    likeBtn.setBackground(null);
                    shareBtn.setBackground(null);

                } else {
                    // Not collapsed
                    backBtn.setBackgroundResource(R.drawable.bg_filter_btn);
                    likeBtn.setBackgroundResource(R.drawable.bg_filter_btn);
                    shareBtn.setBackgroundResource(R.drawable.bg_filter_btn);
                }
            }
        });


        Uri uri = getIntent().getData();
        if(uri != null){
            String event_name = uri.getPathSegments().get(uri.getPathSegments().size()-2);
            String event_category = uri.getLastPathSegment();

            FirebaseManipulations.getEvent(event_name, event_category, task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        event = document.toObject(CardModel.class);
                        Log.e("Card model", event.getName());
                        setUpEventPage();
                    } else {
                        Log.d("EventPageDynamicLink", "No such document");
                    }
                } else {
                    Log.d("EventPageDynamicLink", "get failed with ", task.getException());
                }
            });
        }else {
            event = (CardModel) getIntent().getSerializableExtra("info");
            setUpEventPage();
        }



        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(event.isLiked()){
                    event.setLiked(false);

                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(like, null, null, null);

                    for (CardModel likedCard: ContainerActivity.likedCards ) {
                        if (likedCard.getName().equals(event.getName())){
                            ContainerActivity.likedCards.remove(likedCard);
                            break;
                        }
                    }
                }
                else {
                    event.setLiked(true);
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(like_pressed, null, null, null);
                    ContainerActivity.likedCards.add(event);
                }
                Convertor.saveLikes(getApplicationContext());
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> tags = event.getTags();
                tags.retainAll(FirebaseManipulations.paths);

                String deepLink = "https://www.example.eventory.com/event/" + event.getName()+"/"+tags.get(0);


                FirebaseDynamicLinks dynamicLinks = FirebaseDynamicLinks.getInstance();
                Task<ShortDynamicLink> dynamicLink = dynamicLinks.createDynamicLink()
                        .setLink(Uri.parse(deepLink))
                        .setDomainUriPrefix("https://eventory.page.link")
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                        .buildShortDynamicLink()
                        .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                            @Override
                            public void onSuccess(ShortDynamicLink shortDynamicLink) {
                                Uri shortLink = shortDynamicLink.getShortLink();
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Event Name");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this event I found:  "+ event.getName() +
                                        ". \nDownload the app and join the event: " + shortLink.toString());
                                startActivity(Intent.createChooser(shareIntent, "Share event"));

                            }
                        });
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

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLikes();
            }
        });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Log.e("Time taken", String.valueOf(duration) + "ms");

    }

    private void setUpEventPage(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM, EEEE HH:mm", Locale.ENGLISH);

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
        if(event.isLiked()) likeBtn.setCompoundDrawablesWithIntrinsicBounds(like_pressed, null, null, null);


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



        if (event.getGeoPoint()==null || event.getGeoPoint().equals(new GeoPoint(0,0))) {
            map.setMenuVisibility(false);
            getSupportFragmentManager().beginTransaction().hide(map).commit();
        }
        else map.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("PotentialBehaviorOverride")
            @Override
            public void onMapReady(GoogleMap googleMap) {

                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                googleMap.getUiSettings().setZoomGesturesEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);


                LatLng marker_position = new LatLng(event.getGeoPoint().getLatitude(), event.getGeoPoint().getLongitude());
                googleMap.addMarker(new MarkerOptions().position(marker_position).icon(Convertor.get_icon(event.getTags())));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker_position, 16));

                try {
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    EventPageActivity.this, R.raw.style2_json));

                    if (!success) {
                        Log.e("LocationMap", "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e("LocationMap", "Can't find style.", e);
                }

                googleMap.setOnMapClickListener(latLng -> {
                    Intent i = new Intent(EventPageActivity.this, ContainerActivity.class);
                    i.putExtra("fragment", event.getLocation());
                    startActivity(i);
                });
                googleMap.setOnMarkerClickListener(marker -> {
                    Intent i = new Intent(EventPageActivity.this, ContainerActivity.class);
                    i.putExtra("fragment", event.getLocation());
                    startActivity(i);
                    return false;
                });


            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateLikes();
    }

    private void updateLikes(){
        ContainerActivity.lastLikedEvent = event;
        if(getIntent().getBooleanExtra("fromHome", false))
            startActivity(new Intent(EventPageActivity.this, ContainerActivity.class));
        else {
            finish();
        }
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
