package com.box.androidsdk.share.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;

import java.io.Serializable;
import java.util.ArrayList;


public class CollaborationRolesDialog extends DialogFragment implements Button.OnClickListener, DialogInterface.OnClickListener {

    protected static final String ARGS_USER_ID = "argsUserId";
    protected static final String ARGS_ROLES = "argsRoles";
    protected static final String ARGS_SELECTED_ROLE = "argsSelectedRole";
    protected static final String ARGS_TITLE = "argsTitle";
    protected static final String ARGS_ALLOW_REMOVE = "argsAllowRemove";
    protected static final String ARGS_SERIALIZABLE_EXTRA = "argsTargetId";

    protected BoxSession mSession;
    protected RadioGroup mRadioGroup;

    protected BoxCollaboration.Role[] mRoles;
    protected BoxCollaboration.Role mSelectedRole;
    protected boolean mAllowRemove;
    protected boolean mIsRemoveCollaborationSelected;
    protected Serializable mExtra;
    protected ArrayList<RadioButton> mRolesOptions = new ArrayList<RadioButton>();

    protected OnRoleSelectedListener mOnRoleSelectedListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String userId = getArguments().getString(ARGS_USER_ID);
        String title = getArguments().getString(ARGS_TITLE);
        mRoles = (BoxCollaboration.Role[]) getArguments().getSerializable(ARGS_ROLES);
        mSelectedRole = (BoxCollaboration.Role) getArguments().getSerializable(ARGS_SELECTED_ROLE);
        mAllowRemove = getArguments().getBoolean(ARGS_ALLOW_REMOVE);
        mExtra = getArguments().getSerializable(ARGS_SERIALIZABLE_EXTRA);
        mSession = new BoxSession(getActivity(), userId);

        // Create AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_collaboration_roles_dialog, null);
        builder.setView(view)
            .setNegativeButton(R.string.box_sharesdk_cancel, this)
            .setPositiveButton(R.string.box_sharesdk_ok, this);


        // Set the dialog title
        TextView collaboratorTitle = (TextView) view.findViewById(R.id.collaborator_role_title);
        collaboratorTitle.setText(title);

        // Initialize available roles
        mRadioGroup = (RadioGroup) view.findViewById(R.id.collaborator_roles_group);
        addRolesToView(mRoles);

        return builder.create();
    }

    private void addRolesToView(BoxCollaboration.Role[] roles) {
        LinearLayout rolesLayout = new LinearLayout(getActivity());
        rolesLayout.setOrientation(LinearLayout.VERTICAL);
        mRadioGroup.addView(rolesLayout);
        for (BoxCollaboration.Role role : roles) {

            View radioView = getActivity().getLayoutInflater().inflate(R.layout.radio_item_roles, null);
            TextView rolesName = (TextView) radioView.findViewById(R.id.roles_name);
            rolesName.setText(CollaborationUtils.getRoleName(getActivity(), role));
            TextView rolesDescription = (TextView) radioView.findViewById(R.id.roles_description);
            rolesDescription.setText(CollaborationUtils.getRoleDescription(getActivity(), role));
            LinearLayout rolesTextLayout = (LinearLayout) radioView.findViewById(R.id.roles_text_layout);
            rolesTextLayout.setTag(role);
            rolesTextLayout.setOnClickListener(this);
            RadioButton rolesRadio = (RadioButton) radioView.findViewById(R.id.roles_radio);
            rolesRadio.setTag(role);
            if (role == mSelectedRole) {
                rolesRadio.setChecked(true);
            }
            rolesRadio.setOnClickListener(this);
            mRolesOptions.add(rolesRadio);
            rolesLayout.addView(radioView);
        }

        if (mAllowRemove) {
            // Add remove person option
            View radioView = getActivity().getLayoutInflater().inflate(R.layout.radio_item_remove, null);
            TextView rolesText = (TextView) radioView.findViewById(R.id.roles_name);
            rolesText.setTag(null);
            rolesText.setOnClickListener(this);
            RadioButton rolesRadio = (RadioButton) radioView.findViewById(R.id.roles_radio);
            rolesRadio.setTag(null);
            rolesRadio.setOnClickListener(this);
            mRolesOptions.add(rolesRadio);
            rolesLayout.addView(radioView);
        }
    }

    public static CollaborationRolesDialog newInstance(BoxCollaboration.Role[] roles, BoxCollaboration.Role selectedRole, String title, boolean allowRemove, Serializable serializableExtra) {
        CollaborationRolesDialog dialog = new CollaborationRolesDialog();

        Bundle b = new Bundle();
        b.putSerializable(ARGS_ROLES, roles);
        b.putSerializable(ARGS_SELECTED_ROLE, selectedRole);
        b.putString(ARGS_TITLE, title);
        b.putBoolean(ARGS_ALLOW_REMOVE, allowRemove);
        b.putSerializable(ARGS_SERIALIZABLE_EXTRA, serializableExtra);
        dialog.setArguments(b);

        return dialog;
    }

    public void onClick(View v) {
        BoxCollaboration.Role selectedRole = (BoxCollaboration.Role) v.getTag();
        for (RadioButton radio : mRolesOptions) {
            BoxCollaboration.Role role = (BoxCollaboration.Role) radio.getTag();
            boolean shouldCheck = selectedRole == role ? true : false;
            radio.setChecked(shouldCheck);
            if (shouldCheck) {
                mSelectedRole = role;
            }
        }
        mIsRemoveCollaborationSelected = selectedRole == null;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                mOnRoleSelectedListener.onRoleSelected(this);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
            default:
                // Do nothing
                break;
        }
    }

    public void setOnRoleSelectedListener(OnRoleSelectedListener listener) {
        mOnRoleSelectedListener = listener;
    }

    public BoxCollaboration.Role getSelectedRole() {
        return mSelectedRole;
    }

    public boolean getIsRemoveCollaborationSelected() {
        return mIsRemoveCollaborationSelected;
    }

    public Serializable getSerializableExtra() {
        return mExtra;
    }

    public interface OnRoleSelectedListener {
        public void onRoleSelected(CollaborationRolesDialog rolesDialog);
    }
}