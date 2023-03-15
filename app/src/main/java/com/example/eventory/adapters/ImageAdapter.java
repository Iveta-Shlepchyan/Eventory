package com.example.eventory.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventory.ImageDialogFragment;
import com.example.eventory.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private ArrayList<String> more_images;



    public ImageAdapter(Context context, ArrayList<String> more_images) {
        this.context = context;
        this.more_images = more_images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(more_images.get(position)).into(holder.image);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageDialogFragment dialogFragment = new ImageDialogFragment(more_images.get(holder.getAdapterPosition()));
                Log.e("adapter", "click " + dialogFragment);
                dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "Dialog Fragment");
            }
        });

    }

    @Override
    public int getItemCount() {
        return more_images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.more_images);
        }
    }


}
