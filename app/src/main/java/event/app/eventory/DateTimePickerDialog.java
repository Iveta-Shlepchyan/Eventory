package event.app.eventory;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import event.app.eventory.R;

import event.app.eventory.adapters.DateTimeAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;


import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;


import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class DateTimePickerDialog extends BottomSheetDialogFragment {

    BottomSheetDialog dialog;
    private Context context;
    private ConfirmPressedListener listener;
    private ArrayList<Date> dates;


    public DateTimePickerDialog(Context context){
        this.context = context;
    }

    public DateTimePickerDialog(Context context, ArrayList<Date> dates) {
        this.dates = dates;
    }

    public interface ConfirmPressedListener {
        void onConfirmPressed(ArrayList<Date> dates);
    }

    public void setConfirmPressedListener(ConfirmPressedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.dialog_date_time_picker, null);
        dialog.setContentView(view);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BottomSheetBehavior bottomSheetBehavior =  BottomSheetBehavior.from((View) view.getParent());
//        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
        bottomSheetBehavior.setPeekHeight(1500);


        return dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_date_time_picker, container, false);

        Button applyBtn = view.findViewById(R.id.applyBtn);
        Button cancelBtn = view.findViewById(R.id.cancelBtn);

        MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);
        RecyclerView datesRec = view.findViewById(R.id.date_time_recycler);


        datesRec.setHasFixedSize(true);
        datesRec.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));

        DateTimeAdapter dateTimeAdapter = new DateTimeAdapter(this.getContext(), calendarView);
        if(dates != null) {
            dateTimeAdapter.setDates(dates);
            for (Date date : dates) {
                CalendarDay calendarDay = CalendarDay.from(date.getYear()+1900, date.getMonth() + 1, date.getDate());
                calendarView.setDateSelected(calendarDay, true);
            }
        }

        datesRec.setAdapter(dateTimeAdapter);


        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1).minusDays(1);

        calendarView.state().edit()
                .setMinimumDate(today)
                .setMaximumDate(CalendarDay.from(endDate))
                .commit();

        calendarView.setOnDateChangedListener((widget, calendarDay, selected) -> {

            Calendar calendar = Calendar.getInstance();
            calendar.set(calendarDay.getYear(), calendarDay.getMonth()-1, calendarDay.getDay(),0,0,0);
            Date date = calendar.getTime();
            dateTimeAdapter.dateChanged(date, selected);
        });

        applyBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmPressed(dateTimeAdapter.getDates());
            }
            dismiss();
        });

        cancelBtn.setOnClickListener(v -> {
            dismiss();
        });



        return view;
    }

}


