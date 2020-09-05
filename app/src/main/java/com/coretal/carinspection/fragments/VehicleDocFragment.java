package com.coretal.carinspection.fragments;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.InspectionRecyclerViewAdapter;
import com.coretal.carinspection.adapters.VehicleDocAdapter;
import com.coretal.carinspection.dialogs.AddDriverDialog;
import com.coretal.carinspection.dialogs.ViewPDFDialog;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class VehicleDocFragment extends Fragment implements VehicleDocAdapter.Callback {

    MyPreference myPref;
    RadioGroup radioGroup;
    RadioButton btnTruck, btnTrailer;
    RecyclerView recDocs;
    ArrayList<VehicleDocAdapter.DocContent> docs;

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

        radioGroup = view.findViewById(R.id.toggle);
        btnTruck = view.findViewById(R.id.truck);
        btnTrailer = view.findViewById(R.id.trailer);
        recDocs = view.findViewById(R.id.recDocs);

        docs = new ArrayList<>();

        checkTruckType();

        btnTruck.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setValuesFromFile(true);
                }
            }
        });

        btnTrailer.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setValuesFromFile(false);
                }
            }
        });
        return view;
    }

    private void checkTruckType() {
        radioGroup.setVisibility(View.VISIBLE);
        if (Contents.SECOND_VEHICLE_NUMBER == null ) {
            setValuesFromFile(true);
            radioGroup.setVisibility(View.GONE);
            return;
        }
        switch (Contents.TRUCK_TYPE) {
            case 1:
                if (Contents.SECOND_VEHICLE_NUMBER.isEmpty()) {
                    radioGroup.setVisibility(View.GONE);
                }
                setValuesFromFile(true);
                break;
            case 2:
                radioGroup.setVisibility(View.GONE);
                setValuesFromFile(true);
                break;
            default:
                radioGroup.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            checkTruckType();
        }
    }

    private void setValuesFromFile(boolean isTruck){
        if(!Contents.IS_STARTED_INSPECTION) return;
        JSONArray vehicleDocJson;
        docs =  new ArrayList<>();
        VehicleDocAdapter.DocContent header = new VehicleDocAdapter.DocContent(getContext().getString(R.string.inspection_id), getContext().getString(R.string.inspection_month), getContext().getString(R.string.inspection_type));
        docs.add(header);
        if (isTruck) {
            vehicleDocJson = JsonHelper.readJsonArrayFromFile(Contents.JsonVehicleInspect.FILE_PATH);
        } else {
            vehicleDocJson = JsonHelper.readJsonArrayFromFile(Contents.JsonVehicleInspect.SEC_FILE_PATH);
        }

        if (vehicleDocJson == null) return;
        for (int i=0;i<vehicleDocJson.length(); i++){
            JSONObject jsonObject = null;
            try {
                jsonObject = vehicleDocJson.getJSONObject(i);
                int id = jsonObject.getInt(Contents.JsonVehicleInspect.INSPECTIONID);
                String month = jsonObject.getString(Contents.JsonVehicleInspect.INSPECTIONDATE).substring(0, 3);
                String type = jsonObject.getString(Contents.JsonVehicleInspect.INSPECTIONTYPE);

                VehicleDocAdapter.DocContent content = new VehicleDocAdapter.DocContent(String.format("%d", id), month, type);
                docs.add(content);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        VehicleDocAdapter adapter = new VehicleDocAdapter(getContext(), docs, this);
        LinearLayoutManager linearManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recDocs.setLayoutManager(linearManager);
        recDocs.setItemAnimator(new DefaultItemAnimator());
        recDocs.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClickItem(int position) {
        DialogFragment fragment = ViewPDFDialog.newInstance(docs.get(position));
        fragment.show(getFragmentManager(), "view_pdf_dialog");
    }
}