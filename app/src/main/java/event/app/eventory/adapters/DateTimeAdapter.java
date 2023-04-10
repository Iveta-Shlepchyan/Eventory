package event.app.eventory.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import event.app.eventory.R;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class DateTimeAdapter extends RecyclerView.Adapter<DateTimeAdapter.ViewHolder>{

    private Context context;
    private MaterialCalendarView calendarView;
    private ArrayList<Date> dates = new ArrayList<Date>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM, EEEE", Locale.ENGLISH);

    public DateTimeAdapter(Context context, MaterialCalendarView calendarView) {
        this.context = context;
        this.calendarView = calendarView;
    }

    public void dateChanged(Date date, boolean selected){
        if(selected){
            dates.add(date);
            Collections.sort(dates);
        }
        else {
            ArrayList<Date> selectedDates = new ArrayList<>(dates);
            for (Date date1: selectedDates){
                if(date1.getDate() == date.getDate()){
                    dates.remove(date1);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DateTimeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DateTimeAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_date_time, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date date = dates.get(position);

        holder.date.setText(dateFormat.format(date));
        String time = date.getMinutes() > 9 ?
                date.getHours() + " : " + date.getMinutes()
                : date.getHours() + " : 0" +date.getMinutes();
        holder.time.setText(time);

        holder.dateTimeCard.setOnClickListener(v -> {

            MaterialTimePicker materialTimePicker = setUpTimePicker(position);
            materialTimePicker.show(((FragmentActivity) context).getSupportFragmentManager(), "TIME_PICKER");
        });

        holder.removeBtn.setOnClickListener(v -> {

            int day = dates.get(position).getDate();
            dates.remove(position);
            deselectDate(day);
            notifyDataSetChanged();
        });

    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public ArrayList<Date> getDates() {
        return dates;
    }

    public void setDates(ArrayList<Date> dates) {
        this.dates.addAll(dates);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView dateTimeCard;
        private ImageButton removeBtn;
        private TextView date, time;

        public ViewHolder(View view) {
            super(view);
            dateTimeCard = view.findViewById(R.id.date_time_card);
            removeBtn = view.findViewById(R.id.remove_btn);
            date = view.findViewById(R.id.add_date);
            time = view.findViewById(R.id.add_time);
        }
    }

    private MaterialTimePicker setUpTimePicker(int position) {
        MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                .setTitleText("Select a time")
                .setHour(dates.get(position).getHours())
                .setMinute(dates.get(position).getMinutes())
                .setPositiveButtonText("Change")
                .setNegativeButtonText("Add")
                .build();

        materialTimePicker.addOnPositiveButtonClickListener(v -> {

            Date new_date = getNewDate(materialTimePicker, position);

            if (!dates.contains(new_date)) {
                dates.get(position).setHours(materialTimePicker.getHour());
                dates.get(position).setMinutes(materialTimePicker.getMinute());
                changeTimeForAll(materialTimePicker.getHour(), materialTimePicker.getMinute());
                notifyItemChanged(position);
            }
            else Toast.makeText(context, "This date has already been added", Toast.LENGTH_LONG).show();

        });

        materialTimePicker.addOnNegativeButtonClickListener(v -> {

            Date new_date = getNewDate(materialTimePicker, position);

            if (!dates.contains(new_date)) {
                dates.add(new_date);
                Collections.sort(dates);
                notifyDataSetChanged();
            }
            else Toast.makeText(context, "This date has already been added", Toast.LENGTH_LONG).show();


        });
        return materialTimePicker;
    }

    private void deselectDate(int day) {
        boolean noDate = true;
        for (Date date: dates) {
            if(date.getDate()==day){
                noDate = false;
                break;
            }
        }
        if(noDate) {
            for (CalendarDay calendarDay : calendarView.getSelectedDates()) {
                if (calendarDay.getDay() == day) {
                    calendarView.setDateSelected(calendarDay, false);
                    break;
                }
            }
        }
    }

    private void changeTimeForAll(int hours, int minutes){
        for (int i = 0; i < dates.size(); i++) {
            Date date = dates.get(i);
            if (date.getHours() == 0 && date.getMinutes() == 0){
                date.setHours(hours);
                date.setMinutes(minutes);
                notifyItemChanged(i);
            }
        }
    }

    private Date getNewDate(MaterialTimePicker timePicker, int position){
        Date new_date = new Date();
        new_date.setTime(dates.get(position).getTime());
        new_date.setHours(timePicker.getHour());
        new_date.setMinutes(timePicker.getMinute());
        return new_date;
    }
}
