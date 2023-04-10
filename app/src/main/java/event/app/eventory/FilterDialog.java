package event.app.eventory;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import event.app.eventory.Logic.Convertor;
import event.app.eventory.Logic.Filter;

import event.app.eventory.R;

import event.app.eventory.adapters.LocationAdapter;
import event.app.eventory.adapters.TagAdapter;
import event.app.eventory.models.CardModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.slider.RangeSlider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class FilterDialog extends BottomSheetDialogFragment{

    BottomSheetDialog dialog;

    TagAdapter tagAdapter;
    int sliderStartPrice = 0, sliderEndPrice = 100000;
    static Date startDate = null;
    static Date endDate = null;

    HashSet<CardModel> filteredList;

    FilterDialog(TagAdapter tagAdapter){
        this.tagAdapter = tagAdapter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.dialog_filter, null);
        dialog.setContentView(view);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BottomSheetBehavior bottomSheetBehavior =  BottomSheetBehavior.from((View) view.getParent());
//        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
        bottomSheetBehavior.setPeekHeight(1500);


        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = View.inflate(getContext(), R.layout.dialog_filter, null);
        RecyclerView categoryRec = root.findViewById(R.id.categoryRec);
        TextView allBtn = root.findViewById(R.id.all_btn);
        RangeSlider priceSlider = root.findViewById(R.id.priceSlider);
        RelativeLayout see_location = root.findViewById(R.id.seeLocation);
        ImageView chavron_btn = root.findViewById(R.id.chavron);
        RecyclerView locationRec = root.findViewById(R.id.locationRec);
        Button resetBtn = root.findViewById(R.id.resetBtn);
        Button applyBtn = root.findViewById(R.id.applyBtn);
        Button todayBtn = root.findViewById(R.id.todayBtn);
        Button tmrBtn = root.findViewById(R.id.tomorrowBtn);
        Button wkndBtn = root.findViewById(R.id.weekendBtn);
        Button setDateBtn = root.findViewById(R.id.setDateBtn);

        Calendar calendar = Calendar.getInstance();


        Pair<Long, Long> date_range = new Pair<>(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds());
        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker().setSelection(date_range).build();


        categoryRec.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryRec.setAdapter(tagAdapter);
        categoryRec.setHasFixedSize(true);

        LocationAdapter locationAdapter = new LocationAdapter(getContext(), ContainerActivity.locations_set);
        locationRec.setLayoutManager(new GridLayoutManager(getContext(), 1));
        locationRec.setNestedScrollingEnabled(false);
        locationRec.setHasFixedSize(true);
        locationRec.setAdapter(locationAdapter);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.US);
        String date = "";
        if(startDate != null) date = dateFormat.format(startDate);
        if(endDate != null) date += " - " + dateFormat.format(endDate);
        if(!date.isEmpty()) setDateBtn.setText(date);

        if(tagAdapter.isMapFragment())  filteredList =  MapFragment.mapFilteredList;
        else filteredList = ViewAllActivity.filteredList;

        Filter filter = new Filter(filteredList);


        allBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allBtn.isSelected()) {
                    categoryRec.setLayoutManager(new GridLayoutManager(getContext(), 3));
                    allBtn.setText("less");
                    allBtn.setSelected(false);
                }
                else {
                    categoryRec.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    allBtn.setText("View all");
                    allBtn.setSelected(true);
                }
            }
        });

        priceSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(RangeSlider slider, float value, boolean fromUser) {
                // Get the start and end values of the RangeSlider
                sliderStartPrice = slider.getValues().get(0).intValue();
                sliderEndPrice = slider.getValues().get(1).intValue();


            }
        });



        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagAdapter.selected_tags.clear();
                if(tagAdapter.isMapFragment()) {
                    tagAdapter.selected_tags.addAll(Convertor.categories.keySet());
                }
                tagAdapter.notifyDataSetChanged();
                locationAdapter.selected_locations.clear();
                locationAdapter.notifyDataSetChanged();

                setDateBtn.setText("Choose a date");
                startDate = null;
                endDate = null;
                filter.reset();

                if(tagAdapter.isMapFragment()) MapFragment.showAllMarkers();
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                filteredList.clear();
                filter.filterByTags(tagAdapter);

                if(endDate != null) filter.filterByDate(startDate, endDate);
                else if(startDate != null) filter.filterByDate(startDate);
                filter.filterByPrice(sliderStartPrice, sliderEndPrice);
                filter.filterByLocations(LocationAdapter.selected_locations);


                if(tagAdapter.isMapFragment()) MapFragment.filterMarkers();
                dismiss();

            }
        });

        todayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.US);
                String dateString = dateFormat.format(calendar.getTime());
                setDateBtn.setText(dateString);
                startDate = calendar.getTime();
                endDate = null;
            }
        });
        tmrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.US);
                String dateString = dateFormat.format(calendar.getTime());
                setDateBtn.setText(dateString);
                startDate = calendar.getTime();
                endDate = null;

                // Reset the calendar to the current date for future use
                calendar.setTimeInMillis(System.currentTimeMillis());
            }
        });
        wkndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                        calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.US);
                String dateString = dateFormat.format(calendar.getTime());
                startDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                dateString = dateString + " - " + dateFormat.format(calendar.getTime());
                setDateBtn.setText(dateString);

                endDate = calendar.getTime();

                // Reset the calendar to the current date for future use
                calendar.setTimeInMillis(System.currentTimeMillis());
            }
        });

        setDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getFragmentManager(), "MATERIAL_DATE_PICKER");
            }
        });

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                setDateBtn.setText(""+ materialDatePicker.getHeaderText());
                startDate = new Date(selection.first);
                endDate = new Date(selection.second);
            }
        });

        see_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(chavron_btn.isSelected()){
                    locationRec.setVisibility(View.GONE);
                    chavron_btn.setSelected(false);
                }
                else {
                    locationRec.setVisibility(View.VISIBLE);
                    chavron_btn.setSelected(true);
                }
            }
        });

        return root;
    }
}
