package com.box.androidsdk.share.vm;

/**
 * A special extension of PresenterData to hold data for inviting a new Collaborator
 */
public class InviteCollaboratorsPresenterData extends PresenterData<String> {

    private boolean mInvitationFailed;

    public InviteCollaboratorsPresenterData(String data, int strCode, boolean invitationFailed) {
        super(data, strCode);
        this.mInvitationFailed = invitationFailed;
    }

    /**
     * Returns true if all invitations succeeded.
     * @return true if all invitations succeeded
     */
    @Override
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
        return getData() != null;
    }
}
