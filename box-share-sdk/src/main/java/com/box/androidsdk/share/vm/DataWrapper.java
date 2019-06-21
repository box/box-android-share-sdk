package com.box.androidsdk.share.vm;

import androidx.annotation.StringRes;

/**
 * A data wrapper to make sure LiveData changes can take network responses into account
 * @param <T> the data type of the item in the response that will be returned
 */
public class DataWrapper<T> {
    protected T mData;
    protected int mStrRes;
    public static final int SUCCESS = -1;

    /**
     * Updates the item with the data passed in. Use this if the request was successful.
     * @param data the data from the result
     */
    public void success(T data) {
        this.mData = data;
        this.mStrRes = SUCCESS;
    }

    /**
     * Updates the item with the stringCode passed in. Use this if request was unsuccessful
     * @param strRes The String resource code for the error message
     */
    public void failure(@StringRes int strRes) {
        this.mData = null;
        this.mStrRes = strRes;
    }

    /**
     * Returns true if the request was successful.
     * @return true if the request was successful
     */
    public boolean isSuccess() {
        return mData != null;
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
}
