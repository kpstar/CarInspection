package com.coretal.carinspection.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.VolleyHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrailerDetailFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Spinner trailerSpinner;
    private TextView typeLabel;
    private TextView subtypeLabel;
    private TextView detailsLabel;
    private EditText remarksEdit;

    private List<String> trailers;
    private String type;
    private String subtype;
    private String details;
    private String remarks;
    private String plate;
    private DateAndPictureFragment dateAndPictureFragment;
    private ProgressDialog progressDialog;
    private JSONArray dateAndPictures;

    public TrailerDetailFragment() {
        // Required empty public constructor
    }

    public static TrailerDetailFragment newInstance() {
        return new TrailerDetailFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trailer_detail, container, false);
        trailerSpinner = view.findViewById(R.id.trailer_spinner);
        typeLabel = view.findViewById(R.id.type);
        subtypeLabel = view.findViewById(R.id.subtype);
        detailsLabel = view.findViewById(R.id.details);
        remarksEdit = view.findViewById(R.id.remarks);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Getting trailer data");

        setValuesFromFile();

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("Kangtle", "on hidden trailer detail fragment");
        if (!Contents.IS_STARTED_INSPECTION) return;
        if(!hidden){
            setValuesFromFile();
        }else{
            saveValuesToFile();
        }
    }

    private void saveValuesToFile() {
        if(dateAndPictureFragment == null) return;
        JSONObject driverJsonObject = new JSONObject();
        try {
            plate = trailers.get(trailerSpinner.getSelectedItemPosition());

            remarks = remarksEdit.getText().toString();

            driverJsonObject.put(Contents.JsonVehicleTrailerData.TRAILER_PLATE, plate);
            driverJsonObject.put(Contents.JsonVehicleTrailerData.TRAILER_TYPE, type);
            driverJsonObject.put(Contents.JsonVehicleTrailerData.TRAILER_SUBTYPE, subtype);
            driverJsonObject.put(Contents.JsonVehicleTrailerData.TRAILER_DETAILS, details);
            driverJsonObject.put(Contents.JsonVehicleTrailerData.REMARKS, remarks);
            driverJsonObject.put(Contents.JsonDateAndPictures.DATES_AND_PICTURES, dateAndPictureFragment.getOutput());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonHelper.saveJsonObject(driverJsonObject, Contents.JsonVehicleTrailerData.FILE_PATH);
    }

    public void setValuesFromFile(){
        if(!Contents.IS_STARTED_INSPECTION) return;

        trailers = Contents.JsonTrailers.getTrailers();
        trailers.add(0, "");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, trailers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trailerSpinner.setAdapter(adapter);

        JSONObject trailerDataJson = JsonHelper.readJsonFromFile(Contents.JsonVehicleTrailerData.FILE_PATH);
        if (trailerDataJson == null){
            plate = "";
            type = "";
            subtype = "";
            details = "";
            remarks = "";
            dateAndPictures = null;
        }else{
            try {
                plate = trailerDataJson.getString(Contents.JsonVehicleTrailerData.TRAILER_PLATE);
                type = trailerDataJson.getString(Contents.JsonVehicleTrailerData.TRAILER_TYPE);
                subtype = trailerDataJson.getString(Contents.JsonVehicleTrailerData.TRAILER_SUBTYPE);
                details = trailerDataJson.getString(Contents.JsonVehicleTrailerData.TRAILER_DETAILS);
                remarks = trailerDataJson.optString(Contents.JsonVehicleTrailerData.REMARKS);
                dateAndPictures = trailerDataJson.optJSONArray(Contents.JsonDateAndPictures.DATES_AND_PICTURES);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        trailerSpinner.setOnItemSelectedListener(null);
        trailerSpinner.setSelection(trailers.indexOf(plate));
        trailerSpinner.post(new Runnable() {
            @Override
            public void run() {
                trailerSpinner.setOnItemSelectedListener(TrailerDetailFragment.this);
            }
        });

        typeLabel.setText(type);
        subtypeLabel.setText(subtype);
        detailsLabel.setText(details);
        remarksEdit.setText(remarks);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (dateAndPictures == null){
            if (dateAndPictureFragment != null) {
                fragmentTransaction.remove(dateAndPictureFragment);
                dateAndPictureFragment = null;
            }
        }else{
            dateAndPictureFragment = DateAndPictureFragment.newInstance(Contents.JsonFileTypesEnum.CATEGORIE_TRAILER, dateAndPictures.toString());
            fragmentTransaction.replace(R.id.trailer_fagment_container, dateAndPictureFragment);
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
            String trailer = trailers.get(position);

            progressDialog.show();
            Log.d("Kangtle", "getting trailer data.");
            JsonObjectRequest getTrailerDataRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    String.format(Contents.API_GET_TRAILER, Contents.PHONE_NUMBER, trailer),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressDialog.hide();
                            if (!response.has("error")){
                                Log.d("Kangtle", "got trailer data successfully.");
                                JsonHelper.saveJsonObject(response, Contents.JsonVehicleTrailerData.FILE_PATH);
                                setValuesFromFile();
                            }else{
                                Log.d("Kangtle", "error while getting trailer data.");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Kangtle", "error while getting trailer data.");
                            progressDialog.hide();
                        }
                    }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put(Contents.HEADER_KEY, Contents.TOKEN);
                    return headers;
                }
            };
            VolleyHelper volleyHelper = new VolleyHelper(getContext());
            volleyHelper.add(getTrailerDataRequest);

        }else{
            FileHelper.deleteFile(Contents.JsonVehicleTrailerData.FILE_PATH);
            setValuesFromFile();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onPause() {
        super.onPause();
        saveValuesToFile();
    }

    @Override
    public void onStop() {
        super.onStop();
        progressDialog.dismiss();
    }
}
