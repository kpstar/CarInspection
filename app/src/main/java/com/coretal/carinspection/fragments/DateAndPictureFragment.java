package com.coretal.carinspection.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.coretal.carinspection.activities.MainActivity;
import com.coretal.carinspection.services.API;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.VolleyHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.DateAndPictureRecyclerViewAdapter;
import com.coretal.carinspection.dialogs.DateAndPictureDialog;
import com.coretal.carinspection.models.DateAndPicture;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.MyPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class DateAndPictureFragment extends Fragment implements DateAndPictureDialog.Callback, DateAndPictureRecyclerViewAdapter.Callback, API.Callback {

    private static final String ARG_CATEGORY = "category";
    private static final String ARG_JSON_STRING = "json_string";
    private ArrayList<DateAndPicture> dateAndPictures;
    private ArrayList<DateAndPicture> deletedItems;
    private DateAndPictureRecyclerViewAdapter adapter;
    private String category;
    private MyPreference myPref;
    private API api;
    private DateAndPicture removeItem;
    public int removeIndex;
    public VolleyHelper volleyHelper;
    public DateAndPicture item;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DateAndPictureFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DateAndPictureFragment newInstance(String catetory, String jsonString) {
        DateAndPictureFragment fragment = new DateAndPictureFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, catetory);
        args.putString(ARG_JSON_STRING, jsonString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dateAndPictures = new ArrayList<>();
        deletedItems = new ArrayList<>();
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY);
            String jsonString = getArguments().getString(ARG_JSON_STRING);
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    DateAndPicture item = new DateAndPicture(jsonObject);
                    if(item.status.equals(DateAndPicture.STATUS_DELETED)){
                        deletedItems.add(item);
                    }else{
                        dateAndPictures.add(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dateandpicture_list, container, false);

        Context context = view.getContext();
        api = new API(context, DateAndPictureFragment.this);
        final RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new DateAndPictureRecyclerViewAdapter(getActivity(), dateAndPictures, this, category);
        recyclerView.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                AlertHelper.question(getContext(), getString(R.string.delete), getString(R.string.are_you_sure_delete), getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        synchronized(adapter){
                            removeIndex = viewHolder.getAdapterPosition();
                            if (removeIndex >= dateAndPictures.size()) return;
                            removeItem = dateAndPictures.remove(removeIndex);
                            if (!removeItem.status.equals(DateAndPicture.STATUS_NEW)){
                                if (removeItem.status.equals(DateAndPicture.STATUS_CHANGED)){
                                    removeItem.pictureId = removeItem.oldPictureId;
                                    removeItem.oldPictureId = "";
                                }
                            }
                            api = new API(getContext(), DateAndPictureFragment.this);
                            api.removePicture(removeItem);
                        }
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        synchronized(adapter){
                            int index = viewHolder.getAdapterPosition();
                            adapter.notifyItemChanged(index);
                        }
                        dialog.dismiss();
                    }
                });
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton addFab = (FloatingActionButton)view.findViewById(R.id.fab_add);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = DateAndPictureDialog.newInstance(category, DateAndPictureFragment.this);
                fragment.show(getFragmentManager(), "dialog_date_and_picture");
            }
        });
        myPref = new MyPreference(getContext());
        addFab.setBackgroundTintList(ColorStateList.valueOf(myPref.getColorButton()));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class UploadImageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            SimpleMultiPartRequest multiPartRequest = new SimpleMultiPartRequest(
                    Request.Method.POST,
                    Contents.API_SUBMIT_PICTURE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
//                        progressDialog.dismiss();
                            try {
                                JSONObject json = new JSONObject(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    params.put(Contents.HEADER_KEY, Contents.TOKEN);
                    return params;
                }
            };
            if (item == null) return "Failed";
            volleyHelper = new VolleyHelper(getContext());
            multiPartRequest.addStringParam("pictureId", item.pictureId);
            multiPartRequest.addStringParam("phoneNumber", Contents.PHONE_NUMBER);
            multiPartRequest.addStringParam("pictureDate", item.dateStr);
            multiPartRequest.addStringParam("pictureType", item.type);
            if (category == Contents.JsonFileTypesEnum.CATEGORIE_DRIVER) { // DRIVERS
                multiPartRequest.addStringParam("ModuleEnum", "DRIVERS");
                multiPartRequest.addStringParam("plateOrId", Contents.DRIVER_ID);
            } else if (category == Contents.JsonFileTypesEnum.CATEGORIE_TRAILER) { // TRUCKS
                multiPartRequest.addStringParam("ModuleEnum", "TRUCKS");
                multiPartRequest.addStringParam("plateOrId", Contents.SECOND_VEHICLE_NUMBER);
            } else {
                multiPartRequest.addStringParam("ModuleEnum", "TRUCKS");
                multiPartRequest.addStringParam("plateOrId", Contents.CURRENT_VEHICLE_NUMBER);
            }
            multiPartRequest.addFile("file", item.pictureURL);

            volleyHelper.add(multiPartRequest);
            return "Failed";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @Override
    public void onDoneDateAndPictureDialog(DateAndPicture item, boolean isNew) {
        Log.d("Kangtle", "on done date and picture dialog");

        this.item = item;
        if(isNew) {
            dateAndPictures.add(item);
        }
        adapter.notifyDataSetChanged();
        new UploadImageTask().execute("");
    }

    @Override
    public void onClickItem(int position) {
        DateAndPictureDialog fragment = DateAndPictureDialog.newInstance(category, DateAndPictureFragment.this);
        fragment.editingItem = dateAndPictures.get(position);
        fragment.show(getFragmentManager(), "dialog_date_and_picture");
    }

    public JSONArray getOutput(){
        JSONArray jsonArray = new JSONArray();
        for (DateAndPicture item : dateAndPictures) {
            try {
                jsonArray.put(item.getJSONObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (DateAndPicture item : deletedItems) {
            if(item.status.equals(DateAndPicture.STATUS_NEW)) continue;
            try {
                jsonArray.put(item.getJSONObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonArray;
    }

    @Override
    public void onProcessImage(int okay, String error) {
        removeItem.status = DateAndPicture.STATUS_DELETED;
        deletedItems.add(removeItem);
        if (error.isEmpty()) {
            adapter.notifyItemRemoved(removeIndex);
            adapter.notifyItemRangeChanged(removeIndex, dateAndPictures.size());
            Toast.makeText(getContext(), "Removed Ok!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onProcessSubmit(String error) {

    }
}
