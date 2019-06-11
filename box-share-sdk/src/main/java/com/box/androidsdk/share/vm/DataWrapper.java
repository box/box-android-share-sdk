package com.box.androidsdk.share.vm;

public class DataWrapper<T> {
    private T data;
    private int stringErrorCode;
    public static final int SUCCEESS = -1;


    public void success(T data) {
        this.data = data;
        this.stringErrorCode = SUCCEESS;
    }
    public void failure(int stringErrorCode) {
        this.data = null;
        this.stringErrorCode = stringErrorCode;
    }
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getStringErrorCode() {
        return stringErrorCode;
    }

    public void setStringErrorCode(int stringErrorCode) {
        this.stringErrorCode = stringErrorCode;
    }
}
