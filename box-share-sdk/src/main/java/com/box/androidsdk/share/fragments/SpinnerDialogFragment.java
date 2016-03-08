package com.box.androidsdk.share.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SpinnerDialogFragment extends DialogFragment {

    private static String EXTRA_STRING_RES = "SpinnerDialogFragment.ExtraStringRes";
    private static String EXTRA_STRING_TITLE_RES = "SpinnerDialogFragment.ExtraStringTitleRes";

    protected ProgressDialog mDialog;
    protected int stringTitleRes;
    protected int stringRes;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setRetainInstance(true);

        if (getArguments() != null) {
            stringRes = getArguments().getInt(EXTRA_STRING_RES);
            stringTitleRes = getArguments().getInt(EXTRA_STRING_TITLE_RES);
        }

        mDialog = new ProgressDialog(getActivity());
        mDialog.setTitle(getString(stringTitleRes));
        mDialog.setMessage(getString(stringRes));
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        return mDialog;
    }


    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        if ((dialog != null) && getRetainInstance())
            dialog.setDismissMessage(null);

        super.onDestroyView();
    }

    public static final SpinnerDialogFragment createFragment(final int stringTitleRes, final int stringRes) {
        SpinnerDialogFragment fragment = new SpinnerDialogFragment();
        Bundle b = new Bundle();
        b.putInt(EXTRA_STRING_TITLE_RES, stringTitleRes);
        b.putInt(EXTRA_STRING_RES, stringRes);
        fragment.setArguments(b);
        return fragment;
    }
}
