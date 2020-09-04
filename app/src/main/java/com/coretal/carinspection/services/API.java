package com.coretal.carinspection.services;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.request.SimpleMultiPartRequest;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.DateAndPicture;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.models.SubmissionFile;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.coretal.carinspection.utils.VolleyHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class API implements VolleyHelper.Callback {

    static public Context context = null;
    static public VolleyHelper volleyHelper;
    static public DBHelper dbHelper;
    static public MyPreference myPreference;
    static public Callback callback;
    static public ProgressDialog progressDialog;

    public interface Callback{
        public void onProcessImage(int number, String error);
    }

    public API(Context context, Callback callback) {
        this.context = context;
//        volleyHelper = new VolleyHelper(context, this);
        volleyHelper = new VolleyHelper(context);
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        this.callback = callback;
    }

    static public void submitInspection() {
        progressDialog.show();
        dbHelper = new DBHelper(context);
        myPreference = new MyPreference(context);
        List<Submission> submissions = dbHelper.getSubmissionsToSubmit();

        final Submission submission = submissions.get(submissions.size() - 1);
        Log.d("Kangtle", "started to submit submissions vPlate " + submission.vehiclePlate);
        Log.d("Kangtle", "will submit pictures first");

        VolleyHelper inspectionVolleyHelper = new VolleyHelper(context);
        JsonObjectRequest postInspectionDataRequest = new JsonObjectRequest(
            Request.Method.POST,
            Contents.API_SUBMIT_INSPECTION,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    progressDialog.hide();
                    Log.d("Kangtle", "API_SUBMIT_INSPECTION: " + response.toString());

                    submission.status = Submission.STATUS_SUBMITTED;
                    AlertHelper.message(context, "Success", "Successfully submitted");
                    myPreference.setSubmissionDate();
                    Log.d("Kangtle", "success to submit " + submission.vehiclePlate);
                    dbHelper.setSubmissionStatus(submission);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.hide();
                    Log.e("Kangtle", "API_SUBMIT_INSPECTION: onErrorResponse");

                    submission.failedCount ++;
//                                        submission.errorDetail = "API_SUBMIT_INSPECTION: onErrorResponse";
                    submission.status = Submission.STATUS_FAILED;
                    try {
                        String respStr = new String(error.networkResponse.data, "UTF-8");
                        JSONObject json = new JSONObject(respStr);
                        if (json.has("error")) {
                            callback.onProcessImage(0, json.optString("message"));
                        } else {
                            callback.onProcessImage(0, error.toString());
                        }
                    } catch (Exception e) {
                        callback.onProcessImage(0, e.toString());
                        e.printStackTrace();
                    }

                    Log.d("Kangtle", "failed to submit " + submission.vehiclePlate);
                    dbHelper.setSubmissionStatus(submission);
                }
            }
        ){
            @Override
            public byte[] getBody() {
                String inspectData = getSubmitInspectionData(submission);
                try {
                    return inspectData.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return inspectData.getBytes();
                }
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put(Contents.HEADER_KEY, Contents.TOKEN);
                return params;
            }
        };
        inspectionVolleyHelper.add(postInspectionDataRequest);
    }

    static public void uploadPicture(DateAndPicture item, String type) {

        Log.d("Kangtle", "Upload Picture " + item.pictureId);
        progressDialog.show();
        SimpleMultiPartRequest multiPartRequest = new SimpleMultiPartRequest(
                Request.Method.POST,
                Contents.API_SUBMIT_PICTURE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.hide();
                        Log.d("Kangtle", "API_SUBMIT_PICTURE: " + response);
                        try {
                            JSONObject json = new JSONObject(response);
                            callback.onProcessImage(json.optInt("message"), "");
                        } catch (JSONException e) {
                            callback.onProcessImage(0, "");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Kangtle", "API_SUBMIT_PICTURE: onErrorResponse " + error.toString());
                progressDialog.hide();
                try {
                    String respStr = new String(error.networkResponse.data, "UTF-8");
                    JSONObject json = new JSONObject(respStr);
                    if (json.has("error")) {
                        callback.onProcessImage(0, json.optString("message"));
                    } else {
                        callback.onProcessImage(0, error.toString());
                    }
                } catch (Exception e) {
                    callback.onProcessImage(0, e.toString());
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put(Contents.HEADER_KEY, Contents.TOKEN);
                return params;
            }
        };

        multiPartRequest.addStringParam("pictureId", item.pictureId);
        multiPartRequest.addStringParam("phoneNumber", Contents.PHONE_NUMBER);
        multiPartRequest.addStringParam("pictureDate", item.dateStr);
        multiPartRequest.addStringParam("pictureType", item.type);
        if (type == Contents.JsonFileTypesEnum.CATEGORIE_DRIVER) { // DRIVERS
            multiPartRequest.addStringParam("ModuleEnum", "DRIVERS");
            multiPartRequest.addStringParam("plateOrId", Contents.DRIVER_ID);
        } else if (type == Contents.JsonFileTypesEnum.CATEGORIE_TRAILER) { // TRUCKS
            multiPartRequest.addStringParam("ModuleEnum", "TRUCKS");
            multiPartRequest.addStringParam("plateOrId", Contents.SECOND_VEHICLE_NUMBER);
        } else {
            multiPartRequest.addStringParam("ModuleEnum", "TRUCKS");
            multiPartRequest.addStringParam("plateOrId", Contents.CURRENT_VEHICLE_NUMBER);
        }
        multiPartRequest.addFile("file", item.pictureURL);

        volleyHelper.add(multiPartRequest);
    }

    static public void editPicture(DateAndPicture item, String type) {

        Log.d("Kangtle", "Edit Picture " + item.pictureId);
        progressDialog.show();
        SimpleMultiPartRequest multiPartRequest = new SimpleMultiPartRequest(
                Request.Method.POST,
                Contents.API_MODIFY_PICTURE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.hide();
                        Log.d("Kangtle", "API_MODIFY_PICTURE: " + response);
                        try {
                            JSONObject json = new JSONObject(response);
                            callback.onProcessImage(json.optInt("message"), "");
                        } catch (JSONException e) {
                            callback.onProcessImage(0, "");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Kangtle", "API_MODIFY_PICTURE: onErrorResponse " + error.toString());
                progressDialog.hide();
                try {
                    String respStr = new String(error.networkResponse.data, "UTF-8");
                    JSONObject json = new JSONObject(respStr);
                    if (json.has("error")) {
                        callback.onProcessImage(0, json.optString("message"));
                    } else {
                        callback.onProcessImage(0, error.toString());
                    }
                } catch (Exception e) {
                    callback.onProcessImage(0, e.toString());
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put(Contents.HEADER_KEY, Contents.TOKEN);
                return params;
            }
        };

        if (item.oldPictureId.isEmpty()) {
            multiPartRequest.addStringParam("existingDocumentId", item.pictureId);
        } else {
            multiPartRequest.addStringParam("existingDocumentId", item.oldPictureId);
        }
        multiPartRequest.addStringParam("pictureId", item.pictureId);
        multiPartRequest.addStringParam("phoneNumber", Contents.PHONE_NUMBER);
        multiPartRequest.addStringParam("pictureDate", item.dateStr);
        multiPartRequest.addStringParam("pictureType", item.type);
        if (type == Contents.JsonFileTypesEnum.CATEGORIE_DRIVER) { // DRIVERS
            multiPartRequest.addStringParam("ModuleEnum", "DRIVERS");
            multiPartRequest.addStringParam("plateOrId", Contents.DRIVER_ID);
        } else if (type == Contents.JsonFileTypesEnum.CATEGORIE_TRAILER) { // TRUCKS
            multiPartRequest.addStringParam("ModuleEnum", "TRUCKS");
            multiPartRequest.addStringParam("plateOrId", Contents.SECOND_VEHICLE_NUMBER);
        } else {
            multiPartRequest.addStringParam("ModuleEnum", "TRUCKS");
            multiPartRequest.addStringParam("plateOrId", Contents.CURRENT_VEHICLE_NUMBER);
        }
        multiPartRequest.addFile("file", item.pictureURL);

        volleyHelper.add(multiPartRequest);
    }

    static public void removePicture(final DateAndPicture item) {
        progressDialog.show();
        JsonObjectRequest removeRequest = new JsonObjectRequest(
                Request.Method.POST,
                String.format(Contents.API_REMOVE_PICTURE, Contents.PHONE_NUMBER, item.pictureId),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.hide();
                        Log.d("Kangtle", "API_REMOVE_PICTURE: " + response);
                        callback.onProcessImage(0, "");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Kangtle", "API_REMOVE_PICTURE: onErrorResponse " + error.toString());
                        progressDialog.hide();
                        try {
                            String respStr = new String(error.networkResponse.data, "UTF-8");
                            JSONObject json = new JSONObject(respStr);
                            if (json.has("error")) {
                                callback.onProcessImage(0, json.optString("message"));
                            } else {
                                callback.onProcessImage(0, error.toString());
                            }
                        } catch (Exception e) {
                            callback.onProcessImage(0, "Error occured");
                            e.printStackTrace();
                        }
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
        volleyHelper.add(removeRequest);
    }

    static public String getSubmitInspectionData(Submission submission){
        String jsonDir = context.getExternalFilesDir(submission.vehiclePlate) + "/" + Contents.EXTERNAL_JSON_DIR;

        JSONObject truckInspectionDataJson = JsonHelper.readJsonFromFile(jsonDir + "/" + Contents.JsonTruckInspectionJson.FILE_NAME);
        JSONObject trailerInspectionDataJson = JsonHelper.readJsonFromFile(jsonDir + "/" + Contents.JsonTrailerInspectionJson.FILE_NAME);
        JSONObject driverDataJson = JsonHelper.readJsonFromFile(jsonDir + "/" + Contents.JsonVehicleDriverData.FILE_NAME);
        JSONObject vehicleDataJson = JsonHelper.readJsonFromFile(jsonDir + "/" + Contents.JsonVehicleData.FILE_NAME);
        JSONObject dateAndPicturesJson = JsonHelper.readJsonFromFile(jsonDir + "/" + Contents.JsonDateAndPictures.FILE_NAME);
        JSONObject trailerDataJson = JsonHelper.readJsonFromFile(jsonDir + "/" + Contents.JsonVehicleTrailerData.FILE_NAME);

        JSONArray dateAndPicturesArray = null;
        if(dateAndPicturesJson != null){
            dateAndPicturesArray = dateAndPicturesJson.optJSONArray(Contents.JsonDateAndPictures.DATES_AND_PICTURES);
            fixDateAndPictureStatus(dateAndPicturesArray);
        }

        if(driverDataJson != null){
            fixDateAndPictureStatus(driverDataJson.optJSONArray(Contents.JsonDateAndPictures.DATES_AND_PICTURES));
        }

        if(trailerDataJson != null){
            fixDateAndPictureStatus(trailerDataJson.optJSONArray(Contents.JsonDateAndPictures.DATES_AND_PICTURES));
        }

        JSONObject submitData = new JSONObject();

        try {
            String notesPictureID = dbHelper.getPictureId(submission.id, "INSPECTION_NOTES_HAND_WRITING");
            String driverSigniturePictureId = dbHelper.getPictureId(submission.id, "INSPECTION_SIGNITURE_DRIVER");
            String inspectorSigniturePictureId = dbHelper.getPictureId(submission.id, "INSPECTION_SIGNITURE_INSPECTOR");

            JSONObject inspectionNotesObject = new JSONObject();
            inspectionNotesObject.put("note", submission.notes);
            inspectionNotesObject.put("notePictureId", notesPictureID);

            submitData.put("truckInspectionData", truckInspectionDataJson);
            if (Contents.IS_TRAILER_CHECKED)
                submitData.put("trailerInspectionData", trailerInspectionDataJson);
            submitData.put("driverData", driverDataJson);
            submitData.put("trailerData", trailerDataJson);
            submitData.put("vehicleData", vehicleDataJson);

//            submitData.put("datesAndPictures", dateAndPicturesArray);
            submitData.put("inspectionNotes", inspectionNotesObject);
            submitData.put("driverSigniturePictureId", driverSigniturePictureId);
            submitData.put("inspectorSigniturePictureId", inspectorSigniturePictureId);

            //For test
            String path = Contents.EXTERNAL_JSON_DIR_PATH + "/submitdata.json";
            JsonHelper.saveJsonObject(submitData, path);
            //================

            return submitData.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    static public void fixDateAndPictureStatus(JSONArray dateAndPictureArray){
        if (dateAndPictureArray == null) return;
        for (int i=0; i < dateAndPictureArray.length(); i++) {
            try {
                JSONObject jsonObject = dateAndPictureArray.getJSONObject(i);
                if (!jsonObject.has(Contents.JsonDateAndPictures.STATUS)){
                    jsonObject.putOpt(Contents.JsonDateAndPictures.STATUS, DateAndPicture.STATUS_NO_CHANED);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFinishedAllRequests() {
        Log.d("Kangtle", "Finished uploading picture");
    }
}
