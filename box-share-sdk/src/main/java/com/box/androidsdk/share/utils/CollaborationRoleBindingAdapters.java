package com.box.androidsdk.share.utils;

import android.app.Activity;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.RadioItemRolesBinding;
import com.box.androidsdk.share.fragments.CollaboratorsRolesFragment;

import java.util.HashSet;
import java.util.List;

public class CollaborationRoleBindingAdapters {

    @BindingAdapter(value = {"roles", "allowOwnerRole", "allowRemove", "selectedRole", "removeButton", "roleUpdateNotifier"})
    public static void populateRadioGroup(RadioGroup radioGroup, List roles,
                                      boolean allowOwnerRole, boolean allowRemove,
                                      BoxCollaboration.Role selectedRole, Button removeButton, CollaboratorsRolesFragment.RoleUpdateNotifier notifier) {

        Context context = radioGroup.getContext();
        LinearLayout rolesLayout = new LinearLayout(context);
        rolesLayout.setOrientation(LinearLayout.VERTICAL);
        rolesLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        radioGroup.addView(rolesLayout);


        HashSet<RadioButton> roleOptions = new HashSet<>();
        View.OnClickListener listener = v ->  {
            BoxCollaboration.Role clickedRole = (BoxCollaboration.Role) v.getTag();
            for (RadioButton radio : roleOptions) {
                BoxCollaboration.Role role = (BoxCollaboration.Role) radio.getTag();
                boolean shouldCheck = clickedRole == role;
                radio.setChecked(shouldCheck);
                notifier.setRole(clickedRole);
            }
        };
        RadioItemRolesBinding binding = null;
        for (BoxCollaboration.Role role : BoxCollaboration.Role.values()) {

            if (role == BoxCollaboration.Role.OWNER) {
                if (!allowOwnerRole) {
                    continue;
                }
            } else {
                if (!roles.contains(role)) {
                    continue;
                }
            }
            View radioView = ((Activity)context).getLayoutInflater().inflate(R.layout.radio_item_roles, null);
            binding = DataBindingUtil.bind(radioView);
            binding.setRoleName(CollaborationUtils.getRoleName(context, role));
            binding.setRoleDescription(CollaborationUtils.getRoleDescription(context, role));
            binding.setRoleTag(role);
            binding.setListener(listener);
            binding.setRoleOptions(roleOptions);
            binding.setCheckRole(role == selectedRole);

            rolesLayout.addView(radioView);
            binding.setIsLastDivider(false);
        }
        if (binding != null) {
            binding.setIsLastDivider(true);
        }


        if (!allowRemove) {
            removeButton.setVisibility(View.GONE);
        }
    }
    @BindingAdapter(value = {"roleOptions"})
    public static void addRoleOption(RadioButton button, HashSet roleOptions) {
        roleOptions.add(button);
    }


}
