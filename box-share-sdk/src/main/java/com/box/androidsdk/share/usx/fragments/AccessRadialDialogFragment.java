package com.box.androidsdk.share.usx.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.R;

import java.util.ArrayList;
import java.util.HashSet;


public class AccessRadialDialogFragment extends PositiveNegativeDialogFragment{

    private static final String EXTRA_BOX_ITEM = "extraBoxItem";
    private static final String EXTRA_CHOSEN_ACCESS = "extraChosenAccess";

    private RadioButton mPublicRadio;
    private RadioButton mCompanyRadio;
    private RadioButton mCollaboratorRadio;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        RadioGroup accessRadioGroup = (RadioGroup)getActivity().getLayoutInflater().inflate(R.layout.usx_access_radio_group, null);
        BoxItem boxItem = (BoxItem)getArguments().getSerializable(EXTRA_BOX_ITEM);
        if (boxItem == null){
            throw new RuntimeException("No box item provided");
        }
        BoxSharedLink.Access restoredAccess = null;
        if (savedInstanceState != null){
            restoredAccess = (BoxSharedLink.Access)savedInstanceState.getSerializable(EXTRA_CHOSEN_ACCESS);
        }

        mPublicRadio = (RadioButton)accessRadioGroup.findViewById(R.id.public_access_radio);
        mCompanyRadio = (RadioButton)accessRadioGroup.findViewById(R.id.company_access_radio);
        mCollaboratorRadio = (RadioButton)accessRadioGroup.findViewById(R.id.collaborator_access_radio);
        ArrayList<RadioButton> allRadioButtons = new ArrayList<RadioButton>(3);
        allRadioButtons.add(mPublicRadio);
        allRadioButtons.add(mCompanyRadio);
        allRadioButtons.add(mCollaboratorRadio);
        HashSet<RadioButton> activeRadioButtons = new HashSet<RadioButton>(3);
        for (BoxSharedLink.Access access : boxItem.getAllowedSharedLinkAccessLevels()){
            switch (access){
                case OPEN:
                    activeRadioButtons.add(mPublicRadio);
                    break;
                case COMPANY:
                    activeRadioButtons.add(mCompanyRadio);
                    break;
                case COLLABORATORS:
                    activeRadioButtons.add(mCollaboratorRadio);
                    break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ShareDialogTheme);
        builder.setView(accessRadioGroup);

        builder.setPositiveButton(getResources().getString(R.string.box_sharesdk_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mButtonClickedListener != null) {
                    mButtonClickedListener.onPositiveButtonClicked(AccessRadialDialogFragment.this);
                }
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.box_sharesdk_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mButtonClickedListener != null) {
                    mButtonClickedListener.onNegativeButtonClicked(AccessRadialDialogFragment.this);
                }
            }
        });
        builder.setTitle(R.string.box_sharesdk_access);
        // possibly construct from layout.


        BoxSharedLink.Access access = restoredAccess != null ? restoredAccess : boxItem.getSharedLink().getEffectiveAccess();
        if (access != null) {
            switch (access) {
                case OPEN:
                    mPublicRadio.setChecked(true);
                    break;
                case COLLABORATORS:
                    mCollaboratorRadio.setChecked(true);
                    break;
                case COMPANY:
                    mCompanyRadio.setChecked(true);
            }
        }

        for (RadioButton button :allRadioButtons){
            if (!activeRadioButtons.contains(button)){
                if (button.isChecked()){
                    // should not be possible without server sending improper data.
                    button.setEnabled(false);
                } else {
                    button.setVisibility(View.GONE);
                }
            }
        }
        return builder.create();
    }

    public BoxSharedLink.Access getAccess(){
        if (mPublicRadio.isChecked()){
            return BoxSharedLink.Access.OPEN;
        } else if (mCompanyRadio.isChecked()){
            return BoxSharedLink.Access.COMPANY;
        } else if (mCollaboratorRadio.isChecked()){
            return BoxSharedLink.Access.COLLABORATORS;
        }
        return null;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_CHOSEN_ACCESS, getAccess());
        super.onSaveInstanceState(outState);
    }

    public static final AccessRadialDialogFragment createFragment(final BoxItem item, OnPositiveOrNegativeButtonClickedListener listener){
        AccessRadialDialogFragment fragment = new AccessRadialDialogFragment();
        Bundle b = new Bundle();
        b.putSerializable(EXTRA_BOX_ITEM, item);
        fragment.setArguments(b);
        fragment.setOnPositiveOrNegativeButtonClickedListener(listener);
        return fragment;
    }

}
