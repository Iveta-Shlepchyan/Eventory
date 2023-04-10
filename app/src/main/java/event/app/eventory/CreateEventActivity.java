package event.app.eventory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import event.app.eventory.Logic.Convertor;

import event.app.eventory.R;

import event.app.eventory.adapters.DateAdapter;
import event.app.eventory.adapters.ImageAdapter;
import event.app.eventory.adapters.TagAdapter;
import event.app.eventory.models.CardModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity implements DateTimePickerDialog.ConfirmPressedListener, SetLocationDialog.SetLocationPressedListener {

    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_SELECT_IMAGE = 2;
    private static final int REQUEST_CODE_SELECT_IMAGES = 3;

    ImageView eventImage, eventPlaceIcon;
    TextView eventName, eventDescription,eventDateTime, eventDuration, eventGenre,
            eventMinAge, eventPrice, setLocation;
    ImageButton backBtn, likeBtn, shareBtn;
    Button ticketBtn, addImagesBtn, addDatesBtn;
    RecyclerView moreImagesRec, dateRec, tagsRec;
    LinearLayout dateTimeLayout;
    SupportMapFragment map;

    CardModel event = new CardModel();

    ArrayList<String> selectedImages = new ArrayList<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM, EEEE HH:mm", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        eventImage = findViewById(R.id.eventImage);
        eventName = findViewById(R.id.eventName);
        eventDescription = findViewById(R.id.eventDescription);
        eventDateTime = findViewById(R.id.eventDateTime);
        eventDuration = findViewById(R.id.eventDuration);
        eventGenre = findViewById(R.id.eventGenre);
        eventMinAge = findViewById(R.id.eventMinAge);
        eventPlaceIcon = findViewById(R.id.eventPlaceIcon);
        eventPrice = findViewById(R.id.eventMinPrice);
        setLocation = findViewById(R.id.setLocation);

        backBtn = findViewById(R.id.backBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        ticketBtn = findViewById(R.id.ticketBtn);

        addImagesBtn = findViewById(R.id.add_images);
        addDatesBtn = findViewById(R.id.add_dates);
        dateTimeLayout = findViewById(R.id.date_time_layout);

        moreImagesRec = findViewById(R.id.more_images_recycler);
        dateRec = findViewById(R.id.date_time_list);
        tagsRec = findViewById(R.id.tags_recycler);

        tagsRec.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        TagAdapter tagAdapter = new TagAdapter(this, new ArrayList<String>(Collections.singleton("User Event")), false);
        tagsRec.setAdapter(tagAdapter);
        tagsRec.setHasFixedSize(true);


        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapLocation);
        getSupportFragmentManager().beginTransaction().hide(map).commit();


        eventImage.setImageResource(R.drawable.add_image);
        eventImage.setOnClickListener(v -> {
            if(!checkStoragePermission()) requestStoragePermission();
            if(checkStoragePermission()) chooseImage();
        });

        moreImagesRec.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        addImagesBtn.setOnClickListener(v -> chooseImages());

        dateRec.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        addDatesBtn.setOnClickListener(v -> {
            DateTimePickerDialog pickerDialog = new DateTimePickerDialog(this);
            pickerDialog.setConfirmPressedListener(this);
            pickerDialog.show(getSupportFragmentManager(), "datePicker");
        });
        eventDateTime.setOnClickListener(v -> {
            DateTimePickerDialog pickerDialog = new DateTimePickerDialog(this);
            pickerDialog.setConfirmPressedListener(this);
            pickerDialog.show(getSupportFragmentManager(), "datePicker");
        });

        setLocation.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, setLocation);

            popup.getMenuInflater().inflate(R.menu.set_location_menu, popup.getMenu());
            popup.setGravity(Gravity.START);

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()) {
                        case (R.id.venue) :
                            SetLocationDialog locationDialog = new SetLocationDialog(CreateEventActivity.this);
                            locationDialog.setLocationPressedListener(CreateEventActivity.this);
                            locationDialog.show(getSupportFragmentManager(), "locationDialog");
                            break;
                        case (R.id.online) :
                            eventPlaceIcon.setBackgroundResource(R.drawable.ic_online);
                            break;
                    }

                    return true;
                }
            });

            popup.show();
        });

    }


    private void resetButton(){
        moreImagesRec.setVisibility(View.GONE);
        addImagesBtn.setVisibility(View.VISIBLE);
    }
    private void chooseImage() {

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), REQUEST_SELECT_IMAGE);
    }
    
    private void chooseImages(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select images"), REQUEST_CODE_SELECT_IMAGES);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri selectedImageUri = data.getData();
            cropImage(selectedImageUri);

        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {

            eventImage.setImageDrawable(null);
            Uri croppedImageUri = UCrop.getOutput(data);
            eventImage.setImageURI(croppedImageUri);
            //TODO image to firebase storage then--> event.setImage_url();

        }
        else if (requestCode == REQUEST_CODE_SELECT_IMAGES && resultCode == RESULT_OK) {

            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    selectedImages.add(clipData.getItemAt(i).getUri().toString());
                }
            } else {
                selectedImages.add(data.getData().toString());
            }
            if(!selectedImages.isEmpty()) {

                addImagesBtn.setVisibility(View.GONE);
                moreImagesRec.setVisibility(View.VISIBLE);

                ImageAdapter imageAdapter = new ImageAdapter(this, selectedImages);
                moreImagesRec.setAdapter(imageAdapter);
                imageAdapter.setOnNoItemListener(this::resetButton);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_PERMISSIONS) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

    }

    private void cropImage(Uri selectedImageUri){

        UCrop.Options options = new UCrop.Options();
        options.setToolbarTitle("Crop Image");
        options.setCompressionQuality(100);

        UCrop.of(selectedImageUri, Uri.fromFile(new File(getCacheDir(), "cropped_image")))
                .withAspectRatio(16, 9)
                .withOptions(options)
                .start(this);
    }





    private Boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                REQUEST_PERMISSIONS);
    }

    @Override
    public void onConfirmPressed(ArrayList<Date> dates) {
        event.setDates(dates);
        if(!dates.isEmpty()) {
            DateAdapter dateAdapter = new DateAdapter(this, dates);
            dateRec.setAdapter(dateAdapter);
            dateTimeLayout.setVisibility(View.VISIBLE);
            eventDateTime.setVisibility(View.VISIBLE);
            addDatesBtn.setVisibility(View.GONE);

            String date = dateFormat.format(event.getDates().get(0));
            eventDateTime.setText(date);
            eventDateTime.setTextColor(getResources().getColor(R.color.info));
        }else {
            dateTimeLayout.setVisibility(View.GONE);
            addDatesBtn.setVisibility(View.VISIBLE);
            eventDateTime.setTextColor(getResources().getColor(R.color.purple));
            eventDateTime.setText(R.string.set_date);
        }
    }

    @Override
    public void onSetLocationPressed(String address, LatLng latLng) {
        setLocation.setText(address);
        setLocation.setTextColor(getResources().getColor(R.color.info));
        getSupportFragmentManager().beginTransaction().show(map).commit();
        setUpMap(map,latLng);
    }

    @SuppressLint("PotentialBehaviorOverride")
    private void setUpMap(SupportMapFragment map, LatLng latLng) {


        map.getMapAsync(googleMap -> {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    CreateEventActivity.this, R.raw.style2_json));
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).icon(Convertor.get_icon("User Event")));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            googleMap.setOnMapClickListener(latLng1 -> {
                SetLocationDialog locationDialog = new SetLocationDialog(CreateEventActivity.this, latLng);
                locationDialog.setLocationPressedListener(CreateEventActivity.this);
                locationDialog.show(getSupportFragmentManager(), "locationDialog");
            });
            googleMap.setOnMarkerClickListener(marker -> {
                SetLocationDialog locationDialog = new SetLocationDialog(CreateEventActivity.this, latLng);
                locationDialog.setLocationPressedListener(CreateEventActivity.this);
                locationDialog.show(getSupportFragmentManager(), "locationDialog");
                return false;
            });

        });
    }


}