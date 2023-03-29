package com.example.eventory.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventory.CreateEventActivity;
import com.example.eventory.DateTimePickerDialog;
import com.example.eventory.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> implements DateTimePickerDialog.ConfirmPressedListener {
    private Context context;
    private int last_selected = 0;
    private int selected_card = 0;
    private ArrayList<Date> dates = new ArrayList<>();
    private ArrayList<String> stringDates = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM EEEE HH:mm", Locale.ENGLISH);

    private DateTimePickerDialog.ConfirmPressedListener confirmPressedListener;



    public DateAdapter(Context context, ArrayList<Date> dates) {
        this.context = context;

        for (Date date: dates) {
            stringDates.add(dateFormat.format(date));
        }
        if (context instanceof DateTimePickerDialog.ConfirmPressedListener) {
            confirmPressedListener = (DateTimePickerDialog.ConfirmPressedListener) context;
            this.dates.addAll(dates);
        }

    }

    @Override
    public void onConfirmPressed(ArrayList<Date> dates) {
        for (Date date: dates) {
            stringDates.add( dateFormat.format(date));
        }
    }

    public interface onItemClickListener{
        void onClick(int position);
    }

    DateAdapter.onItemClickListener onItemClickListner;

    public void setOnItemClickListener(DateAdapter.onItemClickListener
                                               onItemClickListner)
    {
        this.onItemClickListner = onItemClickListner;
    }


    @NonNull
    @Override
    public DateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DateAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_time, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DateAdapter.ViewHolder holder, int position) {

        if(position == last_selected){
            holder.dateCard.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            holder.dateCard.setForeground(context.getResources().getDrawable(R.drawable.bg_card_borders));
        }
        else {
            holder.dateCard.setCardBackgroundColor(context.getResources().getColor(R.color.cool_grey));
            holder.dateCard.setForeground(null);
        }


        String dateStr = stringDates.get(position);
        holder.eventDay.setText(dateStr.split(" ")[0]);
        holder.eventMonth.setText(dateStr.split(" ")[1]);
        holder.eventWeekday.setText(dateStr.split(" ")[2]);
        holder.eventTime.setText(dateStr.split(" ")[3]);

        holder.dateCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context instanceof CreateEventActivity){
                    DateTimePickerDialog dialogFragment = new DateTimePickerDialog(context, dates);
                    dialogFragment.setConfirmPressedListener(confirmPressedListener);
                    dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "datePicker");
                }
                else {
                    holder.dateCard.setCardBackgroundColor(context.getResources().getColor(R.color.white));
                    holder.dateCard.setForeground(context.getResources().getDrawable(R.drawable.bg_card_borders));
                    selected_card = holder.getAdapterPosition();
                    notifyItemChanged(last_selected);
                    last_selected = selected_card;
                    onItemClickListner.onClick(last_selected);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return stringDates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView eventWeekday, eventMonth, eventDay, eventTime;
        private CardView dateCard;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            eventWeekday = itemView.findViewById(R.id.weekday);
            eventMonth = itemView.findViewById(R.id.month);
            eventDay = itemView.findViewById(R.id.day);
            eventTime = itemView.findViewById(R.id.time);
            dateCard = itemView.findViewById(R.id.date_card);

        }
    }


}

