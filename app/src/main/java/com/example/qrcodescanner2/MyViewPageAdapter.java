package com.example.qrcodescanner2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.qrcodescanner2.fragments.AdminFragment;
import com.example.qrcodescanner2.fragments.ManualFragment;
import com.example.qrcodescanner2.fragments.MapFragment;
import com.example.qrcodescanner2.fragments.ScanFragment;

public class MyViewPageAdapter extends FragmentStateAdapter {

    public MyViewPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                return new ScanFragment();
            case 1:
                return new ManualFragment();
            case 2:
                return new MapFragment();
            case 3:
                return new AdminFragment();
            default:
                return new ScanFragment();
        }

    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
