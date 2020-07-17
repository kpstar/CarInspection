package com.coretal.carinspection.utils;

import android.content.Context;
import android.os.Environment;

import com.coretal.carinspection.BuildConfig;
import com.coretal.carinspection.MyApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kangtle_R on 1/26/2018.
 */

public class Contents {
    public static boolean IS_STARTED_INSPECTION = false;
    public static String API_ROOT =
            BuildConfig.DEBUG ?
                    "http://24.30.63.116:8080/Peled_v6/restful" :
                    "http://peled.co/Peled_v6/restful";
    public static String API_REGISTER_DEVICE = API_ROOT + "/configuration/registerDevice/%s"; //phone_number
    public static String API_GET_VEHICLE_DATA = API_ROOT + "/vehicle/getVehicleData/%s/%s"; //phone_number/v_plate
    public static String API_GET_INSPECTORS = API_ROOT + "/inspector/getInspectors/%s";//phone_number
    public static String API_GET_DRIVERS = API_ROOT + "/driver/getDriversData/%s/%s";//phone_number/v_plate
    public static String API_GET_DRIVER = API_ROOT + "/driver/getDriverData/%s/%s";//phone_number/driver_id
    public static String API_GET_VEHICLE_DRIVER_DATA = API_ROOT + "/driver/getVhicleDriverData/%s/%s";//phone_number/v_plate
    public static String API_GET_TRAILERS = API_ROOT + "/trailer/getTrailersData/%s/%s";//phone_number/v_plate
    public static String API_GET_TRAILER = API_ROOT + "/trailer/getTrailerData/%s/%s";//phone_number/trailer_id
    public static String API_GET_VEHICLE_TRAILER_DATA = API_ROOT + "/trailer/getVhicleTrailerData/%s/%s";//phone_number/v_plate
    public static String API_GET_VEHICLE_ADDITIONAL_DETAILS = API_ROOT + "/vehicle/getAdditionalDetails/%s/%s";//phone_number/v_plate
    public static String API_GET_TRUCK_INSPECTION_JSON = API_ROOT + "inspection/getTruckInspectionJson/%s";//phone_number
    public static String API_GET_TRAILER_INSPECTION_JSON = API_ROOT + "inspection/getTrailerInspectionJson/%s";//phone_number
    public static String API_GET_DATE_AND_PICTURES = API_ROOT + "/vehicle/getVehicleDateAndPictureInfo/%s/%s";//phone_number/v_plate
    public static String API_GET_PICTURE_BY_ID = API_ROOT + "/image/getPictureById/%s/%s";//phone_number/picture id
    public static String API_GET_CONFIG = API_ROOT + "/configuration/getConfigurationFile/%s";//phone number
    public static String API_GET_CONFIG_FILE_TYPES_EMUM = API_ROOT + "/configuration/getPictureAndData/%s";//phone number
    public static String API_SUBMIT_PICTURE = API_ROOT + "/submitPicture/fileupload";
    public static String API_SUBMIT_INSPECTION = API_ROOT + "/submitInspection";
    public static String API_SERVICE_STATUS = API_ROOT + "/serviceStatus";

    public static String EXTERNAL_JSON_DIR = "Json";
    public static String EXTERNAL_JSON_DIR_PATH;
    public static String EXTERNAL_PICTURES_DIR_PATH;
    public static String PHONE_NUMBER;
    public static String TOKEN;
    public static String CURRENT_VEHICLE_NUMBER;

    public static String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    public static String DATE_PREFIX = "#";
    public static String TOKEN_KEY = "registerDevice";
    public static String HEADER_KEY = "peled-guid";

    public static Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("key", "Value");
        return headers;
    }

    public static class JsonVehicleData {
        public static String VEHICLE_PLATE = "vehiclePlate";
        public static String TYPE = "vehicleType";
        public static String SUBTYPE = "vehicleSubType";
        public static String DETAILS = "vehicleDetails";
        public static String CURRENTODOMETER = "currentOdometer";
        public static String DRIVERID = "driverId";
        public static String DRIVERNAME = "driverName";
        public static String COMPANYNAME = "companyName";
        public static String DATESANDPICTURES = "datesAndPictures";
        public static String INSPECTION_ID = "inspectorId";
        public static String INSPECTION_NAME = "inspectorName";
        public static String INSPECTION_MONTH = "inspectionMonth";
        public static String INSPECTION_DATE = "inspectionDate";
        public static String INSPECTION_VALID_UNTIL_DATE = "inspectionValidUntilDate";
        public static String INSPECTION_LOCATION = "inspectionLocation";
        public static String VEHICLE_MAKE = "vehicleMake";
        public static String VEHICLE_MAKE_ID = "vehicleMakeId";
        public static String FILE_NAME = "vehicle_data.json";
        public static String FILE_PATH;
    }

    public static class JsonInspectors {
        public static String INSPECTORSDATA = "inspectorsData";
        public static String INSPECTORS = "inspector";
        public static String INSPECTOR_ID = "id";
        public static String INSPECTOR_NAME = "name";
        public static String FILE_NAME = "inspectors.json";
        public static String FILE_PATH;

        public static Map<String, String> getInspectors(){
            Map<String, String> inspectors = new LinkedHashMap<>();
            JSONObject inspectorsJson = JsonHelper.readJsonFromFile(FILE_PATH);
            if (inspectorsJson == null) return inspectors;
            try {
                JSONArray driversArray = inspectorsJson.getJSONArray(INSPECTORSDATA);
                if (driversArray != null) {
                    for (int i=0;i<driversArray.length();i++){
                        JSONObject jsonObject = driversArray.getJSONObject(i);
                        String id = jsonObject.getString(INSPECTOR_ID);
                        String name = jsonObject.getString(INSPECTOR_NAME);
                        inspectors.put(id, name);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return inspectors;
        }
    }

    public static class JsonDrivers {
        public static String DRIVERSDATA = "driversData";
        public static String DRIVERS = "drivers";
        public static String DRIVER_ID = "id";
        public static String DRIVER_NAME = "name";
        public static String FILE_NAME = "drivers.json";
        public static String FILE_PATH;

        public static Map<String, String> getDrivers(){
            Map<String, String> drivers = new LinkedHashMap<>();
            JSONObject driversJson = JsonHelper.readJsonFromFile(FILE_PATH);
            if (driversJson == null) return drivers;
            try {
                JSONArray driversArray = driversJson.getJSONArray(DRIVERSDATA);
                if (driversArray != null) {
                    for (int i=0;i<driversArray.length();i++){
                        JSONObject jsonObject = driversArray.getJSONObject(i);
                        String driverId = jsonObject.getString(DRIVER_ID);
                        String driverName = jsonObject.getString(DRIVER_NAME);
                        drivers.put(driverId, driverName);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return drivers;
        }
    }

    public static class JsonVehicleDriverData {
        public static String DRIVER_ID = "driverId";
        public static String FULL_NAME = "fullName";
        public static String DRIVER_LICENSE_NUMBER = "driverLicenseNumber";
        public static String DRIVER_LICENSE_DATE = "driverLicenceDate";
        public static String DRIVER_HATZHARATNAHAG_DATE = "driverHatzharatNahagDate";
        public static String DRIVER_HOMAS_DATE = "driverHomasDate";
        public static String DRIVER_MANOF_DATE = "driverManofDate";
        public static String DRIVER_IS_KAVUA = "driverIsKavua";
        public static String DRIVER_ADDRESS = "driverAddress";
        public static String REMARKS = "remarks";
        public static String FILE_NAME = "vehicle_driver_data.json";
        public static String FILE_PATH;
    }

    public static class JsonTrailers {
        public static String TRAILERSSDATA = "trailersData";
        public static String TRAILERS = "trailers";
        public static String PLATE = "plate";
        public static String FILE_NAME = "trailers.json";
        public static String FILE_PATH;

        public static List<String> getTrailers(){
            ArrayList<String> trailers = new ArrayList<String>();
            JSONObject trailersJson = JsonHelper.readJsonFromFile(FILE_PATH);
            if (trailersJson == null) return trailers;
            try {
                JSONArray driversArray = trailersJson.getJSONArray(TRAILERSSDATA);

                if (driversArray != null) {
                    for (int i=0;i<driversArray.length();i++){
                        trailers.add(driversArray.getJSONObject(i).getString(PLATE));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return trailers;
        }
    }

    public static class JsonVehicleTrailerData {
        public static String TRAILER_PLATE = "trailerPlate";
        public static String TRAILER_DETAILS = "trailerDetails";
        public static String TRAILER_TYPE = "trailerType";
        public static String TRAILER_SUBTYPE = "trailerSubType";
        public static String REMARKS = "remarks";
        public static String FILE_NAME = "vehicle_trailer_data.json";
        public static String FILE_PATH;
    }

    public static class VehicleAdditionalDetails {
        public static String FILE_NAME = "vehicle_additional_details.txt";
        public static String FILE_PATH;
    }

    public static class JsonTruckInspectionJson {
        public static String SECTIONS = "testSections";
        public static String IDENTIFIER = "identifier";
        public static String QUESTIONS = "questions";
        public static String NOTES = "notes";
        public static String ORDER = "order";
        public static String CAPTION = "caption";
        public static String STATUS = "checked"; //status == checked
        public static String ASSET_FILE_NAME = "full_truck_inspection_structure.json";
        public static String FILE_NAME = "truck_inspection_json.json";
        public static String FILE_PATH;
    }

    public static class JsonTrailerInspectionJson {
        public static String SECTIONS = "testSections";
        public static String IDENTIFIER = "identifier";
        public static String QUESTIONS = "questions";
        public static String NOTES = "notes";
        public static String ORDER = "order";
        public static String CAPTION = "caption";
        public static String STATUS = "checked"; //status == checked
        public static String ASSET_FILE_NAME = "full_trailer_inspection_structure.json";
        public static String FILE_NAME = "trailer_inspection_json.json";
        public static String FILE_PATH;
    }

    public static class JsonDateAndPictures {
        public static String DATES_AND_PICTURES = "datesAndPictures";
        public static String DATE = "date";
        public static String PICTUREID = "pictureId";
        public static String TYPE = "type";
        public static String STATUS = "status";
        public static String FILE_NAME = "date_and_pictures.json";
        public static String FILE_PATH;
    }

    public static class JsonFileTypesEnum {
        public static String CATEGORIES = "categories";
        public static String CATEGORIE_APP = "APP";
        public static String CATEGORIE_VEHICLE = "VEHICLE";
        public static String CATEGORIE_DRIVER = "DRIVER";
        public static String CATEGORIE_TRAILER = "TRAILER";
        public static String KEY = "KEY";
        public static String VALUE = "VALUE";
        public static String FILE_NAME = "file_types_enum.json";
        public static String FILE_PATH;

        public static Map<String, String> getAppTypes() {
            return getTypesByCategory(CATEGORIE_APP);
        }

        public static Map<String, String> getVehicleTypes() {
            return getTypesByCategory(CATEGORIE_VEHICLE);
        }

        public static Map<String, String> getDriverTypes() {
            return getTypesByCategory(CATEGORIE_DRIVER);
        }

        public static Map<String, String> getTrailerTypes() {
            return getTypesByCategory(CATEGORIE_TRAILER);
        }

        public static Map<String, String> getTypesByCategory(String category) {
            Map<String, String> types = new LinkedHashMap<>();
            JSONObject allTypesJson = JsonHelper.readJsonFromFile(FILE_PATH);
            if (allTypesJson == null) return types;
            try {
                JSONObject categoriesObject = allTypesJson.getJSONObject(CATEGORIES);
                JSONArray appTypesArray = categoriesObject.getJSONArray(category);
                for (int i = 0; i < appTypesArray.length(); i++) {
                    JSONObject typeObject = appTypesArray.getJSONObject(i);
                    String key = typeObject.getString(KEY);
                    String value = typeObject.getString(VALUE);
                    types.put(key, value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return types;
        }
    }

    public static class Config {

        public static final String CONF_APP_DAYS_DUE = "CONF_APP_DAYS_DUE";
        public static final String CONF_APP_NOTES_LAYOUT = "CONF_APP_NOTES_LAYOUT";
        public static final String CONF_APP_IMAGE_SOURCE = "CONF_APP_IMAGE_SOURCE";
        public static final String CONF_CHEK_BOX_SUBMIT = "CONF_CHEK_BOX_SUBMIT";
        public static final String CONF_APP_SCHEMA_COLOR = "CONF_APP_SCHEMA_COLOR";
        public static final String WS_CONFIG_ALLOW_OFFLINE = "WS_CONFIG_ALLOW_OFFLINE";
        public static final String WS_CONFIG_CONN_CHECK = "WS_CONFIG_CONN_CHECK";
        public static final String WS_CONFIG_CONN_CHECK_DELAY = "WS_CONFIG_CONN_CHECK_DELAY";
        public static final String WS_CONFIG_URL = "WS_CONFIG_URL";
        public static final String CONF_INSPECTOR_ID = "CONF_INSPECTOR_ID";
        public static final String CONF_INSPECTOR_NAME = "CONF_INSPECTOR_NAME";
        public static final String WS_HASH_KEY = "WS_HASH_KEY";

        public static final String CONF_SERVICE_SLEEP = "CONF_SERVICE_SLEEP";
        public static final String CONF_SERVICE_ALLOW_CELL_DATA = "CONF_SERVICE_ALLOW_CELL_DATA";
        public static final String CONF_SERVICE_MAX_RETRY = "CONF_SERVICE_MAX_RETRY";

        public static final String CONF_FILE_STORAGE_LOCATION = "CONF_FILE_STORAGE_LOCATION";

        public static final String CONF_EMAIL_USER = "CONF_EMAIL_USER";
        public static final String CONF_EMAIL_PASSWORD = "CONF_EMAIL_PASSWORD";
        public static final String CONF_EMAIL_SUBJECT = "CONF_EMAIL_SUBJECT";
        public static final String CONF_EMAIL_TARGET_EMAIL = "CONF_EMAIL_TARGET_EMAIL";

        public static final String CONF_APP_SCHEMA_COLOR_BUTTON = "CONF_APP_SCHEMA_COLOR_A";
        public static final String CONF_APP_SCHEMA_COLOR_CHECK = "CONF_APP_SCHEMA_COLOR_B";
        public static final String CONF_APP_SCHEMA_COLOR_UNCHECK = "CONF_APP_SCHEMA_COLOR_C";
        public static final String CONF_APP_SCHEMA_COLOR_BACKGROUND = "CONF_APP_SCHEMA_COLOR_D";

        public static final String CONF_TRUCK_MANDATORY_DOCUMENTS = "CONF_TRUCK_MANDATORY_DOCUMENTS";
        public static final String CONF_TRAILER_MANDATORY_DOCUMENTS = "CONF_TRAILER_MANDATORY_DOCUMENTS";
        public static final String CONF_DRIVER_MANDATORY_DOCUMENTS = "CONF_DRIVER_MANDATORY_DOCUMENTS";

        public static String FILE_NAME = "config.bkp";
        public static String FILE_PATH;
    }

    public static void setVehicleNumber(String vehicleNumber) {
        CURRENT_VEHICLE_NUMBER = vehicleNumber;
        EXTERNAL_JSON_DIR_PATH = MyApp.getContext().getExternalFilesDir(CURRENT_VEHICLE_NUMBER) + "/" + EXTERNAL_JSON_DIR;
        EXTERNAL_PICTURES_DIR_PATH = MyApp.getContext().getExternalFilesDir(CURRENT_VEHICLE_NUMBER) + "/" + Environment.DIRECTORY_PICTURES;

        JsonVehicleData.FILE_PATH = EXTERNAL_JSON_DIR_PATH + "/" + JsonVehicleData.FILE_NAME;
        JsonInspectors.FILE_PATH = EXTERNAL_JSON_DIR_PATH + "/" + JsonInspectors.FILE_NAME;
        JsonDrivers.FILE_PATH = EXTERNAL_JSON_DIR_PATH + "/" + JsonDrivers.FILE_NAME;
        JsonVehicleDriverData.FILE_PATH = EXTERNAL_JSON_DIR_PATH + "/" + JsonVehicleDriverData.FILE_NAME;
        JsonTrailers.FILE_PATH = EXTERNAL_JSON_DIR_PATH + "/" + JsonTrailers.FILE_NAME;
        JsonVehicleTrailerData.FILE_PATH = EXTERNAL_JSON_DIR_PATH + "/" + JsonVehicleTrailerData.FILE_NAME;
        VehicleAdditionalDetails.FILE_PATH = EXTERNAL_JSON_DIR_PATH + "/" + VehicleAdditionalDetails.FILE_NAME;
        JsonTrailerInspectionJson.FILE_PATH = EXTERNAL_JSON_DIR_PATH + "/" + JsonTrailerInspectionJson.FILE_NAME;
        JsonTruckInspectionJson.FILE_PATH = EXTERNAL_JSON_DIR_PATH + "/" + JsonTruckInspectionJson.FILE_NAME;
        JsonDateAndPictures.FILE_PATH = EXTERNAL_JSON_DIR_PATH + "/" + JsonDateAndPictures.FILE_NAME;
        JsonFileTypesEnum.FILE_PATH = MyApp.getContext().getExternalFilesDir(null) + "/" + JsonFileTypesEnum.FILE_NAME;
        Config.FILE_PATH = MyApp.getContext().getExternalFilesDir(null) + "/" + Config.FILE_NAME;
    }

    public static void configAPIs(Context context){
        MyPreference myPreference = new MyPreference(context);

        API_ROOT = myPreference.getAPIBaseURL();

        API_REGISTER_DEVICE = API_ROOT + "/configuration/registerDevice/%s"; //phone_number
        API_GET_VEHICLE_DATA = API_ROOT + "/vehicle/getVehicleData/%s/%s"; //phone_number/v_plate
        API_GET_INSPECTORS = API_ROOT + "/inspector/getInspectors/%s";//phone_number
        API_GET_DRIVERS = API_ROOT + "/driver/getDriversData/%s/%s";//phone_number/v_plate
        API_GET_DRIVER = API_ROOT + "/driver/getDriverData/%s/%s";//phone_number/driver_id
        API_GET_VEHICLE_DRIVER_DATA = API_ROOT + "/driver/getVhicleDriverData/%s/%s";//phone_number/v_plate
        API_GET_TRAILERS = API_ROOT + "/trailer/getTrailersData/%s/%s";//phone_number/v_plate
        API_GET_TRAILER = API_ROOT + "/trailer/getTrailerData/%s/%s";//phone_number/trailer_id
        API_GET_VEHICLE_TRAILER_DATA = API_ROOT + "/trailer/getVhicleTrailerData/%s/%s";//phone_number/v_plate
        API_GET_VEHICLE_ADDITIONAL_DETAILS = API_ROOT + "/vehicle/getAdditionalDetails/%s/%s";//phone_number/v_plate
        API_GET_TRUCK_INSPECTION_JSON = API_ROOT + "/inspection/getTruckInspectionJson/%s";//phone_number
        API_GET_TRAILER_INSPECTION_JSON = API_ROOT + "/inspection/getTrailerInspectionJson/%s";//phone_number
        API_GET_DATE_AND_PICTURES = API_ROOT + "/vehicle/getVehicleDateAndPictureInfo/%s/%s";//phone_number/v_plate
        API_GET_PICTURE_BY_ID = API_ROOT + "/image/getPictureById/%s/%s";//phone_number/picture id
        API_GET_CONFIG = API_ROOT + "/configuration/getConfigurationFile/%s";//phone number
        API_GET_CONFIG_FILE_TYPES_EMUM = API_ROOT + "/configuration/getPictureAndData/%s";//phone number
        API_SUBMIT_PICTURE = API_ROOT + "/submitPicture/fileupload";
        API_SUBMIT_INSPECTION = API_ROOT + "/submitInspection";
        API_SERVICE_STATUS = API_ROOT + "/serviceStatus";
    }
}
