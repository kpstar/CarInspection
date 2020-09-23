package com.coretal.carinspection.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.InspectionRecyclerViewAdapter;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.JsonHelper;
import com.coretal.carinspection.utils.MyPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrailerInspectionFragment extends Fragment {


    private MyPreference myPreference;
    private InspectionRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private FragmentManager fragmentManager;
    private View filterView;
    private LinearLayout layout;

    public ArrayList<InspectionRecyclerViewAdapter.SectionHeader> sectionHeaders;
    public ArrayList<InspectionRecyclerViewAdapter.SectionHeader> searchedSectionHeaders;

    public TrailerInspectionFragment() {
        // Required empty public constructor
    }

    public static TrailerInspectionFragment newInstance() {return new TrailerInspectionFragment();}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_trailer_inspection, container, false);

        myPreference = new MyPreference(getContext());
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        searchEditText = view.findViewById(R.id.search);
        filterView = view.findViewById(R.id.disableSearch);
        layout = view.findViewById(R.id.layout);

        sectionHeaders = new ArrayList<>();
        searchedSectionHeaders = new ArrayList<>();

        CheckBox chkSend = view.findViewById(R.id.trailerCheck);
        if (Contents.TRUCK_TYPE == 2) {
            layout.setVisibility(View.GONE);
        }

        chkSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recyclerView.setEnabled(isChecked);
                if (isChecked) {
                    recyclerView.setAlpha(1);
                    filterView.setClickable(false);
                } else {
                    recyclerView.setAlpha(0.7F);
                    filterView.setClickable(true);
                }
                Contents.IS_TRAILER_CHECKED = isChecked;
                adapter.isClickable = isChecked;
            }
        });

        fragmentManager = getFragmentManager();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        setValuesFromFile();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Contents.IS_STARTED_INSPECTION) return;
                searchedSectionHeaders.clear();
                if (s == "") {
                    searchedSectionHeaders.addAll(sectionHeaders);
                } else {
                    for (InspectionRecyclerViewAdapter.SectionHeader header: sectionHeaders) {
                        if (header.getTitle().toLowerCase().contains(s)){
                            searchedSectionHeaders.add(header);
                            continue;
                        } else {
                            for (InspectionRecyclerViewAdapter.SectionContent content: header.sectionContents) {
                                if (content.getQuestionCaption().toLowerCase().contains(s)) {
                                    searchedSectionHeaders.add(header);
                                    break;
                                }
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("Kangtle", "on hidden changed Inspection fragment " + hidden);
        if (!Contents.IS_STARTED_INSPECTION) return;
        if(!hidden){
            setValuesFromFile();
        }else{
            saveValuesToFile();
        }
    }

    private void saveValuesToFile(){
        JSONObject jsonObject = getOutput();
        JsonHelper.saveJsonObject(jsonObject, Contents.JsonTrailerInspectionJson.FILE_PATH);
    }

    private void setValuesFromFile() {
        if(!Contents.IS_STARTED_INSPECTION || adapter != null) return;

        makeSectionContents();

        searchedSectionHeaders.clear();
        searchedSectionHeaders.addAll(sectionHeaders);
        adapter = new InspectionRecyclerViewAdapter(getContext(), fragmentManager, searchedSectionHeaders);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(500);
    }

    private void makeSectionContents(){
        sectionHeaders.clear();

        JSONObject inspectionDataJson = JsonHelper.readJsonFromFile(Contents.JsonTrailerInspectionJson.FILE_PATH);

        if (inspectionDataJson == null) return;
        try {
            JSONArray sectionsArray = inspectionDataJson.getJSONArray(Contents.JsonTrailerInspectionJson.SECTIONS);
            for (int sectionIndex=0; sectionIndex < sectionsArray.length(); sectionIndex++){
                JSONObject sectionObject = sectionsArray.getJSONObject(sectionIndex);
                String sectionId = sectionObject.getString(Contents.JsonTrailerInspectionJson.IDENTIFIER);
                String sectionCaption = sectionObject.getString(Contents.JsonTrailerInspectionJson.CAPTION);
                int sectionOrder = sectionObject.getInt(Contents.JsonTrailerInspectionJson.ORDER);
                ArrayList<InspectionRecyclerViewAdapter.SectionContent> sectionContents = new ArrayList<>();
                JSONArray questionsArray = sectionObject.getJSONArray(Contents.JsonTrailerInspectionJson.QUESTIONS);
                for(int questionIndex=0; questionIndex<questionsArray.length();questionIndex++){
                    JSONObject questionObject = questionsArray.getJSONObject(questionIndex);
                    String questionId = questionObject.getString(Contents.JsonTrailerInspectionJson.IDENTIFIER);
                    String questionCaption = questionObject.getString(Contents.JsonTrailerInspectionJson.CAPTION);
                    String questionNotes = questionObject.optString(Contents.JsonTrailerInspectionJson.NOTES);
                    int questionOrder = questionObject.getInt(Contents.JsonTrailerInspectionJson.ORDER);
                    String questionStatus = questionObject.optString(Contents.JsonTrailerInspectionJson.CHECKED);
                    boolean isChecked = questionStatus.equals("true");

                    InspectionRecyclerViewAdapter.SectionContent sectionContent =
                            new InspectionRecyclerViewAdapter.SectionContent(
                                    questionId,
                                    questionCaption,
                                    questionNotes,
                                    questionOrder,
                                    isChecked
                            );
                    sectionContents.add(sectionContent);
                }

                boolean confCheck = myPreference.get_conf_chek_box_submit();
                InspectionRecyclerViewAdapter.SectionHeader sectionHeader =
                        new InspectionRecyclerViewAdapter.SectionHeader(sectionId, sectionCaption, sectionOrder, sectionContents, true);
                sectionHeaders.add(sectionHeader);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getOutput(){
        JSONObject allSectionObject = new JSONObject();
        try {
            JSONArray sectionsArray = new JSONArray();
            allSectionObject.put(Contents.JsonTrailerInspectionJson.SECTIONS, sectionsArray);
            for (InspectionRecyclerViewAdapter.SectionHeader header: sectionHeaders){
                String sectionId = header.sectionId;
                String sectionCaption = header.sectionCaption;
                int sectionOrder = header.sectionOrder;
                ArrayList<InspectionRecyclerViewAdapter.SectionContent> sectionContents = header.sectionContents;

                JSONObject sectionObject = new JSONObject();
                sectionObject.put(Contents.JsonTrailerInspectionJson.IDENTIFIER, sectionId);
                sectionObject.put(Contents.JsonTrailerInspectionJson.CAPTION, sectionCaption);
                sectionObject.put(Contents.JsonTrailerInspectionJson.ORDER, sectionOrder);

                JSONArray questionArray = new JSONArray();
                for (InspectionRecyclerViewAdapter.SectionContent sectionContent: sectionContents){
                    String questionId = sectionContent.questionId;
                    String questionCaption = sectionContent.questionCaption;
                    String questionNotes = sectionContent.questionNotes;
                    int questionOrder = sectionContent.questionOrder;
                    boolean isChecked = sectionContent.isChecked;

                    JSONObject questionObject = new JSONObject();
                    questionObject.put(Contents.JsonTrailerInspectionJson.IDENTIFIER, questionId);
                    questionObject.put(Contents.JsonTrailerInspectionJson.CAPTION, questionCaption);
                    questionObject.put(Contents.JsonTrailerInspectionJson.NOTES, questionNotes);
                    questionObject.put(Contents.JsonTrailerInspectionJson.CHECKED, isChecked);
                    questionObject.put(Contents.JsonTrailerInspectionJson.ORDER, questionOrder);

                    questionArray.put(questionObject);
                }
                sectionObject.put(Contents.JsonTrailerInspectionJson.QUESTIONS, questionArray);
                sectionsArray.put(sectionObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allSectionObject;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveValuesToFile();
    }

}
