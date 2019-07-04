package com.box.androidsdk.share.usx.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.box.androidsdk.share.R;


public class PasswordDialogFragment extends PositiveNegativeDialogFragment{

    private static final String EXTRA_PREV_TEXT = "extraPrevText";
    private EditText mPasswordEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String prevPassword = "";
        if (savedInstanceState != null){
            prevPassword = savedInstanceState.getString(EXTRA_PREV_TEXT);
        }
        int title = getArguments().getInt(ARGUMENT_TITLE_ID);
        int message = getArguments().getInt(ARGUMENT_MESSAGE_ID);
        int positive = getArguments().getInt(ARGUMENT_POSITIVE_ID);
        int negative = getArguments().getInt(ARGUMENT_NEGATIVE_ID);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ShareDialogTheme);
        LinearLayout passwordContainer = (LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.password_edit_text, null);
        mPasswordEditText = (EditText)passwordContainer.findViewById(R.id.box_password_edit_text);
        mPasswordEditText.setHint(message);
        mPasswordEditText.setText(prevPassword);
        builder.setPositiveButton(getText(positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mButtonClickedListener != null) {
                    mButtonClickedListener.onPositiveButtonClicked(PasswordDialogFragment.this);
                }
            }
        }).setNegativeButton(getText(negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mButtonClickedListener != null) {
                    mButtonClickedListener.onNegativeButtonClicked(PasswordDialogFragment.this);
                }
            }
        });
        builder.setTitle(title);
        // possibly construct from layout.

        builder.setView(passwordContainer);
        return builder.create();
    }

    public String getPassword(){
        if (mPasswordEditText == null){
            return null;
        }
        return mPasswordEditText.getText().toString();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_PREV_TEXT, mPasswordEditText.getText().toString());
        super.onSaveInstanceState(outState);
    }

    public static final PasswordDialogFragment createFragment(final int titleResId,
                                                              final int messageResId,
                                                              int positiveButtonResId,
                                                              int negativeButtonResId,
                                                              OnPositiveOrNegativeButtonClickedListener listener){
        PasswordDialogFragment fragment = new PasswordDialogFragment();
        Bundle b = new Bundle();
        b.putInt(ARGUMENT_TITLE_ID, titleResId);
        b.putInt(ARGUMENT_MESSAGE_ID, messageResId);
        b.putInt(ARGUMENT_POSITIVE_ID, positiveButtonResId);
        b.putInt(ARGUMENT_NEGATIVE_ID, negativeButtonResId);
        fragment.setArguments(b);
        fragment.setOnPositiveOrNegativeButtonClickedListener(listener);
        return fragment;
    }

}
