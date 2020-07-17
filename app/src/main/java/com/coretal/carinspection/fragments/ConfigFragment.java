package com.coretal.carinspection.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.coretal.carinspection.R;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.coretal.carinspection.utils.VolleyHelper;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigFragment extends Fragment {

    private MyPreference myPreference;
    private DBHelper dbHelper;

    private Spinner actionSpinner;
    private ProgressDialog progressDialog;

    public ConfigFragment() {
        // Required empty public constructor
    }

    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_config, container, false);

        setConfigPreFragment();

        actionSpinner = view.findViewById(R.id.action_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.config_actions, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(adapter);

        myPreference = new MyPreference(getContext());
        dbHelper = new DBHelper(getContext());

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        Button submitButton = view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertHelper.question(getActivity(), "Confirm", "Are you sure?", "Yes", "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (actionSpinner.getSelectedItemPosition()){
                            case 0: //Save
                                myPreference.backup();
                                AlertHelper.message(getContext(), "Success", "Successfully saved");
                                Log.d("Kangtle", "did backup the config data.");
                                break;
                            case 1: //Revert
                                if (FileHelper.exists(Contents.Config.FILE_PATH)){
                                    myPreference.restore();
                                    setConfigPreFragment();
                                    AlertHelper.message(getContext(), "Success", "Successfully reverted");
                                    Log.d("Kangtle", "Successfully reverted the config data.");
                                }else{
                                    AlertHelper.message(getContext(), "Inspection", "Nothing to revert");
                                    Log.d("Kangtle", "Nothing to revert the config.");
                                }
                                break;
                            case 2: //Email
                                BackgroundMail.newBuilder(getContext())
                                        .withUsername(myPreference.get_conf_email_user())
                                        .withPassword(myPreference.get_conf_email_password())
                                        .withMailto(myPreference.get_conf_email_target_email())
                                        .withType(BackgroundMail.TYPE_PLAIN)
                                        .withSubject(myPreference.get_conf_email_subject())
                                        .withBody(myPreference.getConfigToFormatString())
                                        .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d("Kangtle", "successfully send email the config");
                                            }
                                        })
                                        .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                                            @Override
                                            public void onFail() {
                                                Log.d("Kangtle", "failed to send email the config");
                                            }
                                        })
                                        .send();
                                break;
                            case 3: //Overide
                                Log.d("Kangtle", "getting config file");
                                progressDialog.setMessage("Getting config file...");
                                progressDialog.show();
                                JsonObjectRequest getRequest = new JsonObjectRequest(
                                        Request.Method.GET,
                                        String.format(Contents.API_GET_CONFIG, Contents.PHONE_NUMBER),
                                        null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                progressDialog.hide();
                                                myPreference.restoreFromJSONObject(response, null);
                                                setConfigPreFragment();
                                                Log.d("Kangtle", "overrode config file");
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                progressDialog.dismiss();
                                                Log.d("Kangtle", "Can't get config file. failed to override");
                                                AlertHelper.message(getContext(), "Error", "Can't get config file.", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
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
                                VolleyHelper volleyHelper = new VolleyHelper(getContext());
                                volleyHelper.add(getRequest);
                                break;
                            default:
                                Log.d("Kangtle", "no action");
                                break;
                        }
                    }
                }, null);
            }
        });

        DrawableHelper.setColor(submitButton.getBackground(), myPreference.getColorButton());
        return view;
    }

    private void setConfigPreFragment(){
        Fragment newFragment = new ConfigPrefFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.config_pre_fragment, newFragment);
        transaction.commit();
    }

}
