package event.app.eventory.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import event.app.eventory.YourEventsFragment;

public class SwipeFragmentsAdapter extends FragmentStateAdapter {


    public SwipeFragmentsAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = new YourEventsFragment();
        Bundle args = new Bundle();
        args.putString("liveFragment", "Tab " + (position + 1));
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
