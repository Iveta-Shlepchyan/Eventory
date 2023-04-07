package com.example.eventory.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventory.ContainerActivity;
import com.example.eventory.EventPageActivity;
import com.example.eventory.Logic.Convertor;
import com.example.eventory.R;
import com.example.eventory.models.CardModel;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LikeAdapter extends RecyclerView.Adapter<LikeAdapter.ViewHolder>{

    private Context context;
    private List<CardModel> cardModelList;
    private LikeAdapter.onChangeListener onChangeListener;

    public LikeAdapter(Context context, List<CardModel> cardModelList) {
        this.context = context;
        this.cardModelList = cardModelList;
    }

    public interface onChangeListener {
        void onChange();
    }

    public void setOnNoItemListener(LikeAdapter.onChangeListener onChangeListener){
        this.onChangeListener = onChangeListener;
    }



    @NonNull
    @Override
    public LikeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LikeAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_like_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull LikeAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Glide.with(context).load(cardModelList.get(position).getImg_url()).into(holder.img);
        holder.eventName.setText(cardModelList.get(position).getName());
//        holder.eventDateTime.setText(cardModelList.get(position).getDate_time_list().get(0));
        if (cardModelList.get(position).getDates()!=null && !cardModelList.get(position).getDates().isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM, EEEE HH:mm", Locale.ENGLISH);
            String date = dateFormat.format(cardModelList.get(position).getDates().get(0));
            holder.eventDateTime.setText(date);
        }
        holder.eventPlace.setText(cardModelList.get(position).getLocation());
        if(cardModelList.get(position).getPrices() != null && !cardModelList.get(position).getPrices().isEmpty())
            holder.eventPrice.setText(cardModelList.get(position).getPrices().get(0).toString());
        else holder.eventPrice.setVisibility(View.GONE);
//        if(cardModelList.get(position).getMin_price() == null)
//            holder.eventPrice.setVisibility(View.GONE);
//        else holder.eventPrice.setText(cardModelList.get(position).getMin_price());

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContainerActivity.likedCards.remove(cardModelList.get(holder.getPosition()));
                notifyItemRemoved(holder.getPosition());
                Convertor.saveLikes(context.getApplicationContext());
                onChangeListener.onChange();
            }
        });

        holder.likedCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, EventPageActivity.class);
                i.putExtra("info", cardModelList.get(holder.getPosition()));
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return cardModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private TextView eventName, eventDateTime, eventPlace, eventPrice;
        private ImageButton likeBtn;
        private CardView likedCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            likedCard = itemView.findViewById(R.id.eventCard);
            img = itemView.findViewById(R.id.eventImage);
            eventName = itemView.findViewById(R.id.eventName);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
            eventPlace = itemView.findViewById(R.id.eventPlace);
            eventPrice = itemView.findViewById(R.id.eventMinPrice);
            likeBtn = itemView.findViewById(R.id.likeBtn);

        }
    }
}