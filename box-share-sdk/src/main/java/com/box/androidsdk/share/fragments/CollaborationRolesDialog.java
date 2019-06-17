package com.box.androidsdk.share.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;

import java.util.ArrayList;


public class CollaborationRolesDialog extends DialogFragment implements Button.OnClickListener, DialogInterface.OnClickListener {

    protected static final String ARGS_ROLES = "argsRoles";
    protected static final String ARGS_SELECTED_ROLE = "argsSelectedRole";
    protected static final String ARGS_NAME = "argsName";
    protected static final String ARGS_ALLOW_REMOVE = "argsAllowRemove";
    protected static final String ARGS_ALLOW_OWNER_ROLE = "argsAllowOwnerRole";
    protected static final String ARGS_SERIALIZABLE_EXTRA = "argsTargetId";
    static final String TAG = CollaborationRolesDialog.class.getName();

    protected RadioGroup mRadioGroup;

    protected ArrayList<BoxCollaboration.Role> mRoles;
    protected BoxCollaboration.Role mSelectedRole;
    protected boolean mAllowRemove;
    protected boolean mAllowOwnerRole;
    protected boolean mIsRemoveCollaborationSelected;
    protected BoxCollaboration mCollaboration;
    protected ArrayList<RadioButton> mRolesOptions = new ArrayList<RadioButton>();

    protected OnRoleSelectedListener mOnRoleSelectedListener;
    private String mCollaboratorName;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setRetainInstance(true);

        mCollaboratorName = getArguments().getString(ARGS_NAME);
        mRoles = (ArrayList<BoxCollaboration.Role>) getArguments().getSerializable(ARGS_ROLES);
        mSelectedRole = (BoxCollaboration.Role) getArguments().getSerializable(ARGS_SELECTED_ROLE);
        mAllowRemove = getArguments().getBoolean(ARGS_ALLOW_REMOVE);
        mAllowOwnerRole = getArguments().getBoolean(ARGS_ALLOW_OWNER_ROLE);
        mCollaboration = (BoxCollaboration)getArguments().getSerializable(ARGS_SERIALIZABLE_EXTRA);

        // Create AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ShareDialogTheme);
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_collaboration_roles, null);
        builder.setView(view)
            .setNegativeButton(R.string.box_sharesdk_cancel, this)
            .setPositiveButton(R.string.box_sharesdk_ok, this);


        // Set the dialog title
        TextView collaboratorTitle = (TextView) view.findViewById(R.id.collaborator_role_title);
        collaboratorTitle.setText(mCollaboratorName);

        // Initialize available roles
        mRadioGroup = (RadioGroup) view.findViewById(R.id.collaborator_roles_group);
        addRolesToView(mRoles);

        return builder.create();
    }

    private void addRolesToView(ArrayList<BoxCollaboration.Role> roles) {
        LinearLayout rolesLayout = new LinearLayout(getActivity());
        rolesLayout.setOrientation(LinearLayout.VERTICAL);
        mRadioGroup.addView(rolesLayout);
        for (BoxCollaboration.Role role : BoxCollaboration.Role.values()) {

            if (role == BoxCollaboration.Role.OWNER) {
                if (!mAllowOwnerRole) {
                    continue;
                }
            } else {
                if (!roles.contains(role)) {
                    continue;
                }
            }

            View radioView = getActivity().getLayoutInflater().inflate(R.layout.radio_item_roles, null);
            TextView rolesName = (TextView) radioView.findViewById(R.id.roles_name);
            rolesName.setText(CollaborationUtils.getRoleName(getActivity(), role));
            TextView rolesDescription = (TextView) radioView.findViewById(R.id.roles_description);
            rolesDescription.setText(CollaborationUtils.getRoleDescription(getActivity(), role));
            ConstraintLayout rolesTextLayout = (ConstraintLayout) radioView.findViewById(R.id.roles_text_layout);
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

    public static CollaborationRolesDialog newInstance(ArrayList<BoxCollaboration.Role> roles, BoxCollaboration.Role selectedRole, String name, boolean allowRemove, boolean allowOwnerRole, BoxCollaboration collaboration) {
        CollaborationRolesDialog dialog = new CollaborationRolesDialog();

        Bundle b = new Bundle();
        b.putSerializable(ARGS_ROLES, roles);
        b.putSerializable(ARGS_SELECTED_ROLE, selectedRole);
        b.putString(ARGS_NAME, name);
        b.putBoolean(ARGS_ALLOW_REMOVE, allowRemove);
        b.putBoolean(ARGS_ALLOW_OWNER_ROLE, allowOwnerRole);
        b.putSerializable(ARGS_SERIALIZABLE_EXTRA, collaboration);
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

    @Override
    public void onDestroyView()
    {
        Dialog dialog = getDialog();

        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        if ((dialog != null) && getRetainInstance())
            dialog.setDismissMessage(null);

        super.onDestroyView();
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

    public BoxCollaboration getCollaboration() {
        return mCollaboration;
    }

    public String getCollaboratorName() {return mCollaboratorName;}


    public interface OnRoleSelectedListener {
        public void onRoleSelected(CollaborationRolesDialog rolesDialog);
    }
}