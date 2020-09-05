package com.coretal.carinspection.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.coretal.carinspection.R;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;

/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class RemarksDialog extends DialogFragment {
    private String text;

    public interface Callback {
        public void onSubmitRemarks(String remarks);
    }

    private RemarksDialog.Callback callback;
    private DBHelper dbHelper;

    public static RemarksDialog newInstance(RemarksDialog.Callback callback, String text){
        RemarksDialog dialog = new RemarksDialog();
        dialog.callback = callback;
        dialog.text = text;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dbHelper = new DBHelper(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_remarks, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button btnSubmit = (Button) dialogView.findViewById(R.id.btn_submit);
        final EditText remarksEdit = (EditText) dialogView.findViewById(R.id.edit_remarks);
        remarksEdit.setText(text);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remarks = remarksEdit.getText().toString();
                MyHelper.hideKeyBoard(getActivity(), remarksEdit);
                callback.onSubmitRemarks(remarks);
                alertDialog.dismiss();
            }
        });

        return alertDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

}
