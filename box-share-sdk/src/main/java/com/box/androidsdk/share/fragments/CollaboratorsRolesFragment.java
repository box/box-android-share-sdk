package com.box.androidsdk.share.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;

import java.util.ArrayList;
import java.util.List;


public class CollaboratorsRolesFragment extends BoxFragment implements View.OnClickListener {

    public static final String ARGS_ROLES = "argsRoles";
    public static final String ARGS_SELECTED_ROLE = "argsSelectedRole";
    public static final String ARGS_NAME = "argsName";
    public static final String ARGS_ALLOW_REMOVE = "argsAllowRemove";
    public static final String ARGS_ALLOW_OWNER_ROLE = "argsAllowOwnerRole";
    public static final String ARGS_SERIALIZABLE_EXTRA = "argsTargetId";

    private List<BoxCollaboration.Role> mRoles;
    private boolean mAllowOwnerRole;
    private BoxCollaboration.Role mSelectedRole;
    private boolean mAllowRemove;
    private BoxCollaboration mCollaboration;
    protected ArrayList<RadioButton> mRolesOptions = new ArrayList<RadioButton>();

    private RadioGroup mRadioGroup;
    private Button mRemoveButton;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collaboration_roles, container, false);

        mRoles = (ArrayList<BoxCollaboration.Role>) getArguments().getSerializable(ARGS_ROLES);
        mSelectedRole = (BoxCollaboration.Role) getArguments().getSerializable(ARGS_SELECTED_ROLE);
        mAllowRemove = getArguments().getBoolean(ARGS_ALLOW_REMOVE);
        mAllowOwnerRole = getArguments().getBoolean(ARGS_ALLOW_OWNER_ROLE);
        mCollaboration = (BoxCollaboration)getArguments().getSerializable(ARGS_SERIALIZABLE_EXTRA);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.collaborator_roles_group);
        mRemoveButton = (Button) view.findViewById(R.id.remove_btn);
        addRolesToView(mRoles);
        return view;
    }

    private void addRolesToView(List<BoxCollaboration.Role> roles) {
        LinearLayout rolesLayout = new LinearLayout(getActivity());
        rolesLayout.setOrientation(LinearLayout.VERTICAL);
        rolesLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mRadioGroup.addView(rolesLayout);

        View lastElementDivider = null;
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
            //rolesRadio.setOnClickListener(this);
            mRolesOptions.add(rolesRadio);
            rolesLayout.addView(radioView);
            lastElementDivider = radioView.findViewById(R.id.divider);
        }
        if (lastElementDivider != null) {
            lastElementDivider.setVisibility(View.INVISIBLE);
        }

        if (!mAllowRemove) {
            mRemoveButton.setVisibility(View.GONE);
        }
    }
    //
    public static CollaboratorsRolesFragment newInstance(BoxCollaborationItem item, ArrayList<BoxCollaboration.Role> roles, BoxCollaboration.Role selectedRole, String name, boolean allowRemove, boolean allowOwnerRole, BoxCollaboration collaboration) {
        CollaboratorsRolesFragment fragment = new CollaboratorsRolesFragment();

        Bundle b = getBundle(item);
        b.putSerializable(ARGS_ROLES, roles);
        b.putSerializable(ARGS_SELECTED_ROLE, selectedRole);
        b.putString(ARGS_NAME, name);
        b.putBoolean(ARGS_ALLOW_REMOVE, allowRemove);
        b.putBoolean(ARGS_ALLOW_OWNER_ROLE, allowOwnerRole);
        b.putSerializable(ARGS_SERIALIZABLE_EXTRA, collaboration);
        fragment.setArguments(b);

        return fragment;
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
    }


}
