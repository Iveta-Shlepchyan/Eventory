package com.example.eventory;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventory.Logic.FirebaseManipulations;
import com.example.eventory.adapters.LikeAdapter;
import com.example.eventory.models.CardModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LikeFragment extends Fragment implements LikeAdapter.onChangeListener{

    private RecyclerView recyclerView;
    private LikeAdapter likeAdapter;
    private ConstraintLayout emptyFavs;

    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_like, container, false);
        recyclerView = root.findViewById(R.id.like_recView);
        emptyFavs = root.findViewById(R.id.empty_favs);

        if(ContainerActivity.likedCards.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            emptyFavs.setVisibility(View.VISIBLE);
        }



        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        likeAdapter = new LikeAdapter(getContext(), ContainerActivity.likedCards);
        likeAdapter.notifyDataSetChanged();
        likeAdapter.setOnNoItemListener(this);
        recyclerView.setAdapter(likeAdapter);
        recyclerView.setRecycledViewPool(viewPool);
        recyclerView.setHasFixedSize(true);

        //FirebaseManipulations.getFavs();

        /*FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DocumentReference userDocRef = db.collection("User's favorites").document(currentUser.getUid());
            userDocRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    Log.w("LikeFragment", "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String json = documentSnapshot.get("favorites").toString();
                    Log.d("LikeFragment", "Favorites: " + json);
                    Type listType = new TypeToken<List<CardModel>>() {
                    }.getType();
                    Gson gson = new Gson();
                    ContainerActivity.likedCards = gson.fromJson(json, listType);
                    likeAdapter.notifyDataSetChanged();

                } else {
                    Log.d("LikeFragment", "Current data: null");
                }
            });
        }*/

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        likeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChange() {
        if(ContainerActivity.likedCards.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyFavs.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyFavs.setVisibility(View.GONE);
        }
    }
}