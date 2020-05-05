package es.clarify.clarify;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import es.clarify.clarify.Search.NfcIdentifyFragment;
import es.clarify.clarify.Store.StoreFragment;

public class ViewPageAdapter extends FragmentStateAdapter {

    public HomeFragment homeFragment;
    public StoreFragment storeFragment;
    public NfcIdentifyFragment nfcIdentifyFragment;

    public ViewPageAdapter(@NonNull FragmentActivity fragmentActivity, HomeFragment homeFragment, StoreFragment storeFragment, NfcIdentifyFragment nfcIdentifyFragment) {
        super(fragmentActivity);

        this.homeFragment = homeFragment;
        this.storeFragment = storeFragment;
        this.nfcIdentifyFragment = nfcIdentifyFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return homeFragment;
            case 1:
                return storeFragment;
            default:
                return nfcIdentifyFragment;
        }
    }


    @Override
    public int getItemCount() {
        return 3;
    }
}
