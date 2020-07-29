package com.coretal.carinspection.fragments;


import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.MyHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Fragment vehicleDetailFragment;
    private Fragment driverDetailFragment;
    private Fragment vehicleDocFragment;
    private Fragment vehicleInfoFragment;

    private Fragment selectedFragment;

    TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            selectTab(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };
    private TabLayout tabLayout;

    private void selectTab(int position) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        if(selectedFragment != null) {
            transaction.hide(selectedFragment);
        }

        Fragment fragment = getFragmentByPosition(position);
        if(fragment == null){
            fragment = setFragmentWithPosition(position);
            transaction.add(R.id.home_frame_layout, fragment);
        }else{
            transaction.show(fragment);
        }

        selectedFragment = fragment;

        transaction.commit();

    }

    private Fragment getFragmentByPosition(int position) {
        switch (position) {
            case 0:
                return vehicleDetailFragment;
            case 1:
                return driverDetailFragment;
            case 2:
                return vehicleDocFragment;
            case 3:
                return vehicleInfoFragment;
            default:
                return null;
        }
    }

    public Fragment setFragmentWithPosition(int position) {
        switch (position) {
            case 0:
                vehicleDetailFragment = VehicleDetailFragment.newInstance();
                return vehicleDetailFragment;
            case 1:
                driverDetailFragment = DriverDetailFragment.newInstance();
                return driverDetailFragment;
            case 2:
                vehicleDocFragment = VehicleDocFragment.newInstance();
                return vehicleDocFragment;
            case 3:
                vehicleInfoFragment = VehicleInfoFragment.newInstance();
                return vehicleInfoFragment;
            default:
                return null;
        }
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        if (!MyHelper.isConnectedInternet(getActivity())){
            disableTab();
        }
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
        selectTab(0);

        return view;
    }

    private void disableTab(){
        LinearLayout tabStrip = (LinearLayout)tabLayout.getChildAt(0);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setEnabled(false);
        }
    }
}
