package com.coretal.carinspection.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.coretal.carinspection.R;
import com.coretal.carinspection.dialogs.API_PhoneNumberDialog;
import com.coretal.carinspection.dialogs.RemarksDialog;
import com.coretal.carinspection.fragments.TruckInspectionFragment;
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
    private FragmentManager fragmentManager;
    public boolean isClickable = true;

    public InspectionRecyclerViewAdapter(Context context, FragmentManager fragmentManager, List<? extends ExpandableGroup> groups) {
        super(groups);
        this.context = context;
        this.fragmentManager = fragmentManager;
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
            if (! isClickable) {
                return;
            }
            RotateAnimation rotate =
                    new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        private void animateCollapse() {
            if (! isClickable) {
                return;
            }
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

    public class ContentViewHolder extends ChildViewHolder implements CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnClickListener, RemarksDialog.Callback {

        private CheckBox checkBox;
        private TextView editTextTitle;
        private TextView editTextSubTitle;
        private EditText editTextRemarks;
        private ImageButton btnRemarks;
        private DialogFragment fragment;

        private SectionContent content;

        private boolean onBind;

        public ContentViewHolder(View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            editTextTitle = itemView.findViewById(R.id.title);
            editTextSubTitle = itemView.findViewById(R.id.sub_title);
            editTextRemarks = itemView.findViewById(R.id.remarks);
            btnRemarks = itemView.findViewById(R.id.btn_remarks);

            btnRemarks.setOnClickListener(this);
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

        @Override
        public void onClick(View v) {

            fragment = RemarksDialog.newInstance(ContentViewHolder.this);
            ((DialogFragment) fragment).show(fragmentManager, "dialog_remarks");
        }

        @Override
        public void onSubmitRemarks(String remarks) {
            fragment.dismiss();
        }
    }

    public static class SectionHeader extends ExpandableGroup<SectionContent> {
        public String sectionId;
        public String sectionCaption;
        public int sectionOrder;
        public ArrayList<SectionContent> sectionContents;
        public boolean isChecked = false;

        public SectionHeader(String sectionId, String sectionCaption, int sectionOrder, ArrayList<SectionContent> sectionContents, boolean isChecked) {
            super(sectionCaption, sectionContents);
            this.sectionId = sectionId;
            this.sectionCaption = sectionCaption;
            this.sectionContents = sectionContents;
            this.sectionOrder = sectionOrder;
            this.isChecked = isChecked;
            for (SectionContent sectionContent: sectionContents) {
                sectionContent.sectionHeader = this;
            }
        }
    }

    public static class SectionContent implements Parcelable {

        public SectionHeader sectionHeader;
        public String questionId;
        public String questionCaption; //question caption
        public String questionNotes; //questionNotes
        public int questionOrder;
        public boolean isChecked;

        public SectionContent(String questionId, String questionCaption, String questionNotes, int questionOrder, boolean isChecked) {
            this.questionId = questionId;
            this.questionCaption = questionCaption;
            this.questionNotes = questionNotes;
            this.questionOrder = questionOrder;
            this.isChecked = isChecked;
        }

        public SectionContent(Parcel in){
            this.questionId = in.readString();
            this.questionCaption = in.readString();
            this.questionNotes = in.readString();
            this.questionOrder = in.readInt();
            this.isChecked = in.readByte() == 1;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
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
