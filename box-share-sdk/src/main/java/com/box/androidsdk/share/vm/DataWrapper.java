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
     * Use this if the request was successful
     * @param data the data from the result
     */
    public void success(T data) {
        this.mData = data;
        this.mStrCode = SUCCEESS;
    }

    /**
     * Use this if request was unsuccessful
     * @param stringErrorCode The String resource code for the error message
     */
    public void failure(int stringErrorCode) {
        this.mData = null;
        this.mStrCode = stringErrorCode;
    }

    /**
     * A method for checking whether the request was successful or not
     * @return true if the request was successful
     */
    public boolean isSuccess() {
        return mData != null;
    }

    public T getData() {
        return mData;
    }

    public int getStrCode() {
        return mStrCode;
    }
}
