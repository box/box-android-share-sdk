package com.box.androidsdk.share.vm;

/**
 * A special class to hold data for inviting a new Collaborator
 */
public class InviteCollaboratorsDataWrapper {
    private boolean mInvitationFailed;
    private String subMessage; //String used to hold the formatting part
    private int strCode;


    public InviteCollaboratorsDataWrapper() {

    }
    public InviteCollaboratorsDataWrapper(boolean mInvitationFailed, String subMessage, int strCode) {
        this.mInvitationFailed = mInvitationFailed;
        this.subMessage = subMessage;
        this.strCode = strCode;
    }

    public boolean ismInvitationFailed() {
        return mInvitationFailed;
    }

    public void setmInvitationFailed(boolean mInvitationFailed) {
        this.mInvitationFailed = mInvitationFailed;
    }

    public String getSubMessage() {
        return subMessage;
    }

    public void setSubMessage(String subMessage) {
        this.subMessage = subMessage;
    }

    public int getStrCode() {
        return strCode;
    }

    public void setStrCode(int strCode) {
        this.strCode = strCode;
    }

    public void setValues(InviteCollaboratorsDataWrapper other) {
        this.mInvitationFailed = other.mInvitationFailed;
        this.subMessage = other.subMessage;
        this.strCode = other.strCode;
    }

    public boolean hasSubMessage() {
        return subMessage != null;
    }
}
