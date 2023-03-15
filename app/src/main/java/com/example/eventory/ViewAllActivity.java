package com.example.eventory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventory.Logic.Filter;
import com.example.eventory.adapters.LocationAdapter;
import com.example.eventory.adapters.TagAdapter;
import com.example.eventory.adapters.ViewAllAdapter;
import com.example.eventory.models.CardModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


public class ViewAllActivity extends AppCompatActivity {

    public static HashSet<CardModel> filteredList = new HashSet<CardModel>();
    public static ArrayList<CardModel> allEvents;
    public static ViewAllAdapter adapter;
    public static TextView found;

    RecyclerView searchRec, tagsRec;

    SearchView search;
    ImageButton gridBtn, listBtn, filterBtn;
    TagAdapter tagAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        searchRec = findViewById(R.id.search_recycler);
        tagsRec = findViewById(R.id.tags_recycler);
        found = findViewById(R.id.found);
        gridBtn = findViewById(R.id.gridMode);
        listBtn = findViewById(R.id.listMode);
        filterBtn = findViewById(R.id.filter_btn);
        search = findViewById(R.id.search);

        //TODO add tags, filter, card out of layout, add hide tag/found bar,
        // remove if event passed (maybe Room local later), SET SEARCHBAR ACTIVE
        final int COLUMNS = calculate_columns(ViewAllActivity.this);
        Filter filters = new Filter();


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String json = preferences.getString("Tomsarkgh", "");
        if (!json.isEmpty()) {
            Type listType = new TypeToken<List<CardModel>>() {
            }.getType();
            Gson gson = new Gson();
            allEvents = gson.fromJson(json, listType);
            Collections.sort(allEvents, filters.priceComparator);

            adapter = new ViewAllAdapter(this, allEvents, true);
            searchRec.setAdapter(adapter);
            searchRec.setHasFixedSize(true);
            searchRec.setLayoutManager(new GridLayoutManager(this, COLUMNS));
            filters.setFounds();
            gridBtn.setSelected(true);

            tagAdapter = new TagAdapter(this, ContainerActivity.tags_set, true);
            tagsRec.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            tagsRec.setAdapter(tagAdapter);
            tagsRec.setHasFixedSize(true);

            String category = getIntent().getStringExtra("category");
            if(category!=null) {
                filteredList.clear();
                TagAdapter.selected_tags.add(category);
                filters.filterByTag(category, true);
                tagSwap(tagAdapter.getTags().indexOf(category), true);
            }
            String location = getIntent().getStringExtra("location");
            if(location != null){
                filteredList.clear();
                filteredList.addAll(Filter.location(location));
                adapter.filterList(filteredList);
                Filter.setFounds();
            }

//            else search.requestFocus();

            tagAdapter.setOnItemClickListener(new TagAdapter.onItemClickListener() {
                @Override
                public void onClick(int position, String tag, boolean selected) {
                    if(tagAdapter.selected_tags.size() == 1 && selected) filteredList.clear();
                    filters.filterByTag(tag, selected);
                    if (tagAdapter.selected_tags.isEmpty() && filteredList.isEmpty()){
                        filters.reset();
                    }

                    tagSwap(position, selected);
                }
            });


        }


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filters.filter(newText);
                return false;
            }
        });
        
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterDialogFragment dialog = new FilterDialogFragment(tagAdapter);
                dialog.show(getSupportFragmentManager(), dialog.getTag());

            }
        });


        gridBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gridBtn.setSelected(true);
                listBtn.setSelected(false);

                adapter.isGrid = true;
                searchRec.setAdapter(adapter);
                searchRec.setLayoutManager(new GridLayoutManager(ViewAllActivity.this, COLUMNS));

            }
        });

        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listBtn.setSelected(true);
                gridBtn.setSelected(false);

                adapter.isGrid = false;
                searchRec.setAdapter(adapter);
                searchRec.setLayoutManager(new LinearLayoutManager(ViewAllActivity.this));

            }
        });




    }


    public int calculate_columns(Context context) {
        float px = getWindowManager().getDefaultDisplay().getWidth();
        float dp = px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int) dp / 150;
    }

    private void tagSwap(int position, boolean selected){
        int new_position = tagAdapter.selected_tags.size();
        new_position = selected ?  new_position -1 : new_position;

        Collections.swap(tagAdapter.getTags(), position, new_position);

        if(selected) tagsRec.smoothScrollToPosition(new_position);
        tagAdapter.notifyDataSetChanged();
    }

}