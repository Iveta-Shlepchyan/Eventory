package com.example.eventory;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.eventory.Logic.Convertor;
import com.example.eventory.Logic.FirebaseManipulations;
import com.example.eventory.Logic.WebScraping;
import com.example.eventory.models.CardModel;
import com.example.eventory.models.SerializableGeoPoint;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;


public class ContainerActivity extends AppCompatActivity {

    BottomNavigationView bottomNavMenu;
    HomeFragment homeFragment = new HomeFragment();
    MapFragment mapFragment = new MapFragment();
    TicketFragment ticketFragment = new TicketFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    public static List<CardModel> likedCards = new ArrayList<>();
    public static HashSet<String> tags_set = new HashSet<String>();
    public static TreeSet<String> locations_set = new TreeSet<String>();
    public static HashSet<SerializableGeoPoint> geo_points = new HashSet<>();
    public static ArrayList<BitmapDescriptor> pins = new ArrayList<>();

//    public static HashSet<CardModel> allEvents = new HashSet<>();

    public static CardModel lastLikedEvent;

    public interface IOnBackPressed {

        boolean onBackPressed();
    }

    @Override public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if ((fragment instanceof IOnBackPressed)) {
            finishAffinity();
            finish();
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
            bottomNavMenu.getMenu().findItem(R.id.home).setChecked(true);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        bottomNavMenu = findViewById(R.id.bottomNavigationView);

        String marker = getIntent().getStringExtra("goToMap");
        if(marker != null){
            Bundle bundle = new Bundle();
            bundle.putString("marker", marker);
            mapFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container,mapFragment).commit();
            bottomNavMenu.getMenu().findItem(R.id.map).setChecked(true);
        }
        else getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String json = preferences.getString("card_models", "");
        if(!json.isEmpty()) {
            Type listType = new TypeToken<List<CardModel>>() {}.getType();
            Gson gson = new Gson();
            likedCards = gson.fromJson(json, listType);
        }

        MapsInitializer.initialize(getApplicationContext());
        pins.addAll(Convertor.map_pins(ContainerActivity.this));

        FirebaseManipulations.startRemovingPassedEvents();


        WebScraping webScraping = new WebScraping(ContainerActivity.this);
        webScraping.startScraping();




        bottomNavMenu.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();
                        return true;
                    case R.id.map:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,mapFragment).commit();
                        return true;
                    case R.id.like:
                        LikeFragment likeFragment = new LikeFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,likeFragment).commit();
                        return true;
                    case R.id.ticket:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,ticketFragment).commit();
                        return true;
                    case R.id.profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,profileFragment).commit();
                        return true;
                }
                return false;
            }
        });



    }

    /*private void getAllEvents(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("AllEvents")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                CardModel cardModel = document.toObject(CardModel.class);
                                if(!ContainerActivity.likedCards.isEmpty()){
                                    for (CardModel likedCard: ContainerActivity.likedCards ) {
                                        if (likedCard.getName().equals(cardModel.getName()))
                                            cardModel.setLiked(true);
                                    }
                                }
                                ContainerActivity.allEvent.add(cardModel);

                                try {
                                    if(!cardModel.getTags().isEmpty())
                                        ContainerActivity.tags_set.addAll(cardModel.getTags());
                                    if(!cardModel.getLocation().isEmpty())
                                        ContainerActivity.locations_set.add(cardModel.getLocation());
                                    if(cardModel.getGeoPoint() != null) {
                                        GeoPoint geoPoint = cardModel.getGeoPoint();
                                        boolean match = false;
                                        for (SerializableGeoPoint gp: ContainerActivity.geo_points) {
                                            if(geoPoint.equals(gp.getGeoPoint())) {
                                                match = true;
                                                break;
                                            }
                                        }
                                        if(!match)ContainerActivity.geo_points.add(new SerializableGeoPoint(geoPoint, cardModel.getLocation(), cardModel.getTags().get(0)));
                                    }
                                }catch (NullPointerException nullEx){
                                    Log.e("HomeFragment", cardModel.getName()+ " "+ nullEx.getMessage());
                                }
                            }
                        }
                        else {
                            Toast.makeText(ContainerActivity.this,"Error"+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }*/




}
