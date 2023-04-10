package event.app.eventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import event.app.eventory.R;

import event.app.eventory.adapters.SwipeFragmentsAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class YourEventsActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_events);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageView backBtn = findViewById(R.id.backBtn);
        ImageView createEventBtn = findViewById(R.id.create_event_btn);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        String[] tabTitles = {"Live", "Past", "Draft"};

        createEventBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateEventActivity.class));
        });

        SwipeFragmentsAdapter adapter = new SwipeFragmentsAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager2.setAdapter(adapter);


        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            tab.select();
            tab.setText(tabTitles[position]);
        }).attach();

        backBtn.setOnClickListener(v -> {
            finish();
        });

    }
}
