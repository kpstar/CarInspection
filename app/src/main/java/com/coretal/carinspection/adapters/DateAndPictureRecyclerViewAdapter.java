package com.coretal.carinspection.adapters;

import android.app.Activity;
import android.graphics.Color;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.coretal.carinspection.R;
import com.coretal.carinspection.dialogs.PhotoViewDialog;
import com.coretal.carinspection.models.DateAndPicture;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.MyPreference;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DateAndPictureRecyclerViewAdapter extends RecyclerView.Adapter<DateAndPictureRecyclerViewAdapter.ViewHolder> {

    private final List<DateAndPicture> mValues;
    private final MyPreference myPref;
    private final String category;
    private final Map<String, String> fileTypes;
    private AppCompatActivity activity;
    private Callback callback;


    public DateAndPictureRecyclerViewAdapter(Activity activity, List<DateAndPicture> items, Callback callback, String category) {
        this.mValues = items;
        this.activity = (AppCompatActivity) activity;
        this.callback = callback;
        this.category = category;
        this.fileTypes = Contents.JsonFileTypesEnum.getTypesByCategory(category);
        this.myPref = new MyPreference(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_dateandpicture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        String type = mValues.get(position).type;
        holder.typeEdit.setText(fileTypes.get(type));
        holder.dateEdit.setText(Contents.DATE_PREFIX + mValues.get(position).dateStr);
        Date currentDate = Calendar.getInstance().getTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, myPref.get_conf_app_days_due());
        Date appDueDate = calendar.getTime();

        Date expireDate = mValues.get(position).date;

        if (appDueDate.after(expireDate)) {
            holder.dateEdit.setTextColor(Color.RED);
        }else{
            holder.dateEdit.setTextColor(Color.BLACK);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClickItem(position);
            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = PhotoViewDialog.newInstance(holder.mItem.pictureURL);
                fragment.show(activity.getSupportFragmentManager(), "photo_view_dialog");
            }
        });
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.ic_camera_48);
        if (holder.mItem.pictureURL.contains("http")) {
            GlideUrl glideUrl = new GlideUrl(holder.mItem.pictureURL, new LazyHeaders.Builder().addHeader(Contents.HEADER_KEY, Contents.TOKEN).build());
            Glide.with(activity)
                    .load(glideUrl)
                    .apply(requestOptions)
                    .into(holder.imageView);
        } else {
            Glide.with(activity)
                    .load(holder.mItem.pictureURL)
                    .apply(requestOptions)
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView imageView;
        public final TextView typeEdit;
        public final TextView dateEdit;
        public DateAndPicture mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            typeEdit = view.findViewById(R.id.type_edit);
            dateEdit = view.findViewById(R.id.date_edit);
            imageView = (ImageView) view.findViewById(R.id.picture);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + typeEdit.getText() + "'";
        }
    }

    public interface Callback {
        public void onClickItem(int position);
    }
}
