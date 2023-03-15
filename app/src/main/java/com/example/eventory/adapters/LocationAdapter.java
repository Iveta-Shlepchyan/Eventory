package com.example.eventory.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventory.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> locations = new ArrayList<>();
    public static ArrayList<String> selected_locations = new ArrayList<String>();

    public LocationAdapter(Context context, Collection<String> locations) {
        this.context = context;
        this.locations.addAll(locations);
    }

    @NonNull
    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LocationAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LocationAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.location.setText(locations.get(position));
        holder.checkBox.setChecked(false);
        if(selected_locations.contains(locations.get(position))) {
            holder.checkBox.setChecked(true);
        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkBox.isChecked()){
                    selected_locations.add(locations.get(position));
                }
                else selected_locations.remove(locations.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView location;
        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.location);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

}