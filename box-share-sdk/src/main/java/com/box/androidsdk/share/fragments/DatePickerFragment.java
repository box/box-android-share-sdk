package com.box.androidsdk.share.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener, Dialog.OnDismissListener {

    DatePickerDialog mDialog;

    private static final String EXTRA_START_DATE = "extraStartDate";
    private final String EXTRA_KEY_YEAR = "extraYear";
    private final String EXTRA_KEY_MONTH = "extraMonth";
    private final String EXTRA_KEY_DAY = "extraDay";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        if (getArguments() != null){
            Date date = (Date)getArguments().getSerializable(EXTRA_START_DATE);
            if (date != null) {
                c.setTime(date);
            }
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (savedInstanceState != null){
            year = savedInstanceState.getInt(EXTRA_KEY_YEAR);
            month = savedInstanceState.getInt(EXTRA_KEY_MONTH);
            day = savedInstanceState.getInt(EXTRA_KEY_DAY);
        }
        mDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        // Create a new instance of DatePickerDialog and return it
        return mDialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_KEY_YEAR,  mDialog.getDatePicker().getYear());
        outState.putInt(EXTRA_KEY_MONTH,  mDialog.getDatePicker().getMonth());
        outState.putInt(EXTRA_KEY_DAY,  mDialog.getDatePicker().getDayOfMonth());
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Activity activity = getActivity();
        if (activity instanceof DatePickerDialog.OnDateSetListener){
            ((DatePickerDialog.OnDateSetListener) activity).onDateSet(view, year, month, day);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener){
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }


    public static final DatePickerFragment createFragment(final Date date){
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle b = new Bundle();
        b.putSerializable(EXTRA_START_DATE, date);
        fragment.setArguments(b);
        return fragment;
    }
}