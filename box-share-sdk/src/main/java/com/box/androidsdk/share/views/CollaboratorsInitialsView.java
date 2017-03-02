package com.box.androidsdk.share.views;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.content.views.BoxAvatarView;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.fragments.CollaborationsFragment;
import com.box.androidsdk.share.fragments.InviteCollaboratorsFragment;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;

/**
 * This is a custom view designed primarily like a container of other views to show the names of collaborators
 * with proper styling.
 */
public class CollaboratorsInitialsView extends LinearLayout {

    private InviteCollaboratorsFragment.InviteCollaboratorsListener mInviteCollaboratorsListener;
    private TextView mInitialsListHeader;
    private LinearLayout mInitialsListView;
    private LinearLayout mInitialsListViewSection;
    protected BoxIteratorCollaborations mCollaborations;
    protected ShareController mController;
    protected BoxItem mShareItem;
    private BoxCollaborator mUnknownCollaborator;
    public static final String EXTRA_COLLABORATORS = "CollaboratorsInitialsView.ExtraCollaborators";
    public static final String EXTRA_SAVED_STATE = "CollaboratorsInitialsView.ExtraSaveState";

    private ProgressBar mProgressBar;
    private BoxResponse mBoxResponse;

    public CollaboratorsInitialsView(Context context) {
        this(context, null);
    }

    public CollaboratorsInitialsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollaboratorsInitialsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * This is mandatory to execute a request in order to obtain collaborators for a box folder
     *
     * @param folder Box folder
     * @param shareController Share controller used for making a request
     * @param listener InviteCollabs listener used to update the UI with the response
     */
    public void setArguments(BoxFolder folder, ShareController shareController, InviteCollaboratorsFragment.InviteCollaboratorsListener listener) {
        mShareItem = folder;
        mController = shareController;
        mInviteCollaboratorsListener = listener;
    }

    /**
     * Sets up the child views
     */
    private void init() {
        inflate(getContext(), R.layout.view_collaborators_initial, this);
        mProgressBar = (ProgressBar) findViewById(R.id.box_sharesdk_activity_progress_bar);
        mInitialsListView = (LinearLayout) findViewById(R.id.invite_collaborator_initials_list);
        mInitialsListHeader = (TextView) findViewById(R.id.invite_collaborator_initials_list_header);
        mInitialsListViewSection = (LinearLayout) findViewById(R.id.collaborator_initials_list_section);
        mInitialsListViewSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInviteCollaboratorsListener != null) {
                    mInviteCollaboratorsListener.onShowCollaborators(mCollaborations);
                }
            }
        });

        JsonObject jsonObject = new JsonObject();
        jsonObject.add(BoxCollaborator.FIELD_NAME, "");
        mUnknownCollaborator = new BoxUser(jsonObject);
    }

    protected BoxFolder getFolder() {
        return (BoxFolder)mShareItem;
    }

    public final String getString(@StringRes int resId) {
        return getResources().getString(resId);
    }

    /**
     * Executes the request to retrieve collaborations for the folder
     */
    public void fetchCollaborations() {
        if (getFolder() == null || SdkUtils.isBlank(getFolder().getId())) {
            mController.showToast(getContext(), getString(R.string.box_sharesdk_cannot_view_collaborations));
            return;
        }

        // Show spinner
        mProgressBar.setVisibility(VISIBLE);

        if (mBoxResponse == null) {
            // Execute request to fetch collaborators
            mController.fetchCollaborations(getFolder()).addOnCompletedListener(mCollaborationsListener);
        } else {
            // Dismiss spinner
            mProgressBar.setVisibility(GONE);
            updateUi((BoxIteratorCollaborations)mBoxResponse.getResult());
        }
    }

    private BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations> mCollaborationsListener =
            new BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations>() {
                @Override
                public void onCompleted(final BoxResponse<BoxIteratorCollaborations> response) {
                    mBoxResponse = response;
                    final Activity activity = (Activity)getContext();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Dismiss spinner
                            mProgressBar.setVisibility(GONE);
                            if (response.isSuccess() && getFolder() != null) {
                                updateUi(response.getResult());
                            } else {
                                BoxLogUtils.e(CollaborationsFragment.class.getName(), "Fetch Collaborators request failed",
                                        response.getException());
                                mController.showToast(activity, getString(R.string.box_sharesdk_network_error));
                            }
                        }
                    });
                }
            };


    public void updateUi(BoxIteratorCollaborations boxIteratorCollaborations) {
        mCollaborations = boxIteratorCollaborations;
        if (mCollaborations != null && mCollaborations.size() != 0) {

            mInitialsListHeader.setVisibility(View.VISIBLE);
            final int totalCollaborators = mCollaborations.fullSize().intValue();
            final int remainingWidth = mInitialsListView.getWidth();
            final ArrayList<BoxCollaboration> collaborations = mCollaborations.getEntries();

            clearInitialsView();
            mInitialsListView.post(new Runnable() {
                @Override
                public void run() {
                    //Add the first item to calculate the width
                    final View initialsView = addInitialsToList(collaborations.get(0).getAccessibleBy());
                    initialsView.post(new Runnable() {
                        @Override
                        public void run() {
                            int viewWidth = initialsView.getWidth();
                            int viewsCount = remainingWidth/viewWidth;
                            for (int i = 1; i < viewsCount && i < collaborations.size(); i++) {
                                View viewAdded = addInitialsToList(collaborations.get(i).getAccessibleBy());
                                if (i == viewsCount - 1) {
                                    // This is the last one, display count if needed
                                    int remaining = totalCollaborators - viewsCount;
                                    if (remaining > 0) {
                                        BoxAvatarView initials = (BoxAvatarView) viewAdded.findViewById(R.id.collaborator_initials);
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.set(BoxCollaborator.FIELD_NAME, Integer.toString(remaining + 1));
                                        BoxUser numberUser = new BoxUser(jsonObject);
                                        initials.loadUser(numberUser, mController.getAvatarController());
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    private void clearInitialsView() {
        mInitialsListView.removeAllViewsInLayout();
    }

    private View addInitialsToList(BoxCollaborator collaborator) {
        View layoutContainer =  LayoutInflater.from((Activity)getContext()).inflate(R.layout.view_initials, null);
        BoxAvatarView initialsView = (BoxAvatarView) layoutContainer.findViewById(R.id.collaborator_initials);

        if (collaborator == null) {
            initialsView.loadUser(mUnknownCollaborator, mController.getAvatarController());
        } else {
            initialsView.loadUser(collaborator, mController.getAvatarController());
        }
        mInitialsListView.addView(layoutContainer);
        return layoutContainer;
    }

    @Override
    public Parcelable onSaveInstanceState()
    {
        Parcelable savedState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_COLLABORATORS, mBoxResponse);
        bundle.putParcelable(EXTRA_SAVED_STATE, savedState);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            mBoxResponse = (BoxResponse) bundle.getSerializable(EXTRA_COLLABORATORS);
            Parcelable savedState =  bundle.getParcelable(EXTRA_SAVED_STATE);
            super.onRestoreInstanceState(savedState);
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.fetchCollaborations();
    }
}
