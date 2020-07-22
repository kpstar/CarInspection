package com.coretal.carinspection.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.MyPreference;

/**
 * Created by Kangtle_R on 7/18/2020.
 */

public class AddDriverDialog extends DialogFragment {
    public interface Callback {
        public void onSubmitPhoneNumberDialog(String apiRoot, String phoneNumber);
    }

    private EditText phoneNumberEdit;
    private EditText apiRootEdit;
    private Button btnSubmit;
    private MyPreference myPref;
    private Callback callback;

    public static AddDriverDialog newInstance(Callback callback){
        AddDriverDialog dialog = new AddDriverDialog();
//        dialog.callback = callback;
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

//        btnSubmit = dialogView.findViewById(R.id.btn_submit);
//        apiRootEdit = dialogView.findViewById(R.id.edit_api_root);
//        phoneNumberEdit = dialogView.findViewById(R.id.edit_number);
//        apiRootEdit.setText(Contents.API_ROOT);
//
//        btnSubmit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String apiRoot = apiRootEdit.getText().toString();
//                String phoneNumber = phoneNumberEdit.getText().toString();
//                if (URLUtil.isValidUrl(apiRoot) && PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){
//                    myPref.setAPIRoot(apiRoot);
//                    myPref.setPhoneNumber(phoneNumber);
//                    dismiss();
//                    callback.onSubmitPhoneNumberDialog(apiRoot, phoneNumber);
//                }else{
//                    Toast.makeText(getContext(), "Please enter root url and phone number correctly.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        LayerDrawable layerDrawable = (LayerDrawable) dialogView.getBackground();
//        Drawable topDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_top);
//        Drawable containerDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_container);
//        DrawableHelper.setColor(topDrawable, myPref.getColorButton());
//        DrawableHelper.setColor(containerDrawable, myPref.getColorBackground());
//        DrawableHelper.setColor(btnSubmit.getBackground(), myPref.getColorButton());

        return alertDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
