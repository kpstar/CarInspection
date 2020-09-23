package com.coretal.carinspection.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.coretal.carinspection.R;
import com.coretal.carinspection.fragments.DriverDetailFragment;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.services.SyncService;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.coretal.carinspection.utils.VolleyHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kangtle_R on 7/18/2020.
 */

public class AddDriverDialog extends DialogFragment {
    public interface Callback {
        public void onAddNewDriver();
    }

    private EditText driverId, licenseNum, name, address, phoneNum, desc;
    private Button btnSave, btnCancel;
    private CheckBox chk_a2, chk_a1, chk_a, chk_b, chk_c1, chk_c, chk_c_e, chk_d1, chk_d, chk_d3, chk_1;
    private MyPreference myPref;
    private Callback callback;
    private ProgressDialog progressDialog;

    public static AddDriverDialog newInstance(Callback callback){
        AddDriverDialog dialog = new AddDriverDialog();
        dialog.callback = callback;
        dialog.setCancelable(false);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        myPref = new MyPreference(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_new_driver, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnSave = dialogView.findViewById(R.id.btn_save);
        btnCancel = dialogView.findViewById(R.id.btn_cancel);
        driverId = dialogView.findViewById(R.id.edit_driver_id);
        licenseNum = dialogView.findViewById(R.id.edit_license_number);
        name = dialogView.findViewById(R.id.edit_name);
        address = dialogView.findViewById(R.id.edit_address);
        phoneNum = dialogView.findViewById(R.id.edit_phone_number);
        desc = dialogView.findViewById(R.id.edit_desc);
        chk_1 = dialogView.findViewById(R.id.chk_1);
        chk_d1 = dialogView.findViewById(R.id.chk_d1);
        chk_d = dialogView.findViewById(R.id.chk_d);
        chk_d3 = dialogView.findViewById(R.id.chk_d3);
        chk_c1 = dialogView.findViewById(R.id.chk_c1);
        chk_c = dialogView.findViewById(R.id.chk_c);
        chk_c_e = dialogView.findViewById(R.id.chk_c_e);
        chk_b = dialogView.findViewById(R.id.chk_b);
        chk_a = dialogView.findViewById(R.id.chk_a);
        chk_a1 = dialogView.findViewById(R.id.chk_a1);
        chk_a2 = dialogView.findViewById(R.id.chk_a2);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");

        getConfDriverData();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String id = driverId.getText().toString();
                String lic_num = licenseNum.getText().toString();
                final String driver_name = name.getText().toString();
                String driver_address = address.getText().toString();
                String phone_num = phoneNum.getText().toString();
                String descrip = desc.getText().toString();

                boolean a2 = chk_a2.isChecked();
                boolean a1 = chk_a1.isChecked();
                boolean a_1 = chk_1.isChecked();
                boolean b = chk_b.isChecked();
                boolean c1 = chk_c1.isChecked();
                boolean c = chk_c.isChecked();
                boolean c_e = chk_c_e.isChecked();
                boolean d1 = chk_d1.isChecked();
                boolean d = chk_d.isChecked();
                boolean d3 = chk_d3.isChecked();
                boolean a = chk_a.isChecked();

                String error = "";
                if (id.isEmpty() || lic_num.isEmpty() || driver_name.isEmpty() || driver_address.isEmpty() || phone_num.isEmpty() || descrip.isEmpty()) {
                    error = "Please fill the blanks.";
                }
                if (!error.isEmpty()) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                    return;
                }

                final JSONObject driverJson = new JSONObject();
                JSONObject formJson = new JSONObject();
                try {
                    driverJson.put(Contents.NewDriverJson.DRIVER_FORM, formJson);
                    driverJson.put(Contents.NewDriverJson.INSPECTOR_PHONE_NUMBER, Contents.PHONE_NUMBER);

                    formJson.put(Contents.NewDriverJson.ID, id);
                    formJson.put(Contents.NewDriverJson.LICENSE_NUMBER, lic_num);
                    formJson.put(Contents.NewDriverJson.NAME, driver_name);
                    formJson.put(Contents.NewDriverJson.ADDRESS, driver_address);
                    formJson.put(Contents.NewDriverJson.PHONE_NUMBER, phone_num);
                    formJson.put(Contents.NewDriverJson.DESCRIPTION, descrip);
                    formJson.put(Contents.JsonVehicleData.COMPANYID, myPref.getCompanyId());
                    formJson.put("lic_a2", a2);
                    formJson.put("lic_a1", a1);
                    formJson.put("lic_a", a);
                    formJson.put("lic_b", b);
                    formJson.put("lic_c1", c1);
                    formJson.put("lic_c", c);
                    formJson.put("lic_ce", c_e);
                    formJson.put("lic_d1", d1);
                    formJson.put("lic_d", d);
                    formJson.put("lic_3d", d3);
                    formJson.put("lic_1", a_1);

                    Log.d("Kangtle", driverJson.toString());
                    myPref.setAddDriverJson(driverJson.toString());
                    progressDialog.show();

                    VolleyHelper addDriverVolleyHelper = new VolleyHelper(getContext());
                    JsonObjectRequest postDriverDataRequest = new JsonObjectRequest(
                            Request.Method.POST,
                            Contents.API_SUBMIT_NEW_DRIVER,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    progressDialog.hide();
                                    myPref.setAddDriverJson("");
                                    dismiss();
                                    Toast.makeText(getContext(), response.optString("message"), Toast.LENGTH_LONG).show();
                                    callback.onAddNewDriver();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressDialog.hide();
                                    try {
                                        String respTxt = new String(error.networkResponse.data, "UTF-8");
                                        JSONObject resp = new JSONObject(respTxt);
                                        AlertHelper.message(getContext(), getString(R.string.error), resp.optString("message"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    ){
                        @Override
                        public byte[] getBody() {
                            try {
                                return driverJson.toString().getBytes("utf-8");
                            } catch (UnsupportedEncodingException e) {
                                return "error".getBytes();
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
                    addDriverVolleyHelper.add(postDriverDataRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDriverDialog.this.dismiss();
            }
        });

        return alertDialog;
    }

    private void getConfDriverData() {
        String data = myPref.getDriverJson();
        if (data.isEmpty()) return;
        JSONObject driverJson;
        JSONObject formJson;
        try {
            driverJson= new JSONObject(data);
            formJson = driverJson.getJSONObject(Contents.NewDriverJson.DRIVER_FORM);

            driverId.setText(formJson.getString(Contents.NewDriverJson.ID));
            licenseNum.setText(formJson.getString(Contents.NewDriverJson.LICENSE_NUMBER));
            name.setText(formJson.getString(Contents.NewDriverJson.NAME));
            address.setText(formJson.getString(Contents.NewDriverJson.ADDRESS));
            phoneNum.setText(formJson.getString(Contents.NewDriverJson.PHONE_NUMBER));
            desc.setText(formJson.getString(Contents.NewDriverJson.DESCRIPTION));

            chk_a2.setChecked(formJson.optBoolean("lic_a2"));
            chk_a1.setChecked(formJson.optBoolean("lic_a1"));
            chk_a.setChecked(formJson.optBoolean("lic_a"));
            chk_b.setChecked(formJson.optBoolean("lic_b"));
            chk_c1.setChecked(formJson.optBoolean("lic_c1"));
            chk_c.setChecked(formJson.optBoolean("lic_c"));
            chk_c_e.setChecked(formJson.optBoolean("lic_ce"));
            chk_d1.setChecked(formJson.optBoolean("lic_d1"));
            chk_d.setChecked(formJson.optBoolean("lic_d"));
            chk_d3.setChecked(formJson.optBoolean("lic_3d"));
            chk_1.setChecked(formJson.optBoolean("lic_1"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
