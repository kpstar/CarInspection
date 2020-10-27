package com.coretal.carinspection.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.coretal.carinspection.R;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.github.gcacace.signaturepad.views.SignaturePad;

/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class SignatureDialog extends DialogFragment {
    private MyPreference myPref;

    public interface Callback {
        public void onSubmitSignatures();
    }

    private SignaturePad driverSignaturePad;
    private SignaturePad inspectorSignaturePad;
    private EditText driverNameEdit;
    private EditText inspectorNameEdit;
    private Button submitButton;
    DBHelper dbHelper;
    private Callback callback;
    private String driverName;
    private String inspectorName;

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setInspectorName(String inspectorName) {
        this.inspectorName = inspectorName;
    }

    public static SignatureDialog newInstance(Callback callback){
        SignatureDialog dialog = new SignatureDialog();
        dialog.callback = callback;
        return dialog;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_signature, container, true);

        Window dialogWindow = getDialog().getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dbHelper = new DBHelper(getContext());

        driverSignaturePad = dialogView.findViewById(R.id.driver_signature_pad);
        driverNameEdit = dialogView.findViewById(R.id.driver_name_edit);
        inspectorSignaturePad = dialogView.findViewById(R.id.inspector_signature_pad);
        inspectorNameEdit = dialogView.findViewById(R.id.inspector_name_edit);
        submitButton = dialogView.findViewById(R.id.btn_submit);

        driverNameEdit.setText(driverName);
        inspectorNameEdit.setText(inspectorName);

        ImageButton dClearButton = dialogView.findViewById(R.id.driverSignClearButton);
        ImageButton iClearButton = dialogView.findViewById(R.id.inspectorSignClearButton);

        dClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertHelper.question(getContext(), "Confirm", "Are you sure?", "Yes", "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        driverSignaturePad.clear();
                    }
                }, null);
            }
        });

        iClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertHelper.question(getContext(), "Confirm", "Are you sure?", "Yes", "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inspectorSignaturePad.clear();
                    }
                }, null);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkFields()){
                    return;
                }

                Bitmap driverSignature = driverSignaturePad.getSignatureBitmap();
                Bitmap inspectorSignature = inspectorSignaturePad.getSignatureBitmap();

                Submission submission = dbHelper.getDraftSubmission();
                if (submission == null) return;
                int submissionId = submission.id;
                String driverSignatureFileName = submissionId + "_" + "driver_signiture.jpg";
                String inspectorSignatureFileName = submissionId + "_" + "inspector_signiture.jpg";
                String driverSignatureFilePath = Contents.EXTERNAL_PICTURES_DIR_PATH + "/" + driverSignatureFileName;
                String inspectorSignatureFilePath = Contents.EXTERNAL_PICTURES_DIR_PATH + "/" + inspectorSignatureFileName;

                savePicture(driverSignature, driverSignatureFilePath, "INSPECTION_SIGNITURE_DRIVER");
                savePicture(inspectorSignature, inspectorSignatureFilePath, "INSPECTION_SIGNITURE_INSPECTOR");

                callback.onSubmitSignatures();
                SignatureDialog.this.dismiss();
            }
        });

        myPref = new MyPreference(getContext());
        LayerDrawable layerDrawable = (LayerDrawable) dialogView.getBackground();
        Drawable topDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_top);
        Drawable containerDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_container);
        DrawableHelper.setColor(topDrawable, myPref.getColorButton());
        DrawableHelper.setColor(containerDrawable, myPref.getColorBackground());
        DrawableHelper.setColor(submitButton.getBackground(), myPref.getColorButton());

        return dialogView;
    }

    private boolean checkFields(){
        if (driverSignaturePad.isEmpty()){
            Toast.makeText(getContext(), "Required the driver signature", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (inspectorSignaturePad.isEmpty()){
            Toast.makeText(getContext(), "Required the inspector signature", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (driverNameEdit.getText().toString().isEmpty()){
            Toast.makeText(getContext(), "Required the driver name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (inspectorNameEdit.getText().toString().isEmpty()){
            Toast.makeText(getContext(), "Required the inspector name", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void savePicture(Bitmap bitmap, String fileLocation, String fileType){
        String phoneNumber = Contents.PHONE_NUMBER;
        int submissionID = dbHelper.getDraftSubmission().id;
        long newFileID = dbHelper.getLastInsertFileId() + 1;
        long timestamp = System.currentTimeMillis();
        String newPictureID = phoneNumber + "_" + submissionID + "_" + newFileID + "_" + timestamp;

        FileHelper.saveBitmap(bitmap, 100, fileLocation);
        long insertedID = dbHelper.newFile(newPictureID, fileLocation);
        dbHelper.setFileType(insertedID, fileType);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

}
