package com.coretal.carinspection.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Config;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Kangtle_R on 12/30/2017.
 */

public class MyPreference {
    private SharedPreferences configSP;
    private SharedPreferences.Editor configEditor;

    private Context context;

    private static String IS_GETTED_CONFIG = "IS_GETTED_CONFIG";
    private static String PHONE_NUMBER = "PHONE_NUMBER";
    private static String GUID = "PELED_GUID";
    public static String VEHICLE_TYPE = "VEHICLE_TYPE";
    public static String SECOND_VEHICLE_PLATE = "SECOND_VEHICLE_PLATE";
    private static String API_ROOT = "API_ROOT";
    private static String SAVED_DATE = "LAST_SUBMIT_DATE";

    public MyPreference(Context context){
        this.context = context;
        this.configSP = PreferenceManager.getDefaultSharedPreferences(context);
        this.configEditor = configSP.edit();
    }

    public void setPhoneNumber(String phoneNumber){
        configEditor.putString(PHONE_NUMBER, phoneNumber).apply();
    }

    public void setGUID(String guid){
        configEditor.putString(GUID, guid).apply();
    }

    public void setCompanyId(int id){
        configEditor.putInt(Contents.JsonVehicleData.COMPANYID, id).apply();
    }

    public void setVehicleType(int id){
        configEditor.putInt(Contents.JsonVehicleData.INSPECTION_TYPE, id).apply();
    }

    public void setSecondPlate(String trailerId){
        configEditor.putString(SECOND_VEHICLE_PLATE, trailerId).apply();
    }

    public void setAddDriverJson(String driver){
        configEditor.putString(Contents.Config.CONF_ADD_DRIVER_JSON, driver).apply();
    }

    public void setAPIRoot(String rootURL) {
        configEditor.putString(Contents.Config.WS_CONFIG_URL, rootURL).apply();
    }

    public void setSubmissionDate() {
        configEditor.putLong(SAVED_DATE, System.currentTimeMillis()).apply();
    }

    public void setIsSetUrl() {
        configEditor.putBoolean(Contents.IS_SET_URL, true).apply();
    }

    public String getPhoneNumber(){
        return configSP.getString(PHONE_NUMBER, "");
    }

    public String getGUID(){
        return configSP.getString(GUID, "");
    }

    public String getTruckType(){
        return configSP.getString(VEHICLE_TYPE, "NO_TYPE");
    }

    public int getVehicleType(){
        return configSP.getInt(Contents.JsonVehicleData.INSPECTION_TYPE, 0);
    }

    public String getSecondVehiclePlate(){
        return configSP.getString(SECOND_VEHICLE_PLATE, "");
    }

    public String getAppNotesLayout(){
        return configSP.getString(Contents.Config.CONF_APP_NOTES_LAYOUT, "BOTH");
    }

    public String getAPIBaseURL(){
        return configSP.getString(Contents.Config.WS_CONFIG_URL, Contents.API_ROOT);
    }

    public boolean getURLSet(){
        return configSP.getBoolean(Contents.IS_SET_URL, false);
    }

    public String getAppHash(){
        return configSP.getString(Contents.Config.WS_HASH_KEY, null);
    }
    public Boolean isGettedConfig(){
        return configSP.getBoolean(IS_GETTED_CONFIG, false);
    }

    public int get_conf_app_days_due(){
        return configSP.getInt(Contents.Config.CONF_APP_DAYS_DUE, 0);
    }

    public String get_conf_app_image_source(){
        return configSP.getString(Contents.Config.CONF_APP_IMAGE_SOURCE, "BOTH");
    }

    public boolean get_conf_chek_box_submit(){
        return configSP.getBoolean(Contents.Config.CONF_CHEK_BOX_SUBMIT, false);
    }

    public String get_conf_email_user(){
        return configSP.getString(Contents.Config.CONF_EMAIL_USER, null);
    }

    public String get_conf_email_password(){
        return configSP.getString(Contents.Config.CONF_EMAIL_PASSWORD, null);
    }

    public String get_conf_email_subject(){
        return configSP.getString(Contents.Config.CONF_EMAIL_SUBJECT, null);
    }

    public String get_conf_email_target_email(){
        return configSP.getString(Contents.Config.CONF_EMAIL_TARGET_EMAIL, null);
    }

    public String get_conf_inspector_id(){
        return configSP.getString(Contents.Config.CONF_INSPECTOR_ID, null);
    }

    public String get_conf_inspector_name(){
        return configSP.getString(Contents.Config.CONF_INSPECTOR_NAME, null);
    }

    public int get_conf_service_sleep(){
        return configSP.getInt(Contents.Config.CONF_SERVICE_SLEEP, 10);
    }

    public int get_conf_service_max_retry(){
        return configSP.getInt(Contents.Config.CONF_SERVICE_MAX_RETRY, 20);
    }

    public int getColorButton(){
        return configSP.getInt(Contents.Config.CONF_APP_SCHEMA_COLOR_BUTTON, 0xff417EB0);
    }

    public int getCompanyId(){
        return configSP.getInt(Contents.JsonVehicleData.COMPANYID, 0);
    }

    public int getColorCheck(){
        return configSP.getInt(Contents.Config.CONF_APP_SCHEMA_COLOR_CHECK, 0xff00ff00);
    }

    public int getColorUncheck(){
        return configSP.getInt(Contents.Config.CONF_APP_SCHEMA_COLOR_UNCHECK, 0xffff0000);
    }

    public boolean canSubmit() {
        long savedDate = configSP.getLong(SAVED_DATE, 0);
        if ((savedDate + (24 * 60 * 60 * 1000)) < System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    public int getColorBackground(){
        return configSP.getInt(Contents.Config.CONF_APP_SCHEMA_COLOR_BACKGROUND, 0xffffffff);
    }

    public String[] get_conf_months(){
        String[] months = {"ינואר","פברואר","מרץ","אפריל","מאי","יוני","יולי","אוגוסט","ספטמבר","אוקטובר","נובמבר","דצמבר"};
        return months;
    }

    public String[] get_conf_truck_mandatory_documents(){
        String documentsString = configSP.getString(Contents.Config.CONF_TRUCK_MANDATORY_DOCUMENTS, "");
        return (documentsString.isEmpty()) ? new String[0] : documentsString.split(",");
    }

    public String[] get_conf_driver_mandatory_documents(){
        String documentsString = configSP.getString(Contents.Config.CONF_DRIVER_MANDATORY_DOCUMENTS, "");
        return (documentsString.isEmpty()) ? new String[0] : documentsString.split(",");
    }

    public String[] get_conf_trailer_mandatory_documents(){
        String documentsString = configSP.getString(Contents.Config.CONF_TRAILER_MANDATORY_DOCUMENTS, "");
        return (documentsString.isEmpty()) ? new String[0] : documentsString.split(",");
    }

    private Map<String, ?> getAllConfigs(){
        return configSP.getAll();
    }

    public String getAllToString(){
        Map<String, ?> allConfigs = getAllConfigs();
        Gson gson = new Gson();
        return gson.toJson(allConfigs);
    }

    public String getDriverJson() {
        String driverJson = configSP.getString(Contents.Config.CONF_ADD_DRIVER_JSON, "");
        return driverJson;
    }

    public String getConfigToFormatString(){
        String[] configs = {
                Contents.Config.CONF_EMAIL_USER,
                Contents.Config.CONF_EMAIL_PASSWORD,
                Contents.Config.CONF_EMAIL_SUBJECT,
                Contents.Config.CONF_EMAIL_TARGET_EMAIL,
                Contents.Config.CONF_INSPECTOR_ID,
                Contents.Config.CONF_INSPECTOR_NAME,
                Contents.Config.CONF_SERVICE_SLEEP,
                Contents.Config.CONF_SERVICE_MAX_RETRY,
                Contents.Config.CONF_SERVICE_ALLOW_CELL_DATA,
                Contents.Config.WS_CONFIG_URL,
                Contents.Config.WS_CONFIG_CONN_CHECK,
                Contents.Config.WS_CONFIG_CONN_CHECK_DELAY,
        };

        Map<String, ?> allConfigs = getAllConfigs();
        JSONObject jsonObject = new JSONObject();

        for (String config: configs) {
            Object val = allConfigs.get(config);
            try {
                jsonObject.put(config, val);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            return jsonObject.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void backup(){
        FileHelper.writeStringToFile(getAllToString(), Contents.Config.FILE_PATH);
    }

    public void restoreFromJSONObject(JSONObject jsonObject, String[] onlyRequiredFields){
        try {
            Iterator<String> keys = (onlyRequiredFields == null) ? jsonObject.keys() : Arrays.asList(onlyRequiredFields).iterator();

            while( keys.hasNext() ) {
                String key = keys.next();
                Object value = jsonObject.get(key);

                if (value instanceof Boolean){
                    configEditor.putBoolean(key, (Boolean)value).commit();
                }else if (value instanceof Integer){
                    configEditor.putInt(key, (Integer) value).commit();
                }else if (value instanceof Float){
                    configEditor.putFloat(key, (Float) value).commit();
                }else if (value instanceof Long){
                    configEditor.putFloat(key, (Long) value).commit();
                }else if (value instanceof String){
                    String valueStr = (String) value;
                    if (valueStr.startsWith("#")){
                        configEditor.putInt(key, Color.parseColor(valueStr)).commit();
                    }else{
                        configEditor.putString(key, valueStr).commit();
                    }
                }else{
                    configEditor.putString(key, value.toString()).commit();
                }
            }

            configEditor.putBoolean(IS_GETTED_CONFIG, true).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void resetOnlyRequiredFields(JSONObject response) {
        String[] requiredFields = {
            Contents.Config.CONF_TRUCK_MANDATORY_DOCUMENTS,
            Contents.Config.CONF_DRIVER_MANDATORY_DOCUMENTS,
            Contents.Config.CONF_TRAILER_MANDATORY_DOCUMENTS
        };
        restoreFromJSONObject(response, requiredFields);
    }

    public void restore(){
        String savedString = FileHelper.readStringFromFile(Contents.Config.FILE_PATH);
        try {
            restoreFromJSONObject(new JSONObject(savedString), null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void reset(){

    }
}
