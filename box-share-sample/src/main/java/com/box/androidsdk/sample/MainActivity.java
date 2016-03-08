package com.box.androidsdk.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxError;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.activities.BoxActivity;
import com.box.androidsdk.share.activities.BoxCollaborationsActivity;
import com.box.androidsdk.share.activities.BoxSharedLinkActivity;

import java.net.HttpURLConnection;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private static int REQUEST_CODE_SHARE_LINK = 100;
    private static int REQUEST_CODE_INVITE_PEOPLE = 200;
    private static final String EXTRA_SAMPLE_FOLDER = "extraSampleFolder";

    BoxSession mSession = null;
    BoxApiFolder mFolderApi;
    private static final String SHARE_SAMPLE_FOLDER_NAME = "Box Share SDK Sample Folder";

    private BoxFolder mSampleFolder;

    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BoxConfig.IS_LOG_ENABLED = true;
        BoxConfig.CLIENT_ID = "your_client_id";
        BoxConfig.CLIENT_SECRET = "your_client_secret";

        if (savedInstanceState != null) {
            mSampleFolder = (BoxFolder) savedInstanceState.getSerializable(EXTRA_SAMPLE_FOLDER);
        }

        initialize();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_SAMPLE_FOLDER, mSampleFolder);
        super.onSaveInstanceState(outState);
    }

    private void initialize(){
        mSession = new BoxSession(this);
        mSession.authenticate();
        mFolderApi = new BoxApiFolder(mSession);
        createOrFindTestFolder();
    }

    private void createOrFindTestFolder(){
        mDialog = ProgressDialog.show(MainActivity.this, getText(R.string.boxsdk_Please_wait), getText(R.string.boxsdk_Please_wait));
        new Thread(){
            public void run(){
                try {
                    setSampleFolder(mFolderApi.getCreateRequest("0", SHARE_SAMPLE_FOLDER_NAME).send());
                } catch (BoxException e){
                    BoxError error = e.getAsBoxError();
                    if (error != null && error.getStatus() == HttpURLConnection.HTTP_CONFLICT){
                        ArrayList<BoxEntity> conflicts = error.getContextInfo().getConflicts();
                        if (conflicts != null && conflicts.size() == 1 && conflicts.get(0) instanceof BoxFolder){
                            setSampleFolder( (BoxFolder)conflicts.get(0));
                        }
                    }
                } finally{
                    mDialog.dismiss();
                }
            }
        }.start();

    }

    private void setSampleFolder(final BoxFolder folder){
        runOnUiThread( new Runnable(){
            @Override
            public void run() {
                mSampleFolder = folder;
                if (mSampleFolder == null){
                    findViewById(R.id.launchShare).setVisibility(View.GONE);
                    findViewById(R.id.launchCollabs).setVisibility(View.GONE);
                    findViewById(R.id.removeSampleFolder).setVisibility(View.GONE);
                    findViewById(R.id.createSampleFolder).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.launchShare).setVisibility(View.VISIBLE);
                    findViewById(R.id.launchCollabs).setVisibility(View.VISIBLE);
                    findViewById(R.id.removeSampleFolder).setVisibility(View.VISIBLE);
                    findViewById(R.id.createSampleFolder).setVisibility(View.GONE);
                }
            }
        });
    }


    public void onCreateSampleClick(final View view){
        createOrFindTestFolder();
    }


    public void onRemoveSampleClick(final View view){
        deleteSampleFolder();
    }

    /**
     * The logic required to launch the shared link creation/modification ui.
     * @param view
     */
    public void onShareLinkButtonClick(final View view){
        if (mSampleFolder != null)
            startActivityForResult(BoxSharedLinkActivity.getLaunchIntent(this, mSampleFolder, mSession), REQUEST_CODE_SHARE_LINK);
    }

    /**
     * The logic required to launch the collaborator creation/modification ui.
     * @param view
     */
    public void onInvitePeopleButtonClick(final View view){
        if (mSampleFolder != null)
            startActivityForResult(BoxCollaborationsActivity.getLaunchIntent(this, mSampleFolder, mSession), REQUEST_CODE_INVITE_PEOPLE);
    }

    private void deleteSampleFolder(){
        mDialog = ProgressDialog.show(MainActivity.this, getText(R.string.boxsdk_Please_wait), getText(R.string.boxsdk_Please_wait));
        new Thread(){
            public void run(){
                try {
                    if (mSampleFolder != null){
                        mFolderApi.getDeleteRequest(mSampleFolder.getId()).send();
                    } else {
                        BoxIteratorItems items = mFolderApi.getItemsRequest("0").send();
                        for (BoxItem item : items){
                            if (item.getName().equals(SHARE_SAMPLE_FOLDER_NAME)){
                                mSampleFolder = (BoxFolder)item;
                                break;
                            }
                        }
                        if (mSampleFolder != null){
                            mFolderApi.getDeleteRequest(mSampleFolder.getId()).send();
                        }
                    }
                    setSampleFolder(null);
                } catch (BoxException e){
                    Toast.makeText(MainActivity.this, "There was a problem deleting sample folder", Toast.LENGTH_LONG).show();
                }
                finally {
                    mDialog.dismiss();
                }
            }
        }.start();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SHARE_LINK){
            if (data != null){
                // update current data to the latest one returned from the shared link creation.
                mSampleFolder = (BoxFolder) new BoxActivity.ResultInterpreter(data).getBoxItem();
                // if your user created or modified a shared link during this flow you can use it for your own purposes.
                BoxSharedLink link = mSampleFolder.getSharedLink();
                if (link != null) {
                    Toast.makeText(this, link.getURL(), Toast.LENGTH_LONG).show();
                }
            }
        }
        else if (requestCode == REQUEST_CODE_INVITE_PEOPLE){
            if (data != null){
                // update current data to the latest one returned from the shared link creation.
                mSampleFolder = (BoxFolder) new BoxActivity.ResultInterpreter(data).getBoxItem();
                // if your user created or removed collaborations during this flow you can use this list for your own purposes.
                BoxIteratorCollaborations collaborations = new BoxActivity.ResultInterpreter(data).getCollaborations();
                if (collaborations != null) {
                    Toast.makeText(this, "Number of collaborators: " + collaborations.size(), Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
