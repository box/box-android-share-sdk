package com.box.androidsdk.share.vm;

import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

/**
 * A special extension of PresenterData to hold data for inviting a new Collaborator
 */
public class InviteCollaboratorsPresenterData extends PresenterData<String> {

    private boolean mInvitationFailed;
    private int mAlreadyAddedCount;
    private boolean mShowSnackbar;



    public InviteCollaboratorsPresenterData() {

    }

    /**
     * A constructor created to help with a successful request
     * @param data the data to display
     * @param strCode the string resource code for the message
     */
    public InviteCollaboratorsPresenterData(String data, @StringRes @PluralsRes int strCode) {
        super(data, strCode);
        this.mInvitationFailed = false;
        this.mAlreadyAddedCount = 0;
        this.mShowSnackbar = false;
    }

    public InviteCollaboratorsPresenterData(String data, int strCode, boolean invitationFailed, int alreadyAddedCount, boolean showSnackbar) {
        super(data, strCode);
        this.mInvitationFailed = invitationFailed;
        this.mAlreadyAddedCount = alreadyAddedCount;
        this.mShowSnackbar = showSnackbar;
    }

    public int getAlreadyAdddedCount() {
        return mAlreadyAddedCount;
    }

    public boolean isSnackBarMessage() {
        return mShowSnackbar;
    }

    /**
     * Returns true if all invitations succeeded.
     * @return true if all invitations succeeded
     */
    @Override
    public boolean isSuccess() {
        return !mInvitationFailed;
    }

    /**
     * Returns true if there is data that can be used to display a message.
     * @return true if there is data that can be used to display a message
     */
    public boolean isNonNullData() {
        return getData() != null;
    }


}
