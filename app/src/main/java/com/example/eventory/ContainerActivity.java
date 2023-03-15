package com.example.eventory;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventory.Logic.Convertor;
import com.example.eventory.Logic.FirebaseManipulations;
import com.example.eventory.Logic.WebScraping;
import com.example.eventory.models.CardModel;
import com.example.eventory.models.SerializableGeoPoint;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        bottomNavMenu = findViewById(R.id.bottomNavigationView);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
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


        getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();

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


}