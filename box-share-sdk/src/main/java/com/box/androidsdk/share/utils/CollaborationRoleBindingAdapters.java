package com.box.androidsdk.share.utils;

import android.app.Activity;
import android.content.Context;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxRadioItemRolesBinding;
import com.box.androidsdk.share.usx.fragments.CollaboratorsRolesFragment;

import java.util.HashSet;
import java.util.List;

public class CollaborationRoleBindingAdapters {

    @BindingAdapter(value = {"roles", "allowOwnerRole", "allowRemove", "selectedRole", "removeButton", "notifier"})
    public static void populateRadioGroup(RadioGroup radioGroup, List roles,
                                          boolean allowOwnerRole, boolean allowRemove,
                                          LiveData<BoxCollaboration.Role> selectedRole, Button removeButton, CollaboratorsRolesFragment.RoleUpdateNotifier notifier) {

        Context context = radioGroup.getContext();
        LinearLayout rolesLayout = new LinearLayout(context);
        rolesLayout.setOrientation(LinearLayout.VERTICAL);
        rolesLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        radioGroup.addView(rolesLayout);


        HashSet<RadioButton> roleOptions = new HashSet<>();
        View.OnClickListener listener = v ->  {
            BoxCollaboration.Role clickedRole = (BoxCollaboration.Role) v.getTag();
            notifier.setRole(clickedRole);

            ((MutableLiveData<BoxCollaboration.Role>)selectedRole).postValue(clickedRole);
            for (RadioButton radio : roleOptions) {
                BoxCollaboration.Role role = (BoxCollaboration.Role) radio.getTag();
                boolean shouldCheck = clickedRole == role;
                radio.setChecked(shouldCheck);
            }
        };
        UsxRadioItemRolesBinding binding = null;
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
            View radioView = ((Activity)context).getLayoutInflater().inflate(R.layout.usx_radio_item_roles, null);
            binding = DataBindingUtil.bind(radioView);
            binding.setRoleName(CollaborationUtils.getRoleName(context, role));
            binding.setRoleDescription(CollaborationUtils.getRoleDescription(context, role));
            binding.setRoleTag(role);
            binding.setListener(listener);
            binding.setRoleOptions(roleOptions);
            binding.setCheckRole(role == selectedRole.getValue());

            rolesLayout.addView(radioView);
            binding.setIsLastDivider(false);
        }
        if (binding != null) {
            binding.setIsLastDivider(true);
        }

        if (allowRemove) {
            removeButton.setVisibility(View.VISIBLE);
            removeButton.setOnClickListener(v -> notifier.notifyRemove());
        }
    }

    @BindingAdapter(value = {"roleOptions"})
    public static void addRoleOption(RadioButton button, HashSet roleOptions) {
        roleOptions.add(button);
    }

    @BindingAdapter(value = {"noSharePermissionRole", "itemType"})
    public static void setNoPermissionTextForShareLink(TextView textview, BoxCollaboration.Role role, String type) {
        Context context = textview.getContext();
        String translatedType = context.getResources().getString(translatedType(type));
        String translatedRole = role != null ? CollaborationUtils.getRoleName(context, role): "";
        String message = context.getResources().getString(R.string.box_share_sdk_no_permission_share_link, translatedRole, translatedType);
        textview.setText(message);
    }

    @BindingAdapter(value = {"noInvitePermissionRole", "itemType"})
    public static void setNoInviteTextForShareLink(TextView textview, BoxCollaboration.Role role, String type) {
        Context context = textview.getContext();
        String translatedType = context.getResources().getString(translatedType(type));
        String translatedRole = role != null ? CollaborationUtils.getRoleName(context, role): "";
        String message = context.getResources().getString(R.string.box_share_sdk_no_permission_invite_people, translatedRole, translatedType);
        textview.setText(message);
    }

    private static int translatedType(String type) {
        if (type.equals(BoxFolder.TYPE)) {
            return R.string.box_sharesdk_item_type_folder;
        } else if (type.equals(BoxFile.TYPE)) {
            return R.string.box_sharesdk_item_type_file;
        } else {
            return R.string.box_sharesdk_item_type_default;
        }
    }

}
