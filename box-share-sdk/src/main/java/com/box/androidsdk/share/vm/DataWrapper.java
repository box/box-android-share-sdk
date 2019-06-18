package com.box.androidsdk.share.vm;

/**
 * A data wrapper to make sure LiveData changes can take network responses into account
 * @param <T> the data type of the item in the response that will be returned
 */
public class DataWrapper<T> {
    protected T mData;
    protected int mStrCode;
    public static final int SUCCEESS = -1;

    /**
     * Updates the item with the data passed in. Use this if the request was successful.
     * @param data the data from the result
     */
    public void success(T data) {
        this.mData = data;
        this.mStrCode = SUCCEESS;
    }

    /**
     * Updates the item with the stringCode passed in. Use this if request was unsuccessful
     * @param strCode The String resource code for the error message
     */
    public void failure(int strCode) {
        this.mData = null;
        this.mStrCode = strCode;
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
        return mStrCode;
    }
}
