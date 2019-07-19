package com.box.androidsdk.share.usx.views;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxCollaborator;
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
import com.eclipsesource.json.JsonObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * This is a custom view designed primarily like a container of other views to show the names of collaborators
 * with proper styling.
 */
public class CollaboratorsInitialsView extends LinearLayout {

    // Should be implemented by the parent Fragment or Activity
    public interface ShowCollaboratorsListener {
        void onShowCollaborators(BoxIteratorCollaborations collaborations);
    }

    private ShowCollaboratorsListener mShowCollaboratorsListener;
    private LinearLayout mInitialsListView;
    private LinearLayout mInitialsListViewSection;
    protected BoxIteratorCollaborations mCollaborations;
    protected ShareController mController;
    protected BoxItem mShareItem;
    private BoxCollaborator mUnknownCollaborator;
    public static final String EXTRA_COLLABORATORS = "CollaboratorsInitialsView.ExtraCollaborators";
    public static final String EXTRA_SAVED_STATE = "CollaboratorsInitialsView.ExtraSaveState";

    private TextView mInitialsListHeader;
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
     * @param collaborationItem Box item
     * @param shareController Share controller used for making a request
     */
    public void setArguments(BoxCollaborationItem collaborationItem, ShareController shareController) {
        mShareItem = collaborationItem;
        mController = shareController;
    }

    /**
     * Sets up the child views
     */
    private void init() {
        inflate(getContext(), R.layout.view_collaborators_initial, this);
        mProgressBar = (ProgressBar) findViewById(R.id.box_sharesdk_activity_progress_bar);
        mInitialsListView = (LinearLayout) findViewById(R.id.invite_collaborator_initials_list);
        mInitialsListViewSection = (LinearLayout) findViewById(R.id.collaborator_initials_list_section);
        mInitialsListHeader = (TextView) findViewById(R.id.invite_collaborator_initials_list_header);

        JsonObject jsonObject = new JsonObject();
        jsonObject.add(BoxCollaborator.FIELD_NAME, "");
        mUnknownCollaborator = new BoxUser(jsonObject);
    }

    protected BoxCollaborationItem getCollaborationItem() {
        return (BoxCollaborationItem)mShareItem;
    }

    public final String getString(@StringRes int resId) {
        return getResources().getString(resId);
    }

    /**
     * Executes the request to retrieve collaborations for the folder
     */
    public void fetchCollaborations() {
        if (mController == null) {
            return;
        }

        if (getCollaborationItem() == null || SdkUtils.isBlank(getCollaborationItem().getId())) {
            mController.showToast(getContext(), getString(R.string.box_sharesdk_cannot_view_collaborations));
            return;
        }

        // Show spinner
        mProgressBar.setVisibility(VISIBLE);

        if (mBoxResponse == null) {
            // Execute request to fetch collaborators
            mController.fetchCollaborations(getCollaborationItem()).addOnCompletedListener(mCollaborationsListener);
        } else {
            // Dismiss spinner
            mProgressBar.setVisibility(GONE);
            updateView((BoxIteratorCollaborations)mBoxResponse.getResult());
        }
    }

    public void refreshView() {
        mBoxResponse = null;
        fetchCollaborations();
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
                            if (response.isSuccess() && getCollaborationItem() != null) {
                                updateView(response.getResult());
                            } else if (((BoxException)response.getException()).getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                                // The user is not a collaborator anymore
                                mController.showToast(activity, getString(R.string.box_sharesdk_item_unavailable));
                                activity.finish();
                            } else {
                                BoxLogUtils.e(CollaborationsFragment.class.getName(), "Fetch Collaborators request failed",
                                        response.getException());
                                mController.showToast(activity, getString(R.string.box_sharesdk_network_error));
                            }
                        }
                    });
                }
            };

    private void updateViewVisibilityForNoCollaborators() {
        TextView noCollaborators = (TextView) findViewById(R.id.no_collaborators_text);
        noCollaborators.setVisibility(VISIBLE);
        mInitialsListView.setVisibility(GONE);
    }

    private void updateViewVisibilityIfCollaboratorsFound() {
        TextView noCollaborators = (TextView) findViewById(R.id.no_collaborators_text);
        noCollaborators.setVisibility(GONE);
        mInitialsListView.setVisibility(VISIBLE);
    }

    private void updateView(BoxIteratorCollaborations boxIteratorCollaborations) {
        mCollaborations = boxIteratorCollaborations;
        if (mCollaborations == null || mCollaborations.size() == 0) {
            // There are no collaborators for mShareitem
            updateViewVisibilityForNoCollaborators();
            return;
        }

        updateViewVisibilityIfCollaboratorsFound();
        mInitialsListHeader.setVisibility(View.VISIBLE);
        int bestKnownCollabsSize = mCollaborations.size();
        if (mCollaborations.fullSize() != null) {
            bestKnownCollabsSize = mCollaborations.fullSize().intValue();
        }
        final int totalCollaborators = bestKnownCollabsSize;
        final int remainingWidth = mInitialsListView.getWidth();
        final ArrayList<BoxCollaboration> collaborations = mCollaborations.getEntries();

        clearInitialsView();
        mInitialsListView.post(new Runnable() {
            @Override
            public void run() {
                //Add the first item to calculate the width
                final View initialsView = addInitialsToList(collaborations.get(0).getAccessibleBy());
                initialsView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    private boolean initialsAdded = false;
                    @Override
                    public void onGlobalLayout() {
                        if (initialsView.isShown() && !initialsAdded) {
                            initialsAdded = true;
                            int viewWidth = initialsView.getWidth();
                            int viewsCount = remainingWidth / viewWidth;

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
                    }
                });
            }
        });
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

    public void setShowCollaboratorsListener(ShowCollaboratorsListener inviteCollaboratorsListener) {
        mShowCollaboratorsListener = inviteCollaboratorsListener;
        mInitialsListViewSection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShowCollaboratorsListener != null) {
                    mShowCollaboratorsListener.onShowCollaborators(mCollaborations);
                }
            }
        });
    }
}
