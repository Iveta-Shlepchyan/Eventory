package com.example.eventory;

import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventory.Logic.Convertor;
import com.example.eventory.adapters.DateTimeAdapter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.LocalDate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SetLocationDialog extends BottomSheetDialogFragment {

    private GoogleMap googleMap;
    private AutocompleteSupportFragment autocompleteFragment;
    BottomSheetDialog dialog;
    private Context context;
    private SetLocationPressedListener listener;
    private String address;
    private LatLng location;


    public SetLocationDialog(Context context){
        this.context = context;
    }

    public SetLocationDialog(CreateEventActivity context, LatLng latLng) {
        this.context = context;
        this.location = latLng;
    }


    public interface SetLocationPressedListener {
        void onSetLocationPressed(String address, LatLng latLng);
    }

    public void setLocationPressedListener(SetLocationPressedListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.dialog_set_location, null);
        dialog.setContentView(view);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BottomSheetBehavior bottomSheetBehavior =  BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setDraggable(false);
        bottomSheetBehavior.setPeekHeight(1600);


        return dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_set_location, container, false);
        Button setLocationBtn = view.findViewById(R.id.setLocationBtn);


        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) {
            Places.initialize(context.getApplicationContext(), "AIzaSyD2_rxEA-LlkdnZc4KEXDhi_PI4rdRd3C0");
        }
        PlacesClient placesClient = Places.createClient(context);

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        autocompleteFragment = new AutocompleteSupportFragment();
        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();


        setUpSearch(autocompleteFragment);
        setUpMap(supportMapFragment);


        ft.add(R.id.containerSearch, autocompleteFragment);
        ft.add(R.id.containerMap, supportMapFragment);
        ft.commit();

        setLocationBtn.setOnClickListener(v -> {

            if (listener != null && address != null) {
                listener.onSetLocationPressed(address, location);
            }
            dismiss();
        });


        return view;
    }

    private void setUpSearch(AutocompleteSupportFragment autocompleteFragment) {

        autocompleteFragment.setCountry("AM");
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                address = place.getName();
                location = place.getLatLng();
                if(place.getName() == null) address = place.getAddress();
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(place.getLatLng()).icon(Convertor.get_icon("User Event")));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));

            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void setUpMap(SupportMapFragment supportMapFragment) {
        supportMapFragment.getMapAsync(googleMap -> {

            this.googleMap = googleMap;
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                            context, R.raw.style2_all_markers_json));

            googleMap.setPadding(0,0, 0, 300);
            UiSettings UiSettings = googleMap.getUiSettings();
            UiSettings.setZoomControlsEnabled(true);
            UiSettings.setMapToolbarEnabled(false);


            if(location != null){
                googleMap.addMarker(new MarkerOptions().position(location).icon(Convertor.get_icon("User Event")));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            }
            else {
                LatLng yerevan = new LatLng(40.177200, 44.503490);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yerevan, 10));
            }


            googleMap.setOnMapClickListener(latLng -> {
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(latLng).icon(Convertor.get_icon("User Event")));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                new Thread(() -> {
                    location = latLng;
                    address = getAddressFromLatLng(latLng);
                    requireActivity().runOnUiThread(() -> autocompleteFragment.setText(address));
                }).start();
            });


        });
    }


    private String getAddressFromLatLng(LatLng latLng){

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String address = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address returnedAddress = addresses.get(0);
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    sb.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                address = sb.toString().trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] parts = address.split(",");

        return parts[0].trim();
    }

}


