package com.example.eventory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.eventory.Logic.Filter;
import com.example.eventory.Logic.FirebaseManipulations;
import com.example.eventory.adapters.CategoryAdapter;
import com.example.eventory.models.CardModel;
import com.example.eventory.models.CategoryModel;
import com.example.eventory.models.SerializableGeoPoint;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ContainerActivity.IOnBackPressed {

    FirebaseFirestore db ;

    RecyclerView recContainer;

    private ProgressBar loadingPB;

    @Override
    public boolean onBackPressed() {
        return true;
    }

    public interface Callback {
        void onCallback(List<CardModel> cardModels);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        recContainer = root.findViewById(R.id.container_list);
        recContainer.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadingPB = root.findViewById(R.id.loadingPB);
        loadingPB.setVisibility(View.VISIBLE);
        placeInCenter(loadingPB);

        buildItemList();


        TextView search = root.findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(getContext(), ViewAllActivity.class));
            }
        });




        return root;
    }


    private void buildItemList() {
        List<CategoryModel> categoryModelList = new ArrayList<>();
        for (String path: FirebaseManipulations.paths) {
            buildSubItemList(path, new Callback() {
                @Override
                public void onCallback(List<CardModel> cardModels) {
                    CategoryModel categoryModel = new CategoryModel(path, cardModels);
                    categoryModelList.add(categoryModel);
                    if(categoryModelList.size() == FirebaseManipulations.paths.size()){
                        for (int i = categoryModelList.size() - 1; i >= 0; i--) {
                            if (categoryModelList.get(i).getCardModelList().isEmpty()) {
                                categoryModelList.remove(i);
                            }
                        }
                        recContainer.setAdapter(new CategoryAdapter(categoryModelList));
                    }
                    loadingPB.setVisibility(View.GONE);
                }
            });
        }


    }

    private void buildSubItemList(String path, Callback callback) {
        List<CardModel> cardModelList = new ArrayList<>();
        db.collection(path)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                    CardModel cardModel = document.toObject(CardModel.class);
                                    if(!ContainerActivity.likedCards.isEmpty()){
                                        for (CardModel likedCard: ContainerActivity.likedCards ) {
                                            if (likedCard.getName().equals(cardModel.getName()))
                                                cardModel.setLiked(true);
                                        }
                                    }
                                    cardModelList.add(cardModel);

                                    try {
                                        if(!cardModel.getTags().isEmpty())
                                            ContainerActivity.tags_set.addAll(cardModel.getTags());
                                        if(!cardModel.getLocation().isEmpty())
                                            ContainerActivity.locations_set.add(cardModel.getLocation());
                                        if(cardModel.getGeoPoint() != null) {
                                            GeoPoint geoPoint = cardModel.getGeoPoint();
                                            boolean match = false;
                                            for (SerializableGeoPoint gp: ContainerActivity.geo_points) {
                                                if(geoPoint.equals(gp.getGeoPoint())) {
                                                    match = true;
                                                    break;
                                                }
                                            }
                                            ArrayList<String> tags = new ArrayList<>(cardModel.getTags());
                                            tags.retainAll(FirebaseManipulations.paths);
                                            if(!match)ContainerActivity.geo_points.add(new SerializableGeoPoint(geoPoint, cardModel.getLocation(), tags.get(0)));
                                        }
                                    }catch (NullPointerException nullEx){
                                        Log.e("HomeFragment", cardModel.getName()+ " "+ nullEx.getMessage());
                                    }

                            }
                            callback.onCallback(cardModelList);
                        }
                        else {
                            Toast.makeText(getActivity(),"Error"+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void placeInCenter(ProgressBar loadingPB) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) loadingPB.getLayoutParams();
        layoutParams.topMargin = displayMetrics.heightPixels;
        loadingPB.setLayoutParams(layoutParams);
    }

}
