package com.coretal.carinspection.fragments;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.InspectionRecyclerViewAdapter;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.github.barteksc.pdfviewer.PDFView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VehicleDocFragment extends Fragment {

    MyPreference myPref;
    PDFView pdfView;

    public VehicleDocFragment() {
        // Required empty public constructor
    }

    public static VehicleDocFragment newInstance() {
        return new VehicleDocFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myPref = new MyPreference(getContext());
        View view = inflater.inflate(R.layout.fragment_vehicle_doc, container, false);
        pdfView = view.findViewById(R.id.pdfVehicle);
        setValuesFromFile();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("Kangtle", "on hidden vehicle doc fragment");
        if (!Contents.IS_STARTED_INSPECTION) return;
        if(!hidden){
            setValuesFromFile();
        }
    }

    private void createDocument() {
        PdfDocument doc = new PdfDocument();
    }


    private void setValuesFromFile(){
        if(!Contents.IS_STARTED_INSPECTION) return;
        JSONArray vehicleDocJson = JsonHelper.readJsonArrayFromFile(Contents.JsonVehicleInspect.FILE_PATH);

        if (vehicleDocJson == null) return;
        pdfView.fromBytes(vehicleDocJson.toString().getBytes()).defaultPage(1).load();
//        for (int sectionIndex=0; sectionIndex < vehicleDocJson.length(); sectionIndex++){
//
//        }
    }

}