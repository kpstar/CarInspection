package com.coretal.carinspection.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.DrawableHelper;
import com.coretal.carinspection.utils.MyPreference;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.ArrayList;
import java.util.List;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Created by Kangtle_R on 1/19/2018.
 */

public class InspectionRecyclerViewAdapter
                extends ExpandableRecyclerViewAdapter<InspectionRecyclerViewAdapter.HeaderViewHolder, InspectionRecyclerViewAdapter.ContentViewHolder> {
    private final Context context;
    private MyPreference myPref;

    public InspectionRecyclerViewAdapter(Context context, List<? extends ExpandableGroup> groups) {
        super(groups);
        this.context = context;
        myPref = new MyPreference(context);
    }

    @Override
    public HeaderViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inspection_header, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public ContentViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inspection_content, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(ContentViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)holder.itemView.getLayoutParams();
        if(childIndex == group.getItemCount() - 1){
            params.setMargins(0, 0, 0, 50);
        }else {
            params.setMargins(0, 0, 0, 0);
        }
        final SectionContent content = ((SectionHeader) group).getItems().get(childIndex);
        holder.setContent(content);

    }

    @Override
    public void onBindGroupViewHolder(HeaderViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setHeader(group);
    }

    public class HeaderViewHolder extends GroupViewHolder implements CompoundButton.OnCheckedChangeListener {

        private TextView sectionTitleEdit;
        private ImageView arrow;
        private CheckBox checkBox;

        private SectionHeader sectionHeader;

        private boolean onBind;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            DrawableHelper.setColor(itemView.getBackground(), myPref.getColorButton());

            sectionTitleEdit = itemView.findViewById(R.id.section_edit);
            arrow = itemView.findViewById(R.id.list_item_genre_arrow);
            checkBox = itemView.findViewById(R.id.checkBox);
            checkBox.setOnCheckedChangeListener(this);
            int states[][] = {{android.R.attr.state_checked}, {}};
            int colors[] = {myPref.getColorCheck(), myPref.getColorUncheck()};
            CompoundButtonCompat.setButtonTintList(checkBox, new ColorStateList(states, colors));
        }

        public void setHeader(ExpandableGroup header) {
            sectionHeader = (SectionHeader) header;
            sectionTitleEdit.setText(sectionHeader.getTitle());
            onBind = true;
            checkBox.setChecked(sectionHeader.isChecked);
            onBind = false;
        }

        @Override
        public void expand() {
            animateExpand();
        }

        @Override
        public void collapse() {
            animateCollapse();
        }

        private void animateExpand() {
            RotateAnimation rotate =
                    new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        private void animateCollapse() {
            RotateAnimation rotate =
                    new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (onBind) return;
            sectionHeader.isChecked = isChecked;
            for (SectionContent item:sectionHeader.getItems()) {
                item.isChecked = isChecked;
            }
            InspectionRecyclerViewAdapter.this.notifyDataSetChanged();
        }
    }

    public class ContentViewHolder extends ChildViewHolder implements CompoundButton.OnCheckedChangeListener, TextWatcher {

        private CheckBox checkBox;
        private TextView editTextTitle;
        private TextView editTextSubTitle;
        private EditText editTextRemarks;

        private SectionContent content;

        private boolean onBind;

        public ContentViewHolder(View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            editTextTitle = itemView.findViewById(R.id.title);
            editTextSubTitle = itemView.findViewById(R.id.sub_title);
            editTextRemarks = itemView.findViewById(R.id.remarks);
            checkBox.setOnCheckedChangeListener(this);
            editTextRemarks.addTextChangedListener(this);
            int states[][] = {{android.R.attr.state_checked}, {}};
            int colors[] = {myPref.getColorCheck(), myPref.getColorUncheck()};
            CompoundButtonCompat.setButtonTintList(checkBox, new ColorStateList(states, colors));
        }

        public void setContent(SectionContent content) {
            this.content = content;
            onBind = true;
            checkBox.setChecked(content.isChecked);
            onBind = false;
            editTextTitle.setText(content.subsectionCaption);
            editTextSubTitle.setText(content.questionCaption);
            editTextRemarks.setText(content.questionNotes);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (onBind) return;
            content.isChecked = isChecked;
            boolean headerChecked = true;
            for (SectionContent item: content.sectionHeader.sectionContents) {
                if (!item.isChecked) {
                    headerChecked = false;
                    break;
                }
            }
            content.sectionHeader.isChecked = headerChecked;
            InspectionRecyclerViewAdapter.this.notifyDataSetChanged();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            content.questionNotes = s.toString();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public static class SectionHeader extends ExpandableGroup<SectionContent> {
        public String sectionId;
        public String sectionCaption;
        public ArrayList<SectionContent> sectionContents;
        public boolean isChecked = false;

        public SectionHeader(String sectionId, String sectionCaption, ArrayList<SectionContent> sectionContents, boolean isChecked) {
            super(sectionCaption, sectionContents);
            this.sectionId = sectionId;
            this.sectionCaption = sectionCaption;
            this.sectionContents = sectionContents;
            this.isChecked = isChecked;
            for (SectionContent sectionContent: sectionContents) {
                sectionContent.sectionHeader = this;
            }
        }
    }

    public static class SectionContent implements Parcelable {

        public SectionHeader sectionHeader;
        public String subsectionId;
        public String subsectionCaption; // subsection caption
        public String questionId;
        public String questionCaption; //question caption
        public String questionNotes; //questionNotes
        public boolean isChecked;

        public SectionContent(String subsectionId, String subsectionCaption, String questionId, String questionCaption, String questionNotes, boolean isChecked) {
            this.subsectionId = subsectionId;
            this.subsectionCaption = subsectionCaption;
            this.questionId = questionId;
            this.questionCaption = questionCaption;
            this.questionNotes = questionNotes;
            this.isChecked = isChecked;
        }

        public SectionContent(Parcel in){
            this.subsectionId = in.readString();
            this.subsectionCaption = in.readString();
            this.questionId = in.readString();
            this.questionCaption = in.readString();
            this.questionNotes = in.readString();
            this.isChecked = in.readByte() == 1;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(subsectionId);
            dest.writeString(subsectionCaption);
            dest.writeString(questionId);
            dest.writeString(questionCaption);
            dest.writeString(questionNotes);
            dest.writeByte((byte) (isChecked ? 1 : 0));
        }

        public final Creator<SectionContent> CREATOR = new Creator<SectionContent>() {
            @Override
            public SectionContent createFromParcel(Parcel in) {
                return new SectionContent(in);
            }

            @Override
            public SectionContent[] newArray(int size) {
                return new SectionContent[size];
            }
        };
    }
}
