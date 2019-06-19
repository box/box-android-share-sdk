package com.box.androidsdk.share.vm;

/**
 * A special extension of DataWrapper to hold data for inviting a new Collaborator
 */
public class InviteCollaboratorsDataWrapper extends DataWrapper<String> {

    private boolean mInvitationFailed;

    public InviteCollaboratorsDataWrapper(String data, int strCode, boolean invitationFailed) {
        this.mData = data;
        this.mStrCode = strCode;
        this.mInvitationFailed = invitationFailed;
    }

    /**
     * Returns true if all invitation succeeded.
     * @return true if all invitation succeeded
     */
    public boolean isSuccess() {
        return !isInvitationFailed();
    }

    /**
     * Returns true if at least one invitation failed.
     * @return true if at least one invitation failed
     */
    public boolean isInvitationFailed() {
        return mInvitationFailed;
    }

    /**
     * Returns true if the String formatted part exists.
     * @return true if the String formatted part exists
     */
    public boolean hasSubMessage() {
        return mData != null;
    }
}
