package com.box.androidsdk.share.usx.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.box.androidsdk.share.R;


public class PositiveNegativeDialogFragment extends DialogFragment {

    protected static final String ARGUMENT_TITLE_ID = "title_res_id";
    protected static final String ARGUMENT_MESSAGE_ID = "message_res_id";
    protected static final String ARGUMENT_POSITIVE_ID = "positive_res_id";
    protected static final String ARGUMENT_NEGATIVE_ID = "negative_res_id";

    protected OnPositiveOrNegativeButtonClickedListener mButtonClickedListener;
    protected boolean mButtonClicked;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        mButtonClicked = false;
        int title = getArguments().getInt(ARGUMENT_TITLE_ID);
        int msg = getArguments().getInt(ARGUMENT_MESSAGE_ID);
        int positive = getArguments().getInt(ARGUMENT_POSITIVE_ID);
        int negative = getArguments().getInt(ARGUMENT_NEGATIVE_ID);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ShareDialogThemeNew).setTitle(title).setMessage(msg)
                .setPositiveButton(getText(positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mButtonClicked = true;
                        if (mButtonClickedListener != null) {
                            mButtonClickedListener.onPositiveButtonClicked(PositiveNegativeDialogFragment.this);
                        }
            }
        } ).setNegativeButton(getText(negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mButtonClicked = true;
                        if (mButtonClickedListener != null){
                            mButtonClickedListener.onNegativeButtonClicked(PositiveNegativeDialogFragment.this);
                        }
                    }
                });
        return builder.create();
    }

    public void setOnPositiveOrNegativeButtonClickedListener(OnPositiveOrNegativeButtonClickedListener listener) {
        mButtonClickedListener = listener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mButtonClicked == false && mButtonClickedListener != null){
            mButtonClickedListener.onNegativeButtonClicked(PositiveNegativeDialogFragment.this);
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

    public interface OnPositiveOrNegativeButtonClickedListener {
        void onPositiveButtonClicked(PositiveNegativeDialogFragment fragment);
        void onNegativeButtonClicked(PositiveNegativeDialogFragment fragment);
    }

    /**
     * Create a fragment that shows a simple alert dialog with text given by provided string ids.
     * @param titleResId resource id for string for title.
     * @param messageResId resource id for string for message.
     * @param positiveButtonResId resource id for string for positive button.
     * @param negativeButtonResId resource id for string for negative button.
     * @return fragment displaying a simple dialog.
     */
    public static PositiveNegativeDialogFragment createFragment(final int titleResId,
                                                                final int messageResId,
                                                                int positiveButtonResId,
                                                                int negativeButtonResId,
                                                                OnPositiveOrNegativeButtonClickedListener listener){
        PositiveNegativeDialogFragment fragment = new PositiveNegativeDialogFragment();
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
