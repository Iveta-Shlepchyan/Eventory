package com.example.eventory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventory.Logic.Convertor;
import com.example.eventory.Logic.Filter;
import com.example.eventory.adapters.ViewAllAdapter;
import com.example.eventory.models.CardModel;
import com.example.eventory.models.SerializableGeoPoint;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MapFragment extends Fragment {

    SupportMapFragment supportMapFragment;
    public static GoogleMap googleMap;
    private static HashSet<Marker> markers = new HashSet<>();
    private RecyclerView recyclerView;

    LatLng currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private boolean locationPermissionGranted;

    private Marker selectedMarker;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);


        askForLocationPermission();
        getCurrentLocation();

        //FIXME
        getAllEvents();

        ImageView directionBtn = view.findViewById(R.id.direction_btn);
        recyclerView = view.findViewById(R.id.dialogRec);
        setUpRecyclerView(recyclerView);
        setupSearchView(view);


        directionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedMarker != null) {
                    LatLng destination = selectedMarker.getPosition();
                    showDirections(destination);
                }
            }
        });

        /*directionBtn.setOnClickListener(v -> {
            if (selectedMarker != null) {
                LatLng origin = currentLocation;
                LatLng destination = selectedMarker.getPosition();

                String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + origin.latitude + "," + origin.longitude +
                        "&destination=" + destination.latitude + "," + destination.longitude +
                        "&key=" + getString(R.string.google_maps_key);

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                        response -> {
                            List<LatLng> routePoints = getRoutePoints(response);
                            int distance = getDistance(response);
                            int duration = getDuration(response);

                            drawRoute(routePoints);
                            showRouteInfo(distance, duration);
                        },
                        error -> {
                            Toast.makeText(getActivity(), "Error getting directions", Toast.LENGTH_SHORT).show();
                        });

                queue.add(request);
            } else {
                Toast.makeText(getActivity(), "Please select a marker", Toast.LENGTH_SHORT).show();
            }
        });*/




        supportMapFragment.getMapAsync(new OnMapReadyCallback() {

            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {

                MapFragment.googleMap = googleMap;
                UiSettings UiSettings = googleMap.getUiSettings();
                addMarkersToMap();
                setMapStyle();

                if (locationPermissionGranted) {
                    googleMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                }
                UiSettings.setMyLocationButtonEnabled(true);



                if(locationPermissionGranted){
                    getCurrentLocation();
                    if(currentLocation!=null) {
                        CircleOptions circleOptions = new CircleOptions()
                                .center(currentLocation)
                                .radius(1000) // in meters
                                .strokeWidth(2)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.parseColor("#500000FF")); // 50% transparent blue
                        Circle circle = googleMap.addCircle(circleOptions);
                    }
                }
                else {
                    LatLng yerevan = new LatLng(40.177200, 44.503490);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(yerevan));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yerevan, 10));
                }



                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {

                        ViewAllAdapter dialogAdapter = new ViewAllAdapter(getContext(), Filter.location(marker.getTitle()), false);
                        recyclerView.setAdapter(dialogAdapter);
                        recyclerView.setVisibility(View.VISIBLE);
                        selectedMarker = marker;

                        return false;
                    }
                });

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        if(recyclerView.getVisibility()==View.VISIBLE)
                            recyclerView.setVisibility(View.GONE);
                        if (selectedMarker != null) {
                            selectedMarker.hideInfoWindow();
                            selectedMarker = null;
                        }
                    }
                });


                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(@NonNull Marker marker) {
                        Intent i = new Intent(getContext(), ViewAllActivity.class);
                        i.putExtra("location", marker.getTitle());
                        startActivity(i);

                    }
                });

                googleMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(@NonNull Marker marker) {
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(marker.getTitle()));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                });

            }
        });
        return view;
    }

    private void askForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
        else {
            locationPermissionGranted = true;
            getCurrentLocation();
        }
    }


    private void setMapStyle(){
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                           requireContext(), R.raw.style2_json));

            if (!success) {
                Log.e("LocationMap", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("LocationMap", "Can't find style.", e);
        }
    }

    private void addMarkersToMap() {
        if(!ContainerActivity.geo_points.isEmpty()){
            for (SerializableGeoPoint geoPoint : ContainerActivity.geo_points){
                LatLng latLng = new LatLng(geoPoint.getGeoPoint().getLatitude(), geoPoint.getGeoPoint().getLongitude());
                Marker new_marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(geoPoint.getAddress())
                        .icon(Convertor.get_icon(geoPoint.getCategory())));
                boolean match = false;
                for(Marker marker : markers){
                    if(marker.getTitle().equals(geoPoint.getAddress())){
                        match = true;
                        break;
                    }
                }
                if(!match) markers.add(new_marker);
            }
        }
    }


    public void getAllEvents(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String json = preferences.getString("Tomsarkgh", "");
        if (!json.isEmpty()) {
            Type listType = new TypeToken<List<CardModel>>() {
            }.getType();
            Gson gson = new Gson();
            ViewAllActivity.allEvents = gson.fromJson(json, listType);
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    currentLocation = latLng;
                    if(googleMap!=null)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                } else {
                    Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                googleMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public interface DirectionsApiService {

        @GET("directions/json")
        Call<DirectionsResult> getDirections(
                @Query("origin") String origin,
                @Query("destination") String destination,
                @Query("mode") String mode,
                @Query("key") String key
        );
    }
    String BASE_URL = "https://maps.googleapis.com/maps/api/";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    DirectionsApiService apiService = retrofit.create(DirectionsApiService.class);

    private void showDirections(LatLng destination) {
        if (locationPermissionGranted) {
            LatLng origin = currentLocation;

            origin = new LatLng(40.1876719,44.5669348);
            destination = new LatLng(40.179111,44.4882828);
            String originString = origin.latitude + "," + origin.longitude;
            String destinationString = destination.latitude + "," + destination.longitude;
            String mode = "driving";
            String key = getString(R.string.google_maps_key);

            Call<DirectionsResult> call = apiService.getDirections(originString, destinationString, mode, key);

            call.enqueue(new Callback<DirectionsResult>() {
                @Override
                public void onResponse(Call<DirectionsResult> call, Response<DirectionsResult> response) {
                    if (response.isSuccessful()) {

                        DirectionsResult result = response.body();
                        Log.e("API response", ""+ response.body().toString() +" routes"+ result.routes);
                        Log.e("API response check", ""+(result.routes != null && result.routes.length > 0));
                        if (result.routes != null && result.routes.length > 0) {
                            DirectionsRoute route = result.routes[0];

//                            String encodedPolyline = route.overviewPolyline.getEncodedPath();
                            //FIXME not getting OVERVIEW POLYLINE properly
                            String encodedPolyline = "}chtF_o_oG`@@PTL^E@GFIFKPGPEZA\\@XFTHPLHPHNNHNFZBLQn@Wh@e@nAy@y@mA}AQ[c@wAGu@J}AVuA^wAJGf@g@NY?]Ma@MIKEK@q@RyAd@aAb@uCfAoB`@]GSKiCqBkB{AeA{@q@c@gAi@m@SgB]w@Ic@?wBNYRi@LiBr@iP`H_JzDsFfCkHzCsIpDuGtCoF|BoCbBeAp@{BvAq@`AYl@Kj@AP@d@F\\Vr@`@b@RLXDdA?jBFrALrAz@fAv@~BrBxHpHtKnKzEtEdBnBhEnFxDzEtDtEnAbBlAfBp@dAtDfGjAzBf@pA\\~@l@rBh@|CR|APnBFxBG~DCz@SdC{AdN]tD?zALrAb@dBZ|@v@jAvE~FbAnAb@n@P`@Vv@VfAr@bFd@bBp@vAX`@ZZZVbAdAj@ThB`BbDvCz@t@dA~@vB|BhDlDp@p@d@^f@TZDd@Cd@Oj@_@fAu@j@O\\CbCZfAZnCvAhB`Af@n@\\x@L`@\\|@`@f@NLVNr@R|@HzADbAFt@NlAPdATDHHLt@VdAV`@Np@Xh@\\fAb@|BbA|@b@tBtA|@p@lBdBdCpCnAjBlA|B\\~@XfAb@xBt@~FTbCZrFH`DJ~@FV\\pAlBlF~BnHrBpGF`AAXER]h@iA`AeIdGgDhCkBxA^bAnC|JBpG?zCCRWx@[h@gB`CyCrEa@f@MPIAOEMCc@Kg@A]HcCrA_Aj@y@n@QR_@p@Ur@yApFQr@iAhEWvAEpABj@Jl@Z|@F^Nb@Ll@HtAAJSXM^Up@gB~DiC|F_DtHpFtEHBFAJUBETRrBaFFOWU";


                            List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);



                            Log.e("WTF2", ""+ response.body() +" routes"+ result.routes);


                            PolylineOptions polylineOptions = new PolylineOptions();
                            polylineOptions.color(Color.BLUE);
                            polylineOptions.width(10);
                            polylineOptions.addAll(decodedPath);
                            googleMap.addPolyline(polylineOptions);


                            long distance = 0;
                            long duration = 0;
                            for (DirectionsLeg leg : route.legs) {
                                distance += leg.distance.inMeters;
                                duration += leg.duration.inSeconds;
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Route Information");
                            builder.setMessage(String.format("Distance: %d meters\nTime: %d seconds", distance, duration));
                            builder.setPositiveButton("OK", null);
                            builder.show();
                        } else {
                            Log.e("MapFragment", "Failed to retrieve directions");
                            Toast.makeText(getActivity(), "Failed to retrieve directions", Toast.LENGTH_SHORT).show();
                        }
                    }

                }


                @Override
                public void onFailure(Call<DirectionsResult> call, Throwable t) {
                    Log.e("Failed to retrieve directions", t.getMessage());
                }
            });
        }
    }


    /*private void showDirections(LatLng destination) {
        if(locationPermissionGranted){
            LatLng origin = currentLocation;

            LatLngBounds bounds = LatLngBounds.builder()
                    .include(origin)
                    .include(destination)
                    .build();

            Log.e("WTF", origin.latitude + " " + origin.longitude +" "+ destination.latitude + " "+ destination.longitude);

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

            DirectionsApiRequest apiRequest = DirectionsApi.newRequest(getGeoContext())
                    .origin(String.valueOf(origin))
                    .destination(String.valueOf(destination))
                    .mode(TravelMode.DRIVING);
            apiRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
                @Override
                public void onResult(DirectionsResult result) {
                    // If the API call was successful, draw the route on the map
                    if (result.routes != null && result.routes.length > 0) {
                        DirectionsRoute route = result.routes[0];
                        List<com.google.maps.model.LatLng> decodedPath =  PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                        List<LatLng> latLngList = new ArrayList<>();
                        for (com.google.maps.model.LatLng mapsLatLng : decodedPath) {
                            latLngList.add(new LatLng(mapsLatLng.lat, mapsLatLng.lng));
                        }


                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.BLUE);
                        polylineOptions.width(10);
                        polylineOptions.addAll(latLngList);
                        googleMap.addPolyline(polylineOptions);


                        long distance = 0;
                        long duration = 0;
                        for (DirectionsLeg leg : route.legs) {
                            distance += leg.distance.inMeters;
                            duration += leg.duration.inSeconds;
                        }


                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Route Information");
                        builder.setMessage(String.format("Distance: %d meters\nTime: %d seconds", distance, duration));
                        builder.setPositiveButton("OK", null);
                        builder.show();
                    } else {

                        Toast.makeText(getActivity(), "Failed to retrieve directions", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Throwable e) {

                    if (e.getMessage() != null) {
                        Log.e("Failed to retrieve directions", e.getMessage());
                    } else {
                        Log.e("Failed to retrieve directions", "Unknown error occurred");
                    }
                }
            });
        }
    }*/


/*    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();
        return geoApiContext;
    }*/



    @SuppressLint("ClickableViewAccessibility")
    private void setUpRecyclerView(RecyclerView recyclerView){

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        recyclerView.setHasFixedSize(true);
        recyclerView.setVisibility(View.GONE);


        //FIXME SNAP HELPER
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();

                if (action == MotionEvent.ACTION_SCROLL) {

                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                    int currentPosition = linearLayoutManager.findFirstVisibleItemPosition();

                    recyclerView.smoothScrollToPosition(currentPosition + 1);

                }

                return false;
            }
        });
    }

    private void setupSearchView(View view) {
        SearchView search = view.findViewById(R.id.searchLocation);
        final String[] columns = new String[] { BaseColumns._ID, "event_address" };
        final MatrixCursor cursor = new MatrixCursor(columns);

        int i = 0;
        for (SerializableGeoPoint geopoint: ContainerActivity.geo_points) {
            cursor.addRow(new Object[] {i, geopoint.getAddress()});
            i++;
        }

        final CursorAdapter suggestionsAdapter = new SimpleCursorAdapter(requireContext(),
                android.R.layout.simple_list_item_1,
                cursor,
                new String[] {"event_address"},
                new int[] {android.R.id.text1});

        search.setSuggestionsAdapter(suggestionsAdapter);

        search.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = suggestionsAdapter.getCursor();
                cursor.moveToPosition(position);
                @SuppressLint("Range") String suggestion = cursor.getString(cursor.getColumnIndex("event_address"));
                search.setQuery(suggestion, true);
                return true;
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                for (Marker marker: markers) {
                    if(marker.getTitle().toLowerCase(Locale.ROOT).equals(query.toLowerCase(Locale.ROOT))) {
                        LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
                        marker.showInfoWindow();
                        recyclerView.setAdapter(new ViewAllAdapter(getContext(), Filter.location(marker.getTitle()), false));
                        recyclerView.setVisibility(View.VISIBLE);
                        break;
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String[] columns = new String[] { BaseColumns._ID, "event_address" };
                MatrixCursor cursor = new MatrixCursor(columns);
                int count = 0;
                for (Marker marker: markers) {
                    if (marker.getTitle().toLowerCase(Locale.ROOT).contains(newText.toLowerCase())) {
                        cursor.addRow(new Object[] {count, marker.getTitle()});
                        count++;
                        if (count == 3) break;
                    }
                }
                suggestionsAdapter.changeCursor(cursor);
                return true;
            }
        });
    }
}
