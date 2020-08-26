package com.coretal.carinspection.dialogs;

import android.app.Dialog;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class PhotoViewDialog extends DialogFragment {

    private String photoUrl;
    private MyPreference myPref;

    public static PhotoViewDialog newInstance(String pictureURL){
        PhotoViewDialog dialog = new PhotoViewDialog();
        dialog.photoUrl = pictureURL;
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
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_photo_view, container, true);

        Window dialogWindow = getDialog().getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button btnDone = dialogView.findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoViewDialog.this.dismiss();
            }
        });

        PhotoView photoView = dialogView.findViewById(R.id.photo_view);
        GlideUrl glideUrl = new GlideUrl(photoUrl, new LazyHeaders.Builder().addHeader(Contents.HEADER_KEY, Contents.TOKEN).build());
        Glide.with(getActivity())
                .load(glideUrl)
                .into(photoView);

        myPref = new MyPreference(getContext());
        LayerDrawable layerDrawable = (LayerDrawable) dialogView.getBackground();
        Drawable topDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_top);
        Drawable containerDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_container);
        DrawableHelper.setColor(topDrawable, myPref.getColorButton());
        DrawableHelper.setColor(containerDrawable, myPref.getColorBackground());
        DrawableHelper.setColor(btnDone.getBackground(), myPref.getColorButton());

        return dialogView;
    }
}
