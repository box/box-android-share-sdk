package com.box.androidsdk.share.usx.views;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.internal.models.BoxInvitee;
import com.eclipsesource.json.JsonObject;
import com.tokenautocomplete.TokenCompleteTextView;


public class ChipCollaborationView extends TokenCompleteTextView<BoxInvitee> {

    public ChipCollaborationView(Context context) {
        super(context);
        setLongClickable(true);
    }

    public ChipCollaborationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLongClickable(true);
    }

    public ChipCollaborationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLongClickable(true);

    }

    @Override
    protected View getViewForObject(BoxInvitee person) {

        LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        RelativeLayout view = (RelativeLayout)layoutInflater.inflate(R.layout.usx_view_chip_collaboration, (ViewGroup) ChipCollaborationView.this.getParent(), false);

        TextView tvName = ((TextView) view.findViewById(R.id.name));
        tvName.setText(person.getName());

        TextView tvInitials = (TextView) view.findViewById(R.id.collaborator_initials);
        SdkUtils.setInitialsThumb(getContext(), tvInitials, person.getName());
        return view;
    }

    @Override
    protected BoxInvitee defaultObject(String completionText) {
        completionText = completionText.replace(" ", "");

        JsonObject jsonObject = new JsonObject();
        jsonObject.add(BoxInvitee.FIELD_NAME, completionText);
        jsonObject.add(BoxInvitee.FIELD_EMAIL, completionText);

        BoxInvitee invitee = new BoxInvitee();
        invitee.createFromJson(jsonObject);
        return invitee;
    }
}
