package com.box.androidsdk.share.utils;

import androidx.databinding.BindingAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.usx.adapters.InviteeAdapter;
import com.box.androidsdk.share.usx.views.ChipCollaborationView;
import com.tokenautocomplete.TokenCompleteTextView;

public class InviteCollaboratorsBindingAdapters {

    @BindingAdapter(value = {"personalMessageTextView", "addPersonalMessageButton", "bottomDivider"})
    public static void onEmptyAndUnfocused(EditText view, View v1, View v2, View v3) {
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && ((EditText)v).getText().toString().isEmpty()) {
                v1.setVisibility(View.GONE);
                v2.setVisibility(View.VISIBLE);
                v3.setVisibility(View.GONE);
                view.setVisibility(View.GONE);
            }
        });
    }
    @BindingAdapter(value = {"personalMessageEditText", "personalMessageTextView", "bottomDivider"})
    public static void onAddPersonalMessageBottom(Button view, View v1, View v2, View v3) {
        view.setOnClickListener(v -> {
            v1.setVisibility(View.VISIBLE);
            v2.setVisibility(View.VISIBLE);
            v3.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
            v1.requestFocus();
        });
    }

    @BindingAdapter(value = {"roleName"})
    public static void setRoleName(TextView roleName, BoxCollaboration.Role role) {
        if (role != null) roleName.setText(CollaborationUtils.getRoleName(roleName.getContext(), role));
    }

    @BindingAdapter(value = {"roleDescription"})
    public static void setRoleDescription(TextView roleDescription, BoxCollaboration.Role role) {
        if (role != null) roleDescription.setText(CollaborationUtils.getRoleDescription(roleDescription.getContext(), role));
    }

    @BindingAdapter(value = {"setInviteeInitial"})
    public static void setInitialsThumnb(TextView textView, String name) {
        SdkUtils.setInitialsThumb(textView.getContext(), textView, name);
    }

    @BindingAdapter(value = {"adapter", "tokenizer", "tokenListener"})
    public static void setAdaptersAndListeners(ChipCollaborationView chipCollaborationView, InviteeAdapter adapter, MultiAutoCompleteTextView.CommaTokenizer tokenizer, TokenCompleteTextView.TokenListener tokenListener) {
        chipCollaborationView.setAdapter(adapter);
        chipCollaborationView.setTokenizer(tokenizer);
        chipCollaborationView.setTokenListener(tokenListener);
    }

}
