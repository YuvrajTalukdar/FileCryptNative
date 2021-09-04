package com.yuvraj.filecryptnative;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class fragment_adapter extends FragmentStateAdapter {
    public fragment_adapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position)
        {
            case 0:
                return new vault_fragment();
            case 1:
                return new explore_vault_fragment();
        }
        return new vault_fragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
