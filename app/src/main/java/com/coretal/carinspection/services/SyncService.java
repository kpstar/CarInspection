package com.coretal.carinspection.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.request.SimpleMultiPartRequest;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.DateAndPicture;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.models.SubmissionFile;
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

public class SyncService extends Service {
    private DBHelper dbHelper;
    private MyPreference myPreference;
    private int taskCount = 0;

    public static boolean isRunning = false;

    public SyncService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, new Notification());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Kangtle", "started to submit submissions");
        dbHelper = new DBHelper(this);
        myPreference = new MyPreference(this);
        List<Submission> submissions = dbHelper.getSubmissionsToSubmit();
        for (final Submission submission : submissions) {
            if (submission.numTry >= myPreference.get_conf_service_max_retry()){
                Log.d("Kangtle", "Submission num try is already reached to CONF_SERVICE_MAX_RETRY. vehiclePlate: " + submission.vehiclePlate);
                continue;
            }
            Log.d("Kangtle", "started to submit submissions vPlate " + submission.vehiclePlate);
            Log.d("Kangtle", "will submit pictures first");
            VolleyHelper pictureVolleyHelper = new VolleyHelper(this, new VolleyHelper.Callback() {
                @Override
                public void onFinishedAllRequests() {
                    if (submission.failedCount > 0){
                        submission.status = Submission.STATUS_FAILED;
                        Log.d("Kangtle", "failed to submit pictures for " + submission.vehiclePlate);
                        dbHelper.setSubmissionStatus(submission);
                    }else{
                        Log.d("Kangtle", "successed to submit pictures for " + submission.vehiclePlate);
                        Log.d("Kangtle", "will submit inspection data");
                        VolleyHelper inspectionVolleyHelper = new VolleyHelper(SyncService.this);
                        JsonObjectRequest postInspectionDataRequest = new JsonObjectRequest(
                                Request.Method.POST,
                                Contents.API_SUBMIT_INSPECTION,
                                null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("Kangtle", "API_SUBMIT_INSPECTION: " + response.toString());

                                        submission.status = Submission.STATUS_SUBMITTED;
                                        Log.d("Kangtle", "success to submit " + submission.vehiclePlate);
                                        dbHelper.setSubmissionStatus(submission);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("Kangtle", "API_SUBMIT_INSPECTION: onErrorResponse");

                                        submission.failedCount ++;
                                        submission.errorDetail = "API_SUBMIT_INSPECTION: onErrorResponse";
                                        submission.status = Submission.STATUS_FAILED;

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
                }
            });

            List<SubmissionFile> submissionFiles = dbHelper.getFilesForSubmissionId(submission.id);

            for(final SubmissionFile submissionFile: submissionFiles){
                if(!FileHelper.exists(submissionFile.fileLocation)){
                    Log.e("Kangtle", "Submission File " + submissionFile.fileLocation + " not exist");
                }
                SimpleMultiPartRequest multiPartRequest = new SimpleMultiPartRequest(
                    Request.Method.POST,
                    Contents.API_SUBMIT_PICTURE,
                    new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("Kangtle", "API_SUBMIT_PICTURE: " + submissionFile.pictureId + response);
                            }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Kangtle", "API_SUBMIT_PICTURE: onErrorResponse " + submissionFile.pictureId);
                            submission.failedCount ++;
                            submission.errorDetail = "API_SUBMIT_PICTURE: onErrorResponse " + submissionFile.pictureId;
                        }
                    }
                );

                multiPartRequest.addFile("file", submissionFile.fileLocation);
                multiPartRequest.addStringParam("pictureId", submissionFile.pictureId);

                pictureVolleyHelper.add(multiPartRequest);
            }
        }

        return START_STICKY;
    }

    private String getSubmitInspectionData(Submission submission){
        String jsonDir = getExternalFilesDir(submission.vehiclePlate) + "/" + Contents.EXTERNAL_JSON_DIR;

        JSONObject inspectionDataJson = JsonHelper.readJsonFromFile(jsonDir + "/" + Contents.JsonTruckInspectionJson.FILE_NAME);
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

            submitData.put("inspectionData", inspectionDataJson);
            submitData.put("driverData", driverDataJson);
            submitData.put("trailerData", trailerDataJson);
            submitData.put("vehicleData", vehicleDataJson);

            submitData.put("datesAndPictures", dateAndPicturesArray);
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

    private void fixDateAndPictureStatus(JSONArray dateAndPictureArray){
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

    private void enterTask(){
        taskCount ++;
        isRunning = true;
    }

    private void leaveTask(){
        taskCount --;
        if (taskCount == 0){
            isRunning = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
