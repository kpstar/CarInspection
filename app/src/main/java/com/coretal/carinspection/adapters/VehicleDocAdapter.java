package com.coretal.carinspection.adapters;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import com.coretal.carinspection.R;

import java.util.ArrayList;

public class VehicleDocAdapter extends RecyclerView.Adapter<VehicleDocAdapter.ViewHolder> {

    private Context context;
    private ArrayList<DocContent> docs;

    public VehicleDocAdapter(Context context, ArrayList<DocContent> docs) {
        this.context = context;
        this.docs = docs;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.insdoc_content, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DocContent content = docs.get(position);
        if (position == 0) {
            holder.docLayout.setBackgroundColor(context.getColor(R.color.colorPrimary));
        }

        holder.docId.setText(content.id);
        holder.docMonth.setText(content.month);
        holder.docType.setText(content.type);
    }

    @Override
    public int getItemCount() {
        return docs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout docLayout;
        TextView docId, docMonth, docType;

        ViewHolder(View itemView) {
            super(itemView);

            docId = itemView.findViewById(R.id.docId);
            docMonth = itemView.findViewById(R.id.docMonth);
            docType = itemView.findViewById(R.id.docType);
            docLayout = itemView.findViewById(R.id.docLayout);

            docLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(context, docId.getText().toString(), Toast.LENGTH_SHORT).show();
        }
    }


    public static class DocContent implements Parcelable {

        public String id;
        public String month;
        public String type;
        public static final Creator<DocContent> CREATOR = new Creator<DocContent>() {
            @Override
            public DocContent createFromParcel(Parcel in) {
                return new DocContent(in);
            }

            @Override
            public DocContent[] newArray(int size) {
                return new DocContent[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(month);
            dest.writeString(type);
        }

        public DocContent(Parcel in) {
            id = in.readString();
            month = in.readString();
            type = in.readString();
        }

        public DocContent(String id, String month, String type) {
            this.id = id;
            this.month = month;
            this.type = type;
        }
    }
}
