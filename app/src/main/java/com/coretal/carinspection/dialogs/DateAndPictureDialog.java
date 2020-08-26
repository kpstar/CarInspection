package com.coretal.carinspection.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.coretal.carinspection.R;
import com.coretal.carinspection.controls.DateEditText;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.DateAndPicture;
import com.coretal.carinspection.services.API;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DateHelper;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.ImageFilePath;
import com.coretal.carinspection.utils.MyHelper;
import com.coretal.carinspection.utils.MyPreference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class DateAndPictureDialog extends DialogFragment implements SelectPictureSourceDialog.Callback, API.Callback {
    private String category;
    private List<String> fileTypeKeys;
    private List<String> fileTypeValues;
    private MyPreference myPref;
    private API api;

    public interface Callback{
        public void onDoneDateAndPictureDialog(DateAndPicture item, boolean isNew);
    }

    private static final int REQUEST_CAMERA_PERMISSION = 0;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_FROM_GALLERY = 2;
    private String mCurrentPhotoPath;

    private ImageView imageView;
    private Spinner typeSpinner;
    private DateEditText dateEditText;
    private AlertDialog alertDialog;

    private String newPictureID = "";

    private DBHelper dbHelper;

    public DateAndPicture editingItem;
    public DateAndPicture newItem;

    private Callback callback;

    public static DateAndPictureDialog newInstance(String category, Callback callback){
        DateAndPictureDialog dialog = new DateAndPictureDialog();
        dialog.setCancelable(false);
        dialog.callback = callback;
        dialog.category = category;
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dbHelper = new DBHelper(getActivity());
        myPref = new MyPreference(getActivity());
        api = new API(getContext(), this);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_date_and_pictures, null);
        dialogBuilder.setView(dialogView);

        typeSpinner = dialogView.findViewById(R.id.type_spinner);
        imageView = dialogView.findViewById(R.id.picture);
        dateEditText = dialogView.findViewById(R.id.date_edit);
        Button btnDone = (Button) dialogView.findViewById(R.id.btn_done);
        ImageButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Map<String, String> fileTypes = Contents.JsonFileTypesEnum.getTypesByCategory(category);
        fileTypeKeys = new ArrayList<>();
        Collections.addAll(fileTypeKeys, fileTypes.keySet().toArray(new String[fileTypes.size()]));
        fileTypeValues = new ArrayList<>(fileTypes.values());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, fileTypeValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        if(editingItem != null){
            typeSpinner.setSelection(fileTypeKeys.indexOf(editingItem.type));
            dateEditText.setDateString(editingItem.dateStr);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.ic_camera_48);
            GlideUrl glideUrl = new GlideUrl(editingItem.pictureURL, new LazyHeaders.Builder().addHeader(Contents.HEADER_KEY, Contents.TOKEN).build());
            Glide.with(getActivity())
                    .load(glideUrl)
                    .apply(requestOptions)
                    .into(imageView);
        }

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dateStr = dateEditText.getText().toString();
                String type = fileTypeKeys.get(typeSpinner.getSelectedItemPosition());
                if (editingItem != null) {
                    editingItem.dateStr = dateStr;
                    editingItem.date = DateHelper.stringToDate(dateStr);
                    editingItem.type = type;
                    if(!newPictureID.isEmpty()) {
                        if (!editingItem.pictureId.isEmpty()) dbHelper.removeFile(editingItem.pictureId);
                        editingItem.setPictureId(newPictureID);
                        dbHelper.setFileType(dbHelper.getLastInsertFileId(), type);
                    }
                    api.editPicture(editingItem, type);
                }else{
                    String status = DateAndPicture.STATUS_NEW;
                    newItem = new DateAndPicture(dateStr, newPictureID, type, status);
                    api.uploadPicture(newItem, type);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertHelper.question(getContext(), getString(R.string.alert), getString(R.string.are_you_sure_you_want_to_cancel), "Yes", "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        alertDialog.dismiss();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = Contents.PHONE_NUMBER;
                int submissionID = dbHelper.getDraftSubmission().id;
                long newFileID = dbHelper.getLastInsertFileId() + 1;
                long timestamp = System.currentTimeMillis();
                newPictureID = phoneNumber + "_" + submissionID + "_" + newFileID + "_" + timestamp;

                String imageSource = myPref.get_conf_app_image_source();
                if (imageSource.equals("CAMERA")){
                    onTakePhoto();
                }else if (imageSource.equals("GALERY")){
                    onFromGallery();
                }else{
                    DialogFragment fragment = SelectPictureSourceDialog.newInstance(DateAndPictureDialog.this);
                    fragment.show(getFragmentManager(), "dialog_select_picture_source");
                }
            }
        });

        LayerDrawable layerDrawable = (LayerDrawable) dialogView.getBackground();
        Drawable topDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_top);
        Drawable containerDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_container);
        DrawableHelper.setColor(topDrawable, myPref.getColorButton());
        DrawableHelper.setColor(containerDrawable, myPref.getColorBackground());
        DrawableHelper.setColor(btnDone.getBackground(), myPref.getColorButton());

        return alertDialog;
    }

    @Override
    public void onProcessImage(String okay, String error) {
        if (error.isEmpty()) {
            alertDialog.dismiss();
            AlertHelper.message(getContext(), "Success", okay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (editingItem != null) {
                        if (!editingItem.status.equals(DateAndPicture.STATUS_NEW)){
                            editingItem.status = DateAndPicture.STATUS_CHANGED;
                        }
                        callback.onDoneDateAndPictureDialog(editingItem, false);
                    }else{
                        String status = DateAndPicture.STATUS_NEW;
                        callback.onDoneDateAndPictureDialog(newItem, true);
                    }
                }
            });
        } else {
            AlertHelper.message(getContext(), "Error", error);
        }
    }

    @Override
    public void onTakePhoto() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_FROM_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(getActivity(), "Please grant camera permission", Toast.LENGTH_SHORT).show();
                }
        }
    }


    private File createImageFile() throws IOException {


        // Create an image file name
        File storageDir = new File(Contents.EXTERNAL_PICTURES_DIR_PATH);
        File image = new File(storageDir, newPictureID + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.coretal.carinspection.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            String fileLocation = "";
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO:
                    fileLocation = mCurrentPhotoPath;
                    Bitmap bitmap = MyHelper.getScaledBitMap(mCurrentPhotoPath);
                    imageView.setImageBitmap(bitmap);
                    break;
                case REQUEST_FROM_GALLERY:
                    Uri selectedImage = data.getData();
                    imageView.setImageURI(selectedImage);
                    String filePath = ImageFilePath.getPath(getActivity(), selectedImage);
                    String destPath = Contents.EXTERNAL_PICTURES_DIR_PATH + "/" + newPictureID + ".jpg";
                    FileHelper.copyFile(filePath, destPath);
                    fileLocation = destPath;
                    break;
                default:
                    break;
            }
            dbHelper.newFile(newPictureID, fileLocation);
        }else{
            newPictureID = "";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
