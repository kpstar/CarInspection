package com.coretal.carinspection.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.coretal.carinspection.R;
import com.coretal.carinspection.adapters.SubmissionTableViewAdapter;
import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DateHelper;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.evrencoskun.tableview.TableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubmissionFragment extends Fragment {


    private SubmissionTableViewAdapter mTableViewAdapter;
    private DBHelper dbHelper;
    private MyPreference myPref;
    private Spinner actionSpinner;
    private List<List<SubmissionTableViewAdapter.Cell>> cellList;
    public SubmissionFragment() {
        // Required empty public constructor
    }

    public static SubmissionFragment newInstance() {
        return new SubmissionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_submission, container, false);
        TableView tableView = view.findViewById(R.id.submission_list);
        mTableViewAdapter = new SubmissionTableViewAdapter(getContext());
        tableView.setAdapter(mTableViewAdapter);

        actionSpinner = view.findViewById(R.id.action_spinner);
        String[] actions = {getString(R.string.resend), getString(R.string.clear), getString(R.string.send)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, actions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(adapter);

        dbHelper = new DBHelper(getContext());
        myPref = new MyPreference(getContext());
        List<Submission> submissions = dbHelper.getAllSubmissions();

        cellList = getCellListForAllSubmissions(submissions); // getCellList();
        List<SubmissionTableViewAdapter.RowHeader> rowHeaders = createRowHeaderList(submissions.size());
        List<SubmissionTableViewAdapter.ColumnHeader> columnHeaders = createColumnHeaderModelList();
//        mTableViewAdapter.setAllItems(columnHeaders, rowHeaders, cellList);
        mTableViewAdapter.setRowHeaderItems(rowHeaders);
        mTableViewAdapter.setColumnHeaderItems(columnHeaders);
        mTableViewAdapter.setCellItems(cellList);

        Button submitButton = view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertHelper.question(getActivity(), "Confirm", "Are you sure?", "Yes", "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (actionSpinner.getSelectedItemPosition()){
                            case 0: //Reset
                                Log.d("Kangtle", "Reset num try");
                                dbHelper.resetNumtry();
                                reloadSubmissions();
                                break;
                            case 1:
                                Log.d("Kangtle", "Clear submissions");
                                sendSubmissionsByEmail();
                                dbHelper.clearSubmissions();
                                reloadSubmissions();
                                break;
                            case 2: //Email
                                sendSubmissionsByEmail();
                                break;
                            default:
                                Log.d("Kangtle", "no action");
                                break;
                        }
                    }
                }, null);
            }
        });

        DrawableHelper.setColor(submitButton.getBackground(), myPref.getColorButton());

        TextView headerlabel = view.findViewById(R.id.headerLabel);
        DrawableHelper.setColor(headerlabel.getBackground(), myPref.getColorButton());

        return view;
    }

    private void reloadSubmissions(){
        List<Submission> submissions = dbHelper.getAllSubmissions();
        List<List<SubmissionTableViewAdapter.Cell>> cellList = getCellListForAllSubmissions(submissions); // getCellList();
        List<SubmissionTableViewAdapter.RowHeader> rowHeaders = createRowHeaderList(submissions.size());
        mTableViewAdapter.setRowHeaderItems(rowHeaders);
        mTableViewAdapter.setCellItems(cellList);
        mTableViewAdapter.notifyDataSetChanged();
    }

    private void sendSubmissionsByEmail(){
        Log.d("Kangtle", "Send by email");
        String[] monthList = myPref.get_conf_months();
        StringBuilder emailBody = new StringBuilder("Id\tVehicle\tMonth\tDate\tType\tStatus\tError Detail\tNum Try\tNotes\tStarted At\tEnded At\n");
        List<Submission> subs = dbHelper.getAllSubmissions();
        for (Submission sub: subs) {
            emailBody.append(String.format(Locale.US, "%d\t%s\t%s\t%s\t%s\t%s\t%s\t%d\t%s\t%s\t%s\n",
                    sub.id, sub.vehiclePlate, monthList[sub.month], DateHelper.dateToString(sub.date),
                    sub.type, sub.status, sub.errorDetail, sub.numTry, sub.notes,
                    DateHelper.datetimeToString(sub.startedAt),
                    DateHelper.datetimeToString(sub.endedAt)
            ));
        }

        BackgroundMail.newBuilder(getContext())
                .withUsername(myPref.get_conf_email_user())
                .withPassword(myPref.get_conf_email_password())
                .withMailto(myPref.get_conf_email_target_email())
                .withType(BackgroundMail.TYPE_PLAIN)
                .withSubject(myPref.get_conf_email_subject())
                .withBody(emailBody.toString())
                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("Kangtle", "successfully send email");
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        Log.d("Kangtle", "failed to send email");
                    }
                })
                .send();
    }

    private List<List<SubmissionTableViewAdapter.Cell>> getCellListForAllSubmissions(List<Submission> submissions) {
        List<List<SubmissionTableViewAdapter.Cell>> list = new ArrayList<>();

        for (Submission submission : submissions) {
            String[] monthList = myPref.get_conf_months();
            String selectedMonth = "No selected";
            if (submission.month >= 0) selectedMonth = monthList[submission.month];

            List<SubmissionTableViewAdapter.Cell> cellList = new ArrayList<>();

            cellList.add(new SubmissionTableViewAdapter.Cell(submission.id));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.vehiclePlate));
            cellList.add(new SubmissionTableViewAdapter.Cell(selectedMonth));
            cellList.add(new SubmissionTableViewAdapter.Cell(DateHelper.dateToString(submission.date, Contents.DEFAULT_DATE_FORMAT)));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.type));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.status));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.errorDetail));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.numTry));
            cellList.add(new SubmissionTableViewAdapter.Cell(submission.notes));
            cellList.add(new SubmissionTableViewAdapter.Cell(DateHelper.datetimeToString(submission.startedAt)));
            cellList.add(new SubmissionTableViewAdapter.Cell(DateHelper.datetimeToString(submission.endedAt)));

            list.add(cellList);
        }

        return list;
    }

    private List<SubmissionTableViewAdapter.ColumnHeader> createColumnHeaderModelList() {
        List<SubmissionTableViewAdapter.ColumnHeader> list = new ArrayList<>();

        // Create Column Headers
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.id)));
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.vehicle)));
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.month)));
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.date)));
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.type)));
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.status)));
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.error_detail)));
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.num_try)));
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.notes)));
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.started_at)));
        list.add(new SubmissionTableViewAdapter.ColumnHeader(getString(R.string.ended_at)));

        return list;
    }


    private List<SubmissionTableViewAdapter.RowHeader> createRowHeaderList(int rowCount) {
        List<SubmissionTableViewAdapter.RowHeader> list = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            // In this example, Row headers just shows the index of the TableView List.
            list.add(new SubmissionTableViewAdapter.RowHeader(String.valueOf(i + 1)));
        }
        return list;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
