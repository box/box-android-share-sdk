package com.box.androidsdk.share.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiCollaboration;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.api.ShareController;

/**
 * Base class for Fragments in Share SDK
 * This fragment contains common code for all fragments
 */
public abstract class BoxFragment extends Fragment {
    public static final String EXTRA_ITEM = "BoxFragment.ExtraItem";
    public static final String EXTRA_USER_ID = "BoxFragment.ExtraUserId";

    protected BoxItem mShareItem;

    private static final int  DEFAULT_SPINNER_DELAY = 500;

    private ProgressDialog mDialog;
    private LastRunnableHandler mDialogHandler;

    protected ShareController mController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mDialogHandler = new LastRunnableHandler();
        String userId = null;

        if (savedInstanceState != null && savedInstanceState.getSerializable(EXTRA_ITEM) != null){
            userId = savedInstanceState.getString(EXTRA_USER_ID);
            mShareItem = (BoxItem)savedInstanceState.getSerializable(EXTRA_ITEM);
        } else if (getArguments() != null) {
            Bundle args = getArguments();
            userId = args.getString(EXTRA_USER_ID);
            mShareItem = (BoxItem)args.getSerializable(EXTRA_ITEM);
        }

        if (SdkUtils.isBlank(userId)) {
            Toast.makeText(getActivity(), R.string.box_sharesdk_session_is_not_authenticated, Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        }
        if (mShareItem == null){
            Toast.makeText(getActivity(), R.string.box_sharesdk_no_item_selected, Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_ITEM, mShareItem);
        super.onSaveInstanceState(outState);
    }

    public void SetController(ShareController controller) {
        mController = controller;
    }

    /**
     * Dismisses the spinner if it is currently showing
     */
    protected void dismissSpinner(){
        if (mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
        }
        mDialogHandler.cancelLastRunnable();
    }

    /**
     * Shows the spinner with the default wait messaging
     */
    protected void showSpinner(){
        showSpinner(R.string.boxsdk_Please_wait, R.string.boxsdk_Please_wait);
    }

    /**
     * Shows the spinner with a custom title and description
     *
     * @param stringTitleRes string resource for the spinner title
     * @param stringRes string resource for the spinner description
     */
    protected void showSpinner(final int stringTitleRes, final int stringRes) {
        mDialogHandler.queue(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mDialog != null && mDialog.isShowing()) {
                        return;
                    }
                    mDialog = ProgressDialog.show(getActivity(), getText(stringTitleRes), getText(stringRes));
                    mDialog.show();
                } catch (Exception e) {
                    // WindowManager$BadTokenException will be caught and the app would not display
                    // the 'Force Close' message
                    mDialog = null;
                    return;
                }
            }
        }, DEFAULT_SPINNER_DELAY);

    }

    /**
     * Helper method to hide a view ie. set the visibility to View.GONE
     *
     * @param view the view that should be hidden
     */
    protected void hideView(View view){
        view.setVisibility(View.GONE);
    }

    /**
     * Helper method to show a view ie. set the visibility to View.VISIBLE
     *
     * @param view the view that should be shown
     */
    protected void showView(View view){
        view.setVisibility(View.VISIBLE);
    }

    public static Bundle getBundle(BoxItem boxItem, String userId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_ITEM, boxItem);
        bundle.putString(EXTRA_USER_ID, userId);
        return bundle;
    }

    /**
     * Helper class used to keep track of last runnable queued.
     */
    private class LastRunnableHandler extends Handler {
        private Runnable mLastRunable;

        public void queue(final Runnable runnable, final int delay){
            cancelLastRunnable();
            postDelayed(runnable, delay);
            mLastRunable = runnable;
        }

        public void cancelLastRunnable(){
            if (mLastRunable != null){
                removeCallbacks(mLastRunable);
            }
        }

    }
}
