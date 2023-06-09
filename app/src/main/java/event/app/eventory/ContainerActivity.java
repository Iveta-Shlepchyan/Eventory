package event.app.eventory;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import event.app.eventory.Logic.Convertor;
import event.app.eventory.Logic.FirebaseManipulations;
import event.app.eventory.Logic.WebScraping;

import event.app.eventory.R;

import event.app.eventory.models.CardModel;
import event.app.eventory.models.SerializableGeoPoint;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
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

        String fragment = getIntent().getStringExtra("fragment");

        if(fragment != null && fragment.equals("profile")){
            getSupportFragmentManager().beginTransaction().replace(R.id.container,profileFragment).commit();
            bottomNavMenu.getMenu().findItem(R.id.profile).setChecked(true);
        }
        else if(fragment != null){
            Bundle bundle = new Bundle();
            bundle.putString("marker", fragment);
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
                    /*case R.id.ticket:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,ticketFragment).commit();
                        return true;*/
                    case R.id.profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,profileFragment).commit();
                        return true;
                }
                return false;
            }
        });



    }
}
