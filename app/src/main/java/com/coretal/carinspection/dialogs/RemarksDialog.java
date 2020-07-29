package com.coretal.carinspection.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.coretal.carinspection.R;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;

/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class RemarksDialog extends DialogFragment {
    private MyPreference myPref;

    public interface Callback {
        public void onSubmitRemarks(String remarks);
    }

    private RemarksDialog.Callback callback;
    private DBHelper dbHelper;

    public static RemarksDialog newInstance(RemarksDialog.Callback callback){
        RemarksDialog dialog = new RemarksDialog();
        dialog.callback = callback;
//        dialog.setCancelable(false);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dbHelper = new DBHelper(getContext());
        myPref = new MyPreference(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_remarks, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button btnSubmit = (Button) dialogView.findViewById(R.id.btn_submit);
        final EditText remarksEdit = (EditText) dialogView.findViewById(R.id.edit_remarks);
//
//        Submission draft = dbHelper.getDraftSubmission();
//        if(draft != null) {
//            String draftVPlate = draft.vehiclePlate;
//            vPlateEdit.setText(draftVPlate);
//        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remarks = remarksEdit.getText().toString();
                if (remarks.isEmpty()) return;
//                if (dbHelper.checkUnsubmittedSubmission(vPlate)){
//                    AlertHelper.message(getContext(), "Warning", getString(R.string.enter_another_vehicle_number));
//                    return;
//                }
                MyHelper.hideKeyBoard(getActivity(), remarksEdit);
                callback.onSubmitRemarks(remarksEdit.getText().toString());
                alertDialog.dismiss();
            }
        });

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
        dbHelper.close();
    }

}
