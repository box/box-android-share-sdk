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
     * Check if any invitation failed
     * @return true if at least one invitation to a collaborator failed
     */
    public boolean isInvitationFailed() {
        return mInvitationFailed;
    }

    /**
     * Check if the String formatted part exists
     * @return true if String formatted part exists
     */
    public boolean hasSubMessage() {
        return mData != null;
    }
}
