package com.example.eventory.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventory.CreateEventActivity;
import com.example.eventory.ImageDialog;
import com.example.eventory.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> more_images;
    private int layout_item = R.layout.item_image;
    private ImageAdapter.onNoItemListener onNoItemListener;


    public ImageAdapter(Context context, ArrayList<String> more_images) {
        this.context = context;
        this.more_images = more_images;
        if(context instanceof CreateEventActivity) layout_item = R.layout.item_removable_image;
    }

    public interface onNoItemListener {
        void onNoItem();
    }

    public void setOnNoItemListener(ImageAdapter.onNoItemListener onNoItemListener){
        this.onNoItemListener = onNoItemListener;
    }






    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(more_images.get(position)).into(holder.image);



            holder.image.setOnClickListener(v -> {
                if(layout_item == R.layout.item_image) {
                    ImageDialog dialogFragment = new ImageDialog(more_images.get(holder.getAdapterPosition()));
                    dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "Dialog Fragment");
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    ((AppCompatActivity) context).startActivityForResult(Intent.createChooser(intent, "Select images"), 3);
                  }
            });



            if(holder.removeBtn != null){
                holder.removeBtn.setOnClickListener(v -> {
                    more_images.remove(more_images.get(holder.getPosition()));
                    notifyItemRemoved(holder.getPosition());
                    if(getItemCount() == 0) onNoItemListener.onNoItem();
                });
            }

    }




    @Override
    public int getItemCount() {
        return more_images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private ImageButton removeBtn;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.more_images);
            removeBtn = itemView.findViewById(R.id.remove_btn);
        }
    }


}
