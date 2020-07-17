package com.coretal.carinspection.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.InspectionRecyclerViewAdapter;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class InspectionFragment extends Fragment {



    private Fragment truckInspectionFragment;
    private Fragment trailerInspectionFragment;

    private Fragment selectedFragment;

    public InspectionFragment() {
        // Required empty public constructor
    }

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
            transaction.add(R.id.inspection_frame_layout, fragment);
        }else{
            transaction.show(fragment);
        }

        selectedFragment = fragment;

        transaction.commit();

    }

    private Fragment getFragmentByPosition(int position) {
        switch (position) {
            case 0:
                return truckInspectionFragment;
            case 1:
                return trailerInspectionFragment;
            default:
                return null;
        }
    }

    public Fragment setFragmentWithPosition(int position) {
        switch (position) {
            case 0:
                truckInspectionFragment = TruckInspectionFragment.newInstance();
                return truckInspectionFragment;
            case 1:
                trailerInspectionFragment = TrailerInspectionFragment.newInstance();
                return trailerInspectionFragment;
            default:
                return null;
        }
    }

    public static InspectionFragment newInstance() {
        return new InspectionFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_inspection, container, false);

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
