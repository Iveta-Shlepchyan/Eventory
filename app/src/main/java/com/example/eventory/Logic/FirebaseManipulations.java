package com.example.eventory.Logic;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventory.ContainerActivity;
import com.example.eventory.models.CardModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.model.Document;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManipulations {

    //#TODO replace Tomsarkgh with AllEvents
    public static final List<String> paths = Arrays.asList("Theater","Clubs & pubs","Cinema","Concert","Other", "Tomsarkgh");
   /* public static final List<String> paths = Arrays.asList("Theater", "Opera", "Clubs","Cinema","Concert",
            "Entertainment","Tours","Interesting places","Museums","Conference","Other","Tomsarkgh");*/

    public static void addToFirebase(CardModel event){
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference eventsRef = db.collection("Tomsarkgh");
            addingToFirebase(event, eventsRef);

            boolean match = false;

            for (String path:event.getTags()) {

                if(paths.contains(path)) {
                    eventsRef = db.collection(path);
                    addingToFirebase(event, eventsRef);
                    match = true;

                }
            }
            if(!match)eventsRef = db.collection("Other");
            addingToFirebase(event, eventsRef);

        }catch (Exception exception){
            Log.e("addToFirebase", "failed "+ exception.getMessage() + " link: " + event.getLink());
        }

    }

    public static void addingToFirebase(CardModel event, CollectionReference eventsRef){
        eventsRef.document(event.getName()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {

                        try {
                            eventsRef.document(event.getName()).set(event);
                        } catch (Exception e) {
                            Log.e("addToFirebase", "event setting failed");
                        }
                    }
                    else {
                        //FIXME update base
                        eventsRef.document(event.getName()).set(event);
                    }
                }
            }
        });
    }

    public static void removePassedEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Date currentDate = new Date();

        for (String path : paths) {
           db.collection(path).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            CardModel cardModel = document.toObject(CardModel.class);
                            ArrayList<Date> dates = cardModel.getDates();

                            if (dates != null) {
                                List<Date> updatedDates = new ArrayList<>();
                                for (Date date : dates) {
                                    if (date.after(currentDate)) {
                                        updatedDates.add(date);
                                    }
                                }
                                if (updatedDates.isEmpty()) {
                                    document.getReference().delete();
                                } else {
                                    document.getReference().update("dates", updatedDates);
                                }
                            }
                        }
                    } else {
                        Log.e("Firebase / removePassedEvents", task.getException().getMessage());
                    }
                }
            });
        }
    }

    public static void startRemovingPassedEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference controlDocRef = db.collection("control").document("removePassedEvents");

        controlDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Date lastTimestamp = documentSnapshot.getTimestamp("timestamp").toDate();

                Calendar lastCal = Calendar.getInstance();
                lastCal.setTime(lastTimestamp);

                Calendar todayCal = Calendar.getInstance();
                todayCal.set(Calendar.HOUR_OF_DAY, 0);
                todayCal.set(Calendar.MINUTE, 0);
                todayCal.set(Calendar.SECOND, 0);
                todayCal.set(Calendar.MILLISECOND, 0);

                if (!lastCal.after(todayCal)) {
                    // Call a function if it hasn't been called today (by a random user)
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            removePassedEvents();
                        }
                    };
                    Thread secThread = new Thread(runnable);
                    secThread.start();

                    controlDocRef.update("timestamp", todayCal.getTime());
                }
            }
        });
    }


    public static void getEvent(String eventName, String category, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventDocRef = db.collection(category).document(eventName);
        eventDocRef.get().addOnCompleteListener(onCompleteListener);
    }

    /*public static void updateFavs(String json) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference favoritesCollection = db.collection("User's favorites");
            favoritesCollection.document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Update the existing document with the new JSON string
                                favoritesCollection.document(userId)
                                        .update("favorites", json)
                                        .addOnSuccessListener(aVoid -> Log.d("Firebase / update favorites", "Document updated successfully"))
                                        .addOnFailureListener(e -> Log.e("Firebase / update favorites", "Error updating document", e));
                            } else {
                                // Create a new document with the JSON string
                                Map<String, String> data = new HashMap<>();
                                data.put("favorites", json);
                                favoritesCollection.document(userId)
                                        .set(data)
                                        .addOnSuccessListener(aVoid -> Log.d("Firebase / update favorites", "Document created successfully"))
                                        .addOnFailureListener(e -> Log.e("Firebase / update favorites", "Error creating document", e));
                            }
                        } else {
                            Log.e("Firebase / update favorites", "Error getting document", task.getException());
                        }
                    });
        }
    }*/

    public static void updateFavs(String json) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference favoritesCollection = db.collection("User's favorites");

            Map<String, String> data = new HashMap<>();
            data.put("favorites", json);
            favoritesCollection.document(userId).delete();
            favoritesCollection.document(userId).set(data);
            Log.e("removed", "update");
            }
    }


    public static void getFavs() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DocumentReference docRef = db.collection("User's favorites").document(user.getUid()); // get a reference to the user's document
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // get the JSON data from the document
                        String json = documentSnapshot.get("favorites").toString();
                        Type listType = new TypeToken<List<CardModel>>() {}.getType();
                        Gson gson = new Gson();
                        ContainerActivity.likedCards = gson.fromJson(json, listType);
                        // use the JSON data as needed
                        Log.d("Firebase / get favorites", "JSON data: " + json);
                    } else {
                        Log.e("Firebase / get favorites", "No such document");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Firebase / get favorites", "Error getting document: " + e.getMessage());
                }
            });
        }
    }
}
