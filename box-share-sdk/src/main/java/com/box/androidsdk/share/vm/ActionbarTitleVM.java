package com.box.androidsdk.share.vm;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ActionbarTitleVM extends ViewModel {
    MutableLiveData<String> mTitle = new MutableLiveData<>();
    MutableLiveData<String> mSubtitle = new MutableLiveData<>();

    public MutableLiveData<String> getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle.postValue(title);
    }

    public MutableLiveData<String> getSubtitle() {
        return mSubtitle;
    }

    public void setSubtitle(String subtitle) {
        this.mSubtitle.postValue(subtitle);
    }
}
