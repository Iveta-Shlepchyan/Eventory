package com.example.eventory.Logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.example.eventory.ContainerActivity;
import com.example.eventory.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;

public class Convertor {

    public static BitmapDescriptor get_icon(String category){
        switch (category){
            case "Theater":
                return ContainerActivity.pins.get(0);

            case "Cinema":
                return ContainerActivity.pins.get(1);

            case "Concert":
                return ContainerActivity.pins.get(2);

            case "Clubs & pubs":
                return ContainerActivity.pins.get(3);

            default:
                return ContainerActivity.pins.get(4);
        }
    }

    public static ArrayList<BitmapDescriptor> map_pins(Context context){
        ArrayList<BitmapDescriptor> pins = new ArrayList<>();
        pins.add(bitmapDescriptorFromVector(context, R.drawable.pin_theater));
        pins.add(bitmapDescriptorFromVector(context, R.drawable.pin_cinema));
        pins.add(bitmapDescriptorFromVector(context, R.drawable.pin_concert));
        pins.add(bitmapDescriptorFromVector(context, R.drawable.pin_club));
        pins.add(bitmapDescriptorFromVector(context, R.drawable.pin_other));
        return pins;
    }
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
