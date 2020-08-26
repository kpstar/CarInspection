package com.coretal.carinspection.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.coretal.carinspection.R;
import com.coretal.carinspection.activities.MainActivity;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.coretal.carinspection.utils.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class API_PhoneNumberDialog extends DialogFragment {
    public interface Callback {
        public void onSubmitPhoneNumberDialog(String apiRoot, String phoneNumber);
    }

    private EditText phoneNumberEdit;
    private EditText apiRootEdit;
    private Button btnSubmit;
    private MyPreference myPref;
    private Callback callback;
    private VolleyHelper volleyHelper;
    private ProgressDialog progressDialog;

    public static API_PhoneNumberDialog newInstance(Callback callback){
        API_PhoneNumberDialog dialog = new API_PhoneNumberDialog();
        dialog.callback = callback;
        dialog.setCancelable(false);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        myPref = new MyPreference(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_api_phone_number, null);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        volleyHelper = new VolleyHelper(getContext());
        btnSubmit = dialogView.findViewById(R.id.btn_submit);
        apiRootEdit = dialogView.findViewById(R.id.edit_api_root);
        phoneNumberEdit = dialogView.findViewById(R.id.edit_number);
        apiRootEdit.setText(Contents.API_ROOT);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                btnSubmit.setEnabled(false);
                final String apiRoot = apiRootEdit.getText().toString();
                final String phoneNumber = phoneNumberEdit.getText().toString();
                if (URLUtil.isValidUrl(apiRoot) && PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){
                    Log.d("Kangtle", "Register device, getting token...");
                    progressDialog.show();
                    JsonObjectRequest getRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            String.format(apiRoot + "/configuration/registerDevice/%s", phoneNumber),
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    progressDialog.hide();
                                    if (response.has("error")){
                                        String error = response.optString("error");
                                        Log.d("Kangtle", error);
                                        AlertHelper.message(getContext(), "Error", error, null);
                                    } else {
                                        Log.d("Kangtle", "Getted token successfully");
                                        try {
                                            String token = response.getString(Contents.TOKEN_KEY);
                                            Contents.TOKEN = token;
                                            myPref.setAPIRoot(apiRoot);
                                            myPref.setIsSetUrl();
                                            myPref.setPhoneNumber(phoneNumber);
                                            myPref.setGUID(token);
                                            callback.onSubmitPhoneNumberDialog(apiRoot, phoneNumber);
                                            dismiss();
                                            Log.d("****Token****", Contents.TOKEN);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Kangtle", error.toString());
                                    progressDialog.hide();
                                    try {
                                        String respTxt = new String(error.networkResponse.data, "UTF-8");
                                        JSONObject resp = new JSONObject(respTxt);
                                        String errotText = resp.has("error") ? resp.optString("message") : "Something went wrong, please try again";
                                        AlertHelper.message(getContext(), getString(R.string.error), errotText);
                                    } catch (Exception e) {
                                        AlertHelper.message(getContext(), getString(R.string.error), "Something went wrong, please try again");
                                        e.printStackTrace();
                                    }
                                }
                            }
                    );
                    volleyHelper.add(getRequest);
                }else{
                    Toast.makeText(getContext(), "Please enter root url and phone number correctly.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LayerDrawable layerDrawable = (LayerDrawable) dialogView.getBackground();
        Drawable topDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_top);
        Drawable containerDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_container);
        DrawableHelper.setColor(topDrawable, myPref.getColorButton());
        DrawableHelper.setColor(containerDrawable, myPref.getColorBackground());
        DrawableHelper.setColor(btnSubmit.getBackground(), myPref.getColorButton());

        return alertDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
