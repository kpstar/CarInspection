package com.coretal.carinspection.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;

import com.android.volley.error.AuthFailureError;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.coretal.carinspection.R;
import com.coretal.carinspection.dialogs.API_PhoneNumberDialog;
import com.coretal.carinspection.fragments.VehicleDateAndPicturesFragment;
import com.coretal.carinspection.fragments.HomeFragment;
import com.coretal.carinspection.fragments.InspectionFragment;
import com.coretal.carinspection.fragments.NotesFragment;
import com.coretal.carinspection.fragments.SettingFragment;
import com.coretal.carinspection.receivers.AlarmReceiver;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.coretal.carinspection.utils.VolleyHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements API_PhoneNumberDialog.Callback {

    String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};

    private BottomNavigationViewEx navigation;
    private ImageView connectivityImageView;

    private MyPreference myPreference;

    private Fragment homeFragment;
    private Fragment inspectionFragment;
    private Fragment vehicleDateAndPicturesFragment;
    private Fragment notesFragment;
    private Fragment settingFragment;
    public static Menu menu;
    private Fragment selectedFragment;
    public static Context mContext;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            selectFragment(item);
            return true;
        }
    };
    private ProgressDialog progressDialog;
    private ConnectivityReceiver connectivityReceiver;

    private Timer timer;
    private TimerTask timerTask;
    private final Handler timerHandler = new Handler();

    private VolleyHelper volleyHelper;

    private void selectFragment(MenuItem item) {
        gotoFragment(item.getItemId());
    }

    public static void dismissAllDialogs(FragmentManager manager) {
        List<Fragment> fragments = manager.getFragments();

        if (fragments == null)
            return;

        for (Fragment fragment : fragments) {
            if (fragment instanceof DialogFragment) {
                DialogFragment dialogFragment = (DialogFragment) fragment;
                dialogFragment.dismissAllowingStateLoss();
            }

            FragmentManager childFragmentManager = fragment.getChildFragmentManager();
            if (childFragmentManager != null)
                dismissAllDialogs(childFragmentManager);
        }
    }

    private void gotoFragment(int menuId){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (selectedFragment instanceof InspectionFragment && menuId == R.id.navigation_truck) {
            return;
        }

        if(selectedFragment != null) {
            transaction.hide(selectedFragment);
        }

        Fragment fragment = getFragmentByMenuId(menuId);
        if(fragment == null){
            fragment = setFragmentWithMenuId(menuId);
            transaction.add(R.id.frame_layout, fragment);
        }else{
            transaction.show(fragment);
        }

        selectedFragment = fragment;

        transaction.commit();
    }

    private Fragment getFragmentByMenuId(int menuId) {
        switch (menuId) {
            case R.id.navigation_home:
                return homeFragment;
            case R.id.navigation_truck:
                return inspectionFragment;
            case R.id.navigation_camera:
                return vehicleDateAndPicturesFragment;
            case R.id.navigation_notes:
                return notesFragment;
            case R.id.navigation_setting:
                return settingFragment;
            default:
                return null;
        }
    }

    public Fragment setFragmentWithMenuId(int menuId) {
        switch (menuId) {
            case R.id.navigation_home:
                homeFragment = HomeFragment.newInstance();
                return homeFragment;
            case R.id.navigation_truck:
                inspectionFragment = InspectionFragment.newInstance();
                return inspectionFragment;
            case R.id.navigation_camera:
                vehicleDateAndPicturesFragment = VehicleDateAndPicturesFragment.newInstance();
                return vehicleDateAndPicturesFragment;
            case R.id.navigation_notes:
                notesFragment = NotesFragment.newInstance();
                return notesFragment;
            case R.id.navigation_setting:
                settingFragment = SettingFragment.newInstance();
                return settingFragment;
            default:
                return null;
        }
    }

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            if (hasPermissions(PERMISSIONS)){
                setupAfterPermissions();
            }else{
                int PERMISSION_ALL = 1;
                if(!hasPermissions(PERMISSIONS)){
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        volleyHelper = new VolleyHelper(this);
        myPreference = new MyPreference(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(myPreference.getColorButton());
        }

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        navigation.enableShiftingMode(false);
        navigation.setItemIconTintList(ColorStateList.valueOf(myPreference.getColorButton()));
        navigation.setItemTextColor(ColorStateList.valueOf(myPreference.getColorButton()));

        menu = navigation.getMenu();
        selectFragment(menu.getItem(0));

        startConnectivityTimer();

        connectivityImageView = findViewById(R.id.connectivityImageView);
//        registerConnectivityAction();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        new Handler().postDelayed(checkRunnable, 200);

//        startAlarmReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Kangtle", "MainActivity onResume");
//        registerConnectivityAction();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.d("Kangtle", "MainActivity onPause");
//        dismissAllDialogs(getSupportFragmentManager());
////        unregisterReceiver(connectivityReceiver);
//    }

    public void refresh(){
        finish();
        startActivity(getIntent());
    }

    private void setupAfterPermissions(){
        String apiRoot = myPreference.getAPIBaseURL();
        boolean isSetURL = myPreference.getURLSet();
        String phoneNumber = myPreference.getPhoneNumber();
        int truckType = myPreference.getVehicleType();
        String guid = myPreference.getGUID();
        Contents.TOKEN = guid;
        Contents.TRUCK_TYPE = truckType;
        if (phoneNumber.isEmpty() || !isSetURL || guid.isEmpty()){
            getAPI_PhoneNumberWithDialog();
        }else{
            Contents.configAPIs(this);
            Contents.PHONE_NUMBER = phoneNumber;
            if(!myPreference.isGettedConfig()){
                getConfigFile();
            }
//            registerDevice();
        }
    }


    private boolean hasPermissions(String... permissions){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (hasPermissions(PERMISSIONS)){
            setupAfterPermissions();
        }else{
            AlertHelper.message(this, "Permissionss", "Need all permission", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
    }

    private void registerConnectivityAction(){
        connectivityReceiver = new ConnectivityReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, intentFilter);
    }

    public void getAPI_PhoneNumberWithDialog(){

        DialogFragment fragment = API_PhoneNumberDialog.newInstance(this);
        fragment.show(getSupportFragmentManager(), "dialog_api_phone_number");

    }

//    private void registerDevice() {
//        JsonObjectRequest getRequest = new JsonObjectRequest(
//                Request.Method.GET,
//                String.format(Contents.API_REGISTER_DEVICE, Contents.PHONE_NUMBER),
//                null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        if (response.has("error")){
//                            String error = response.optString("error");
//                            Log.d("Kangtle", error);
//                        } else {
//                            try {
//                                String token = response.getString(Contents.TOKEN_KEY);
//                                myPreference.setGUID(token);
//                                Contents.TOKEN = token;
//
//                                if(myPreference.isGettedConfig()){
//                                    resetConfigOnlyRequiredFields();
//                                }else{
//                                    getConfigFile();
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d("Kangtle", error.toString());
//                        AlertHelper.message(MainActivity.this, "ErrorToken", error.toString(), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                getAPI_PhoneNumberWithDialog();
//                            }
//                        });
//                    }
//                }
//        );
//        volleyHelper.add(getRequest);
//    }

    @Override
    public void onSubmitPhoneNumberDialog(String apiRoot, String phoneNumber) {

        setupAfterPermissions();
    }

    private void getConfigFile() {
        Log.d("Kangtle", "Getting config file...");
        progressDialog.setMessage("Getting config file...");
        progressDialog.show();
        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET,
                String.format(Contents.API_GET_CONFIG, Contents.PHONE_NUMBER),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.has("error")){
                            String error = response.optString("error");
                            Log.d("Kangtle", error);
                            progressDialog.dismiss();
                            AlertHelper.message(MainActivity.this, "Error", error, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    getAPI_PhoneNumberWithDialog();
                                }
                            });
                        }else{
                            Log.d("Kangtle", "Getted config file successfully");
                            progressDialog.hide();
                            myPreference.restoreFromJSONObject(response, null);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Kangtle", "Can't get config file.");
                        progressDialog.dismiss();
                        AlertHelper.message(MainActivity.this, "Error", "Can't get config file.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getAPI_PhoneNumberWithDialog();
                            }
                        });
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
        volleyHelper.add(getRequest);
    }

    private void resetConfigOnlyRequiredFields() {
        Log.d("Kangtle", "reset config file for only required field");
        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET,
                String.format(Contents.API_GET_CONFIG, Contents.PHONE_NUMBER),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!response.has("error")){
                            Log.d("Kangtle", "Getted config file successfully");
                            myPreference.resetOnlyRequiredFields(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Kangtle", "Can't get config file.");
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
        volleyHelper.add(getRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        progressDialog.dismiss();
    }

    public void startConnectivityTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                timerHandler.post(new Runnable() {
                    public void run() {
                        JsonObjectRequest getRequest = new JsonObjectRequest(
                                Request.Method.GET,
                                Contents.API_SERVICE_STATUS,
                                null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        connectivityImageView.setImageResource(R.drawable.ic_green_circle);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        connectivityImageView.setImageResource(R.drawable.ic_red_circle);
                                    }
                                }
                        );
                        volleyHelper.add(getRequest);
                    }
                });
            }
        };
        timer.schedule(timerTask, 5000, 10000); //
    }

    private class ConnectivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = MyHelper.isConnectedInternet(MainActivity.this);
            Log.d("Kangtle", "network is connected " + isConnected);
            if(isConnected){
                connectivityImageView.setImageResource(R.drawable.ic_green_circle);
            }else{
                connectivityImageView.setImageResource(R.drawable.ic_red_circle);
            }
        }
    }
}
