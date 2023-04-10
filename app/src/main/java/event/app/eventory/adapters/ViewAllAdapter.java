package event.app.eventory.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import event.app.eventory.ContainerActivity;
import event.app.eventory.EventPageActivity;
import event.app.eventory.Logic.Convertor;
import event.app.eventory.R;
import event.app.eventory.models.CardModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;


public class ViewAllAdapter extends RecyclerView.Adapter<ViewAllAdapter.ViewHolder> {

    private Context context;
    private ArrayList<CardModel> cardModelList;
    public boolean isGrid = true;


    public ViewAllAdapter(Context context, ArrayList<CardModel> cardModelList, boolean isGrid) {
        this.context = context;
        this.cardModelList = cardModelList;
        this.isGrid = isGrid;
    }


    public void filterList(Collection<CardModel> filterlist) {
        cardModelList = new ArrayList<>();
        cardModelList.addAll(filterlist);
        notifyDataSetChanged();
    }

    public void updateLikedState(String eventName, boolean isLiked) {

        for (int i = 0; i < cardModelList.size(); i++) {
            CardModel event = cardModelList.get(i);
            if(event.getName().equals(eventName)) {
                event.setLiked(isLiked);
                notifyItemChanged(i);
                break;
            }
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isGrid) {
            view = LayoutInflater.from(context).inflate(R.layout.item_event_centered, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_like_list, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        CardModel cardModel = cardModelList.get(position);
        Glide.with(context).load(cardModel.getImg_url()).into(holder.cardImg);
        holder.eventName.setText(cardModel.getName());
        if (cardModel.getDates()!=null && !cardModel.getDates().isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM, EEEE HH:mm", Locale.ENGLISH);
            String date = dateFormat.format(cardModel.getDates().get(0));
            holder.eventDateTime.setText(date);
        }
        holder.eventPlace.setText(cardModel.getLocation());
        if(cardModelList.get(position).getPrices() != null && !cardModelList.get(position).getPrices().isEmpty()) {
            String price = cardModel.getPrices().get(0).toString();
            if(price == "0") price = context.getString(R.string.free);
            if(cardModel.getPrices().size() > 1)
                price +="+";
            holder.eventPrice.setText(price);
        }
        else holder.eventPrice.setVisibility(View.GONE);

        int heart = isGrid ? R.drawable.ic_heart_card : R.drawable.ic_heart_card_black;

        holder.likeBtn.setBackgroundResource(heart);
        if(cardModel.isLiked()){
            holder.likeBtn.setBackgroundResource(R.drawable.ic_heart_card_pressed);
        }


        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!cardModelList.get(position).isLiked()) {

                    cardModelList.get(position).setLiked(true);
                    holder.likeBtn.setBackgroundResource(R.drawable.ic_heart_card_pressed);

                    ContainerActivity.likedCards.add(cardModelList.get(position));

                }else {
                    cardModelList.get(position).setLiked(false);
                    holder.likeBtn.setBackgroundResource(heart);


                    for (CardModel likedCard: ContainerActivity.likedCards ) {
                        if (likedCard.getName().equals(cardModel.getName())){
                            ContainerActivity.likedCards.remove(likedCard);
                            break;
                        }
                    }
                }

                Convertor.saveLikes(context.getApplicationContext());
            }
        });


        holder.eventCard.setOnClickListener(new View.OnClickListener() {
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
        private ImageView cardImg;
        private TextView eventName, eventDateTime, eventPlace, eventPrice;
        private ImageButton likeBtn;
        private CardView eventCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImg = itemView.findViewById(R.id.eventImage);
            eventName = itemView.findViewById(R.id.eventName);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
            eventPlace = itemView.findViewById(R.id.eventPlace);
            eventPrice = itemView.findViewById(R.id.eventMinPrice);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            eventCard = itemView.findViewById(R.id.eventCard);
        }
    }
}
