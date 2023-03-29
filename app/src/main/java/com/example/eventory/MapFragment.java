package com.example.eventory;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.eventory.Logic.Convertor;
import com.example.eventory.Logic.Filter;
import com.example.eventory.adapters.TagAdapter;
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

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class MapFragment extends Fragment implements GoogleMap.OnMyLocationChangeListener {

    SupportMapFragment supportMapFragment;
    private static GoogleMap googleMap;
    private GeoApiContext geoApiContext;

    private static final int REQUEST_CODE = 101;
    private boolean locationPermissionGranted;

    public static HashSet<Marker> markers = new HashSet<>();
    public static HashSet<CardModel> mapFilteredList = new HashSet<>();
    public static HashSet<String> filtered_address_set = new HashSet<>();
    private Filter filters = new Filter(mapFilteredList);


//    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public  ViewAllAdapter dialogAdapter;
    private TagAdapter tagAdapter;
    private RecyclerView eventRecView;
    private RecyclerView tagsRecView;


    private LatLng currentLocation;
    private Marker selectedMarker;
    private static Polyline main_polyline;
    private static Marker polyline_info;

    @Override
    public void onResume() {
        super.onResume();
        if(ContainerActivity.lastLikedEvent != null && dialogAdapter !=null)
        dialogAdapter.updateLikedState(ContainerActivity.lastLikedEvent.getName(), ContainerActivity.lastLikedEvent.isLiked());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();

        askForLocationPermission();
        getCurrentLocation();
        getAllEvents();


        ImageView directionBtn = view.findViewById(R.id.direction_btn);
        ImageView filterBtn = view.findViewById(R.id.filter_btn);
        ImageButton currentLocationBtn = view.findViewById(R.id.current_location_btn);
        ImageButton layersBtn = view.findViewById(R.id.layers_btn);
        eventRecView = view.findViewById(R.id.dialogRec);
        /*eventRecView.setRecycledViewPool(viewPool);
        eventRecView.setHasFixedSize(true);*/
        tagsRecView = view.findViewById(R.id.tags_recycler);

        setUpEventRecycler(eventRecView);
        setUpTagRecycler(tagsRecView);
        setUpSearchView(view);
        setUpMap();



        filterBtn.setOnClickListener(v -> {
            FilterDialog dialog = new FilterDialog(tagAdapter);
            dialog.show(getFragmentManager(), dialog.getTag());
        });


        directionBtn.setOnClickListener(v -> {
            if (selectedMarker != null) {
                LatLng destination = selectedMarker.getPosition();
                if(main_polyline != null) main_polyline.remove();
                calculateDirections(destination);
            }
            else {
                if(main_polyline != null) {
                    main_polyline.remove();
                    polyline_info.remove();
                }
            }
        });

        currentLocationBtn.setOnClickListener(v -> {
            getCurrentLocation();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
        });

        layersBtn.setOnClickListener(v -> {

                    PopupMenu popup = new PopupMenu(requireContext(), layersBtn);

                    popup.getMenuInflater().inflate(R.menu.map_layers_menu, popup.getMenu());
                    popup.setGravity(Gravity.END);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case (R.id.map) :
                                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                    setMapStyle(R.raw.style2_json);
                                    break;
                                case (R.id.satellite) :
                                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                    break;
                                case (R.id.all_markers) :
                                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                    setMapStyle(R.raw.style2_all_markers_json);
                                    break;
                            }

                            return true;
                        }
                    });

                    popup.show();
                });

        return view;

    }

    private void setUpTagRecycler(RecyclerView tagRecView) {
        tagAdapter = new TagAdapter(requireContext(), Convertor.categories.keySet(), true);
        tagAdapter.select_all_map_tags();
        tagsRecView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        tagsRecView.setAdapter(tagAdapter);
        tagsRecView.setHasFixedSize(true);

        tagAdapter.setOnItemClickListener(new TagAdapter.onItemClickListener() {
            @Override
            public void onClick(int position, String tag, boolean selected) {
                filters.filterMarkers(tag, selected);
            }
        });
    }




    @SuppressLint("PotentialBehaviorOverride")
    private void setUpMap(){
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {

            @SuppressLint({"MissingPermission", "PotentialBehaviorOverride"})
            @Override
            public void onMapReady(GoogleMap googleMap) {

                MapFragment.googleMap = googleMap;
                UiSettings UiSettings = googleMap.getUiSettings();
                UiSettings.setMapToolbarEnabled(false);
                UiSettings.setMyLocationButtonEnabled(false);
                googleMap.setPadding(0,300, 0, 0);
                addMarkersToMap();
                setMapStyle(R.raw.style2_json);

                LatLng yerevan = new LatLng(40.177200, 44.503490);
                if (locationPermissionGranted) {
                    googleMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                    if (currentLocation != null)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    else {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yerevan, 10));
                    }
                }
                else {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yerevan, 10));
                }


                /*if(locationPermissionGranted){
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
                }*/



                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {

                        if(!marker.equals(polyline_info)) {
                            dialogAdapter = new ViewAllAdapter(getContext(), filters.location(marker.getTitle()), false);
                            eventRecView.setAdapter(dialogAdapter);
                            eventRecView.setVisibility(View.VISIBLE);
                            selectedMarker = marker;
                        }

                        return false;
                    }
                });

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        if(eventRecView.getVisibility()==View.VISIBLE)
                            eventRecView.setVisibility(View.GONE);
                        if (selectedMarker != null) {
                            selectedMarker.hideInfoWindow();
                            selectedMarker = null;
                        }
                        if(polyline_info != null) polyline_info.showInfoWindow();
                    }
                });


                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(@NonNull Marker marker) {
                        if(!marker.equals(polyline_info)) {
                            Intent i = new Intent(getContext(), ViewAllActivity.class);
                            i.putExtra("location", marker.getTitle());
                            startActivity(i);
                        }

                    }
                });

                googleMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(@NonNull Marker marker) {
                        if(!marker.equals(polyline_info)) {
                            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(marker.getTitle()));
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                    }
                });

            }
        });

    }

    @Override
    public void onMyLocationChange(@NonNull Location location) {
        if(main_polyline != null) main_polyline.remove();
        calculateDirections(new LatLng(location.getLatitude(), location.getLongitude()));
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


    private void setMapStyle(int style){
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                           requireContext(), style));

            if (!success) {
                Log.e("LocationMap", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("LocationMap", "Can't find style.", e);
        }
    }

    private void addMarkersToMap() {
        googleMap.clear();
        markers.clear();
        if(!ContainerActivity.geo_points.isEmpty()){
            for (SerializableGeoPoint geoPoint : ContainerActivity.geo_points){
                LatLng latLng = new LatLng(geoPoint.getGeoPoint().getLatitude(), geoPoint.getGeoPoint().getLongitude());
                Marker new_marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(geoPoint.getAddress())
                        .icon(Convertor.get_icon(geoPoint.getCategory())));
                new_marker.setTag(geoPoint.getCategory());
                markers.add(new_marker);
            }
        }
    }

    public static void filterMarkers(){
        if(main_polyline != null) {
            main_polyline.remove();
            polyline_info.remove();
        }
        getAddressSet(filtered_address_set);
        if(filtered_address_set.size() == markers.size()) showAllMarkers();
        else {
            for (Marker marker : markers) {
                if (filtered_address_set.contains(marker.getTitle())) {
                    marker.setVisible(true);
                } else marker.setVisible(false);
            }
        }
    }

   private static void getAddressSet(HashSet<String> address_set){
        address_set.clear();
        for (CardModel event : mapFilteredList) {
            address_set.add(event.getLocation());
        }
    }

    public static void showAllMarkers(){
        for (Marker marker: markers) {
            marker.setVisible(true);
        }
    }


    public void getAllEvents(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext());
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


    private void calculateDirections(LatLng destination) {
        if (locationPermissionGranted) {
            com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude);
            com.google.maps.model.LatLng destinationLatLng = new com.google.maps.model.LatLng(destination.latitude, destination.longitude);

            DirectionsApiRequest directions = DirectionsApi.newRequest(geoApiContext);

            directions.origin(origin);

            directions.destination(destinationLatLng).setCallback(new PendingResult.Callback<DirectionsResult>() {
                @Override
                public void onResult(DirectionsResult result) {
                    if (result.routes != null && result.routes.length > 0) {
                        DirectionsRoute route = result.routes[0];

                        String encodedPolyline = route.overviewPolyline.getEncodedPath();

                        List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);


                        long distance = 0;
                        long duration = 0;
                        for (DirectionsLeg leg : route.legs) {
                            distance += leg.distance.inMeters;
                            duration += leg.duration.inSeconds;
                        }

                        drawPolyline(decodedPath, distance, duration);

                    } else {
                        Log.e("MapFragment", "Failed to retrieve directions");
                        Toast.makeText(getActivity(), "Failed to retrieve directions", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    Log.e("MapFragment", "calculateDirections: Failed to get directions: " + e.getMessage() );

                }
            });

        }
    }


    private void drawPolyline(List<LatLng> decodedPath, long distance, long duration){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(R.color.direction);
                polylineOptions.width(10);
                polylineOptions.addAll(decodedPath);
                main_polyline = googleMap.addPolyline(polylineOptions);
                animatePolyline(main_polyline, decodedPath, distance, duration);
//                showPolylineInfoWindow(decodedPath, distance, duration);
            }
        });
    }

    private void animatePolyline(final Polyline polyline, final List<LatLng> decodedPath, long distance, long duration) {

        final ValueAnimator animation = ValueAnimator.ofInt(0, 100);
        animation.setDuration(2000);
        animation.setInterpolator(new LinearInterpolator());

        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                int animatedValue = (int) valueAnimator.getAnimatedValue();
                float fraction = animatedValue / 100f;

                int endIndex = (int) (decodedPath.size() * fraction);
                List<LatLng> subList = decodedPath.subList(0, endIndex);

                polyline.setPoints(subList);


            }
        });

        animation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showPolylineInfoWindow(decodedPath, distance, duration);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });



        animation.start();
    }




    private void showPolylineInfoWindow(List<LatLng> decodedPath, long distance, long duration){

        eventRecView.setVisibility(View.GONE);
        selectedMarker = null;

        LatLng polylineCenter = decodedPath.get(decodedPath.size() / 2);

        String distanceStr, durationStr;

        double distanceInKm = distance / 1000.0;

        long minutes = duration / 60;
        int hours = (int) minutes / 60;
        if (hours > 0) {
            durationStr = "Duration: " + hours + " h. " + (minutes - hours * 60) + " min.";
        } else {
            durationStr =  "Duration: " + minutes + " min.";
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        String formattedDistance = decimalFormat.format(distanceInKm).replace(",", ".");
        distanceStr = "Distance: " + formattedDistance + " km";

        MarkerOptions markerOptions = new MarkerOptions()
                .position(polylineCenter)
                .title(durationStr)
                .snippet(distanceStr)
                .alpha(0.0f);

        if(polyline_info != null) polyline_info.remove();
        polyline_info = googleMap.addMarker(markerOptions);
        polyline_info.setAnchor(0.5f, 0.1f);
        polyline_info.showInfoWindow();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpEventRecycler(RecyclerView recyclerView){

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        recyclerView.setHasFixedSize(true);
        recyclerView.setVisibility(View.GONE);


        //FIXME better swipe
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

    private void setUpSearchView(View view) {
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
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                        marker.setVisible(true);
                        marker.showInfoWindow();
                        selectedMarker = marker;
                        eventRecView.setAdapter(new ViewAllAdapter(getContext(), Filter.filterByLocation(marker.getTitle()), false));
                        eventRecView.setVisibility(View.VISIBLE);
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
