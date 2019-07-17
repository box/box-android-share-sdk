package com.box.androidsdk.share.vm;

import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

import com.box.androidsdk.content.BoxException;

/**
 * A data wrapper that can also take an accompanying string resource to display message to users.
 * @param <T> the data type of the item in the response that will be returned
 */
public class PresenterData<T> {


    public static final int NO_MESSAGE = -1;
    private int mStrRes = NO_MESSAGE;
    private T mData;
    Exception mException;

    public PresenterData() {

    }

    public PresenterData(T data, @StringRes @PluralsRes int strRes) {
        this.mData = data;
        this.mStrRes = strRes;
    }

    public PresenterData(T data, @StringRes @PluralsRes int strRes, BoxException exception) {
        this.mData = data;
        this.mStrRes = strRes;
        this.mException = exception;
    }

    /**
     * Updates the item with the data passed in. Use this if the request was successful.
     * @param data the data from the result
     */
    public void success(T data) {
        this.mData = data;
        this.mStrRes = NO_MESSAGE;
    }

    /**
     * Updates the item with the data passed in. Use this if the request was successful.
     * @param data the data from the result
     * @param strRes the String resource for the success message
     */
    public void success(T data, @StringRes @PluralsRes int strRes) {
        this.mData = data;
        this.mStrRes = strRes;
        this.mException = null;
    }

    /**
     * Updates the item with the stringCode passed in. Use this if request was unsuccessful
     * @param strRes the String resource for the error message
     */
    public void failure(@StringRes @PluralsRes int strRes, Exception exception) {
        this.mData = null;
        this.mStrRes = strRes;
        this.mException = exception;
    }

    /**
     * Returns true if the request was successful.
     * @return true if the request was successful
     */
    public boolean isSuccess() {
        return mException == null;
    }

    /**
     * Returns the data.
     * @return the data
     */
    public T getData() {
        return mData;
    }

    /**
     * Returns the string resource code.
     * @return the string resource code
     */
    public int getStrCode() {
        return mStrRes;
    }

    public Exception getException() {
        return mException;
    }

    public void setException(Exception exception) {
        this.mException = exception;
    }
}
