package com.coretal.carinspection.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kangtle_R on 1/26/2018.
 */

public class JsonHelper {
    public static void saveJsonObject(JSONObject jsonObject, String path){
        if (jsonObject == null) {
            FileHelper.writeStringToFile("", path);
        } else {
            FileHelper.writeStringToFile(jsonObject.toString(), path);
        }
    }

    public static void saveJsonArray(JSONArray jsonArray, String path){
        FileHelper.writeStringToFile(jsonArray.toString(), path);
    }

    public static JSONObject readJsonFromFile(String path){
        String jsonString = FileHelper.readStringFromFile(path);
        if (jsonString.isEmpty()) return null;
        try {
            return new JSONObject(jsonString);
        } catch (Exception e) {
            Log.e("JsonHelper", "Failed to parse the json", e);
            return null;
        }
    }

    public static JSONArray readJsonArrayFromFile(String path){
        String jsonString = FileHelper.readStringFromFile(path);
        if (jsonString.isEmpty()) return null;
        try {
            return new JSONArray(jsonString);
        } catch (Exception e) {
            Log.e("JsonHelper", "Failed to parse the json", e);
            return null;
        }
    }

    public static JSONObject readJsonFromAsset(String fileName){
        String jsonString = FileHelper.readStringFromAsset(fileName);
        try {
            return new JSONObject(jsonString);
        } catch (Exception e) {
            Log.e("JsonHelper", "Failed to parse the json", e);
            return null;
        }
    }
}
