package com.coretal.carinspection.fragments;


import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.coretal.carinspection.R;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.github.gcacace.signaturepad.views.SignaturePad;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotesFragment extends Fragment {
    private MyPreference myPreference;
    SignaturePad pad;
    EditText notesEdit;
    ImageButton clearButton;
    DBHelper dbHelper;
    FrameLayout signaterPadWrapper;
    private String signatureFilePath;


    public NotesFragment() {
        // Required empty public constructor
    }

    public static NotesFragment newInstance() {
        return new NotesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myPreference = new MyPreference(getContext());
        dbHelper = new DBHelper(getContext());

        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        signaterPadWrapper = view.findViewById(R.id.signature_wrapper);
        pad = view.findViewById(R.id.signature_pad);
        notesEdit = view.findViewById(R.id.notes_edit);
        clearButton = view.findViewById(R.id.clearButton);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertHelper.question(getContext(), getString(R.string.confirm), getString(R.string.are_you_sure), getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pad.clear();
                    }
                }, null);
            }
        });

        onHiddenChanged(false);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!Contents.IS_STARTED_INSPECTION) return;

        Submission submission = dbHelper.getDraftSubmission();
        int submissionId = submission.id;
        String submissionNotes = submission.notes;
        String signatureFileName = submissionId + "_" + "inspection_notes_hand_writing.jpg";
        signatureFilePath = Contents.EXTERNAL_PICTURES_DIR_PATH + "/" + signatureFileName;

        if(!hidden){
            if (myPreference.getAppNotesLayout().equals("TEXT")){
                signaterPadWrapper.setVisibility(View.GONE);
                notesEdit.setVisibility(View.VISIBLE);
            }else if (myPreference.getAppNotesLayout().equals("SCRIBBLE")){
                signaterPadWrapper.setVisibility(View.VISIBLE);
                notesEdit.setVisibility(View.GONE);
            }else{
                signaterPadWrapper.setVisibility(View.VISIBLE);
                notesEdit.setVisibility(View.VISIBLE);
            }
            notesEdit.setText(submissionNotes);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(FileHelper.exists(signatureFilePath)){
                        Bitmap bitmap = BitmapFactory.decodeFile(signatureFilePath);
                        pad.setSignatureBitmap(bitmap);
                    }
                }
            }, 100);

        }else{
            if (myPreference.getAppNotesLayout().equals("TEXT")){
                String notes = notesEdit.getText().toString();
                dbHelper.setNotesForDraftSubmission(notes);
            }else if (myPreference.getAppNotesLayout().equals("SCRIBBLE")){
                savePicture();
            }else{
                String notes = notesEdit.getText().toString();
                dbHelper.setNotesForDraftSubmission(notes);
                savePicture();
            }
        }
    }

    private void savePicture(){
        String phoneNumber = Contents.PHONE_NUMBER;
        int submissionID = dbHelper.getDraftSubmission().id;
        long newFileID = dbHelper.getLastInsertFileId() + 1;
        long timestamp = System.currentTimeMillis();
        String newPictureID = phoneNumber + "_" + submissionID + "_" + newFileID + "_" + timestamp;

        Bitmap bitmap = pad.getSignatureBitmap();
        FileHelper.saveBitmap(bitmap, 100, signatureFilePath);
        if (!dbHelper.fileExists(signatureFilePath)){
            long insertedID = dbHelper.newFile(newPictureID, signatureFilePath);
            dbHelper.setFileType(insertedID, "INSPECTION_NOTES_HAND_WRITING");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        onHiddenChanged(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
