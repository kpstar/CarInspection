package com.coretal.carinspection.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Config;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
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

    public MyPreference(Context context){
        this.context = context;
        this.configSP = PreferenceManager.getDefaultSharedPreferences(context);
        this.configEditor = configSP.edit();
    }

    public void setPhoneNumber(String phoneNumber){
        configEditor.putString(PHONE_NUMBER, phoneNumber).apply();
    }

    public String getPhoneNumber(){
        return configSP.getString(PHONE_NUMBER, "");
    }

    public String getAppNotesLayout(){
        return configSP.getString(Contents.Config.CONF_APP_NOTES_LAYOUT, "BOTH");
    }

    public String getAPIBaseURL(){
        return configSP.getString(Contents.Config.WS_CONFIG_URL, Contents.API_ROOT);
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

    private Map<String, ?> getAllConfigs(){
        return configSP.getAll();
    }

    public String getAllToString(){
        Map<String, ?> allConfigs = getAllConfigs();
        Gson gson = new Gson();
        return gson.toJson(allConfigs);
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

    public void restoreFromJSONObject(JSONObject jsonObject){
        try {
            Iterator<String> keys = jsonObject.keys();

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
                    configEditor.putString(key, (String) value).commit();
                }else{
                    configEditor.putString(key, value.toString()).commit();
                }
            }

            configEditor.putBoolean(IS_GETTED_CONFIG, true).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void restore(){
        String savedString = FileHelper.readStringFromFile(Contents.Config.FILE_PATH);
        try {
            restoreFromJSONObject(new JSONObject(savedString));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void reset(){

    }
}
