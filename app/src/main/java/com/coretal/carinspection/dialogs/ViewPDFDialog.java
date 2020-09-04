package com.coretal.carinspection.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.AsyncTask;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.request.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.VehicleDocAdapter;
import com.coretal.carinspection.fragments.VehicleDocFragment;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.FileHelper;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.coretal.carinspection.utils.VolleyHelper;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;


/**
 * Created by Kangtle_R on 1/24/2018.
 */

public class ViewPDFDialog extends DialogFragment {

    private VehicleDocAdapter.DocContent content;
    private MyPreference myPref;
    private ProgressDialog progressDialog;
    private PDFView pdfView;

    public static ViewPDFDialog newInstance(VehicleDocAdapter.DocContent content){
        ViewPDFDialog dialog = new ViewPDFDialog();
        dialog.content = content;
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
        View dialogView = inflater.inflate(R.layout.dialog_pdf_view, container, true);

        Window dialogWindow = getDialog().getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button btnDone = dialogView.findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewPDFDialog.this.dismiss();
            }
        });

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");

        pdfView = dialogView.findViewById(R.id.pdfView);


        myPref = new MyPreference(getContext());
        LayerDrawable layerDrawable = (LayerDrawable) dialogView.getBackground();
        Drawable topDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_top);
        Drawable containerDrawable = layerDrawable.findDrawableByLayerId(R.id.dialog_bg_container);
        DrawableHelper.setColor(topDrawable, myPref.getColorButton());
        DrawableHelper.setColor(containerDrawable, myPref.getColorBackground());
        DrawableHelper.setColor(btnDone.getBackground(), myPref.getColorButton());

//        getPdf();
        new DownloadFileFromURL().execute("http://24.30.63.116:8080/Peled_v6/restful/vehicle/getVehicleInspectionGet/038a8a7e-cc40-442d-aeca-c754ca700a54/8617165340102/53386");
        return dialogView;
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        String filename="";
        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();
                String depo = conection.getHeaderField("Content-Disposition");
                String depoSplit[] = depo.split("filename=");
                filename = depoSplit[1].replace("filename=", "").replace("\"", "").trim();
                Log.v("","fileName"+filename);

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream
                OutputStream output = new FileOutputStream(Contents.JsonVehicleData.PDF_PATH);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.toString());
            }

            return null;
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {

            File file = new File(Contents.JsonVehicleData.PDF_PATH);
            pdfView.fromFile(file)
                    .defaultPage(1)
                    .onLoad(new OnLoadCompleteListener() {

                        @Override
                        public void loadComplete(int nbPages) {
                            Log.d("Dr", String.valueOf(nbPages));
                        }
                    })
                    .onError(new OnErrorListener() {
                        @Override
                        public void onError(Throwable t) {
                            Log.d("err", t.getLocalizedMessage());
                        }
                    })
                    .load();
        }
    }

    private void getPdf() {
        progressDialog.show();
        StringRequest getDriverDataRequest = new StringRequest(
                Request.Method.GET,
//                String.format(Contents.API_VEHICLE_PDF, Contents.TOKEN, Contents.PHONE_NUMBER, content.id),
                "http://24.30.63.116:8080/Peled_v6/restful/vehicle/getVehicleInspectionGet/038a8a7e-cc40-442d-aeca-c754ca700a54/8617165340102/53386",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.hide();
                        FileHelper.writeStringToFile(response, Contents.JsonVehicleData.PDF_PATH);
                        File file = new File(Contents.JsonVehicleData.PDF_PATH);
                        InputStream input = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
                            //input is your input stream object
                        OutputStream output = null;
                        try {
                            output = new FileOutputStream(file);
                            try {
                                try {
                                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                    int read;

                                    while ((read = input.read(buffer)) != -1) {
                                        output.write(buffer, 0, read);
                                    }
                                    output.flush();
                                } finally {
                                    output.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // handle exception, define IOException and others
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }


                        pdfView.fromFile(file)
                                .defaultPage(1)
                                .onLoad(new OnLoadCompleteListener() {

                                    @Override
                                    public void loadComplete(int nbPages) {
                                        Log.d("Dr", String.valueOf(nbPages));
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.d("err", t.getLocalizedMessage());
                                    }
                                })
                                .load();
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(getContext(), "No PDF", Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                }
        );
        VolleyHelper volleyHelper = new VolleyHelper(getContext());
        volleyHelper.add(getDriverDataRequest);
    }
}
