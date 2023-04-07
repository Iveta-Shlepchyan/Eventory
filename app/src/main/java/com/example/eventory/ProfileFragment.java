package com.example.eventory;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    Button logout_btn, your_events_btn, signUpBtn;
    ConstraintLayout guestLayout;
    LinearLayout profileLayout;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        guestLayout = rootView.findViewById(R.id.guest_layout);
        profileLayout = rootView.findViewById(R.id.profile_layout);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(mUser!=null){
            guestLayout.setVisibility(View.GONE);
            profileLayout.setVisibility(View.VISIBLE);
        }

        your_events_btn = rootView.findViewById(R.id.your_events);
        logout_btn = rootView.findViewById(R.id.logout_btn);

        logout_btn.setOnClickListener(v -> {
            logout();
        });
        your_events_btn.setOnClickListener(v -> {
            startActivity(new Intent(this.getActivity(), YourEventsActivity.class));
        });

        signUpBtn = rootView.findViewById(R.id.signUpBtn);

        signUpBtn.setOnClickListener(v -> {
            Intent i = new Intent(this.getActivity(),RegisterActivity.class);
            startActivity(i);
        });

        return rootView;
    }

    private void logout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        googleSignInClient.revokeAccess();
        FirebaseAuth.getInstance().signOut();
        guestLayout.setVisibility(View.VISIBLE);
        profileLayout.setVisibility(View.GONE);
    }

}