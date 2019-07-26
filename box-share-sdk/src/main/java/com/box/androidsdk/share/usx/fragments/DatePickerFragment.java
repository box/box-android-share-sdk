package com.box.androidsdk.share.usx.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.box.androidsdk.share.R;

import java.util.Calendar;
import java.util.Date;


public class DatePickerFragment extends PositiveNegativeDialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private DatePickerDialog mDialog;
    private DatePickerDialog.OnDateSetListener mOnDateSetListener;

    private static final String EXTRA_START_DATE = "extraStartDate";
    private final String EXTRA_KEY_YEAR = "extraYear";
    private final String EXTRA_KEY_MONTH = "extraMonth";
    private final String EXTRA_KEY_DAY = "extraDay";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setRetainInstance(true);

        mButtonClicked = false;

        final Calendar minDateCalendar = Calendar.getInstance();
        minDateCalendar.add(Calendar.DATE, 1);
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();

        if (getArguments() != null){
            Date date = (Date)getArguments().getSerializable(EXTRA_START_DATE);
            if (date != null) {
                c.setTime(date);
            }
        }
        // we cannot have a minimum date that is earlier than the set date.
        if (c.getTimeInMillis() < minDateCalendar.getTimeInMillis()){
            c.setTime(minDateCalendar.getTime());
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (savedInstanceState != null){
            year = savedInstanceState.getInt(EXTRA_KEY_YEAR);
            month = savedInstanceState.getInt(EXTRA_KEY_MONTH);
            day = savedInstanceState.getInt(EXTRA_KEY_DAY);
        }
        mDialog = new DatePickerDialog(getActivity(), R.style.ShareDialogThemeUSX, this, year, month, day);
        mDialog.getDatePicker().setMinDate(minDateCalendar.getTimeInMillis());
        // Create a new instance of DatePickerDialog and return it
        return mDialog;
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        mOnDateSetListener = listener;
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
        mButtonClicked = true;
        if (mOnDateSetListener != null){
            mOnDateSetListener.onDateSet(view, year, month, day);
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

    public static final DatePickerFragment createFragment(final Date date, DatePickerDialog.OnDateSetListener dateSetListener, OnPositiveOrNegativeButtonClickedListener listener){
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle b = new Bundle();
        b.putSerializable(EXTRA_START_DATE, date);
        fragment.setArguments(b);
        fragment.setOnDateSetListener(dateSetListener);
        fragment.setOnPositiveOrNegativeButtonClickedListener(listener);
        return fragment;
    }
}