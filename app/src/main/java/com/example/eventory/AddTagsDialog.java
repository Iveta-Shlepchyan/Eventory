package com.example.eventory;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventory.adapters.DateTimeAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddTagsDialog extends BottomSheetDialogFragment {

    BottomSheetDialog dialog;
    private Context context;
//    private DateTimePickerDialog.ConfirmPressedListener listener;
    private ArrayList<String> tags;


    public AddTagsDialog(Context context){
        this.context = context;
    }

    public AddTagsDialog(Context context, ArrayList<Date> dates) {
        this.tags = tags;
    }

  /*  public interface ConfirmPressedListener {
        void onConfirmPressed(ArrayList<Date> dates);
    }

    public void setConfirmPressedListener(DateTimePickerDialog.ConfirmPressedListener listener) {
        this.listener = listener;
    }*/

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
        View view = inflater.inflate(R.layout.dialog_add_tags, container, false);

        MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);
        RecyclerView tagsRec = view.findViewById(R.id.tags_recycler);
        RecyclerView selectedTagsRec = view.findViewById(R.id.selected_tags_recycler);



        tagsRec.setLayoutManager(new GridLayoutManager(getContext(), 3));
        selectedTagsRec.setLayoutManager(new GridLayoutManager(getContext(), 3));


        if(tags != null) {

        }

//        tagsRec.setAdapter();



        return view;
    }


}
