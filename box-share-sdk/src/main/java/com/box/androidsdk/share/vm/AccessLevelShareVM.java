package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.box.androidsdk.content.models.BoxSharedLink;

import java.util.HashSet;

public class AccessLevelShareVM extends ViewModel {

    MutableLiveData<BoxSharedLink.Access> mSelectedAccess = new MutableLiveData<>();
    HashSet<BoxSharedLink.Access> mActiveRadioButtons = new HashSet<>();

    public void setSelectedAccess(BoxSharedLink.Access access) {
        if (access != null) mSelectedAccess.postValue(access);
    }

    public LiveData<BoxSharedLink.Access> getSelectedAccess() {
        return mSelectedAccess;
    }

    public void setActiveRadioButtons(HashSet<BoxSharedLink.Access> activeRadioButtons) {
        this.mActiveRadioButtons = activeRadioButtons;
    }

    public HashSet<BoxSharedLink.Access> getActiveRadioButtons() {
        return mActiveRadioButtons;
    }
}
