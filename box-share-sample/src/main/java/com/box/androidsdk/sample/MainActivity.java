package com.box.androidsdk.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxError;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.usx.activities.BoxActivity;
import com.box.androidsdk.share.usx.activities.BoxInviteCollaboratorsActivity;
import com.box.androidsdk.share.usx.activities.BoxSharedLinkActivity;

import java.net.HttpURLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_CODE_SHARE_LINK = 100;
    private static int REQUEST_CODE_INVITE_PEOPLE = 200;
    private static final String EXTRA_SAMPLE_FOLDER = "extraSampleFolder";

    BoxSession mSession = null;
    BoxApiFolder mFolderApi;
    private static final String SHARE_SAMPLE_FOLDER_NAME = "Box Share SDK Sample Folder";

    private BoxCollaborationItem mSampleItem;

    private ProgressDialog mDialog;

    Button mShareBtn;
    Button mCollabsBtn;
    Button mCreateSampleFolderBtn;
    TextView mChooseActionTv;
    LinearLayout mCreateSampleFolderContainer;
    LinearLayout mShareActionsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(R.string.box_sharesdk_sample_name);

        BoxConfig.IS_LOG_ENABLED = true;
        BoxConfig.CLIENT_ID = "9itlbj9i8mh5qxpj5vr2pjy08t43n3yy";
        BoxConfig.CLIENT_SECRET = "MVLrHgvscnPCpkNhiD2rrETuh2vNBftz";

        if (savedInstanceState != null) {
            mSampleItem = (BoxFolder) savedInstanceState.getSerializable(EXTRA_SAMPLE_FOLDER);
        }

        initialize();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_SAMPLE_FOLDER, mSampleItem);
        super.onSaveInstanceState(outState);
    }

    private void initialize(){
        mCreateSampleFolderContainer = (LinearLayout) findViewById(R.id.createSampleFolderContainer);
        mShareActionsContainer = (LinearLayout) findViewById(R.id.shareActionsContainer);
        mShareBtn = (Button)  findViewById(R.id.launchShare);
        mCollabsBtn = (Button)  findViewById(R.id.launchCollabs);
        mCreateSampleFolderBtn = (Button)  findViewById(R.id.createSampleFolder);
        mChooseActionTv = (TextView) findViewById(R.id.chooseAction);

        mChooseActionTv.setText(getString(R.string.box_sharesdk_sample_choose_action, SHARE_SAMPLE_FOLDER_NAME));

        mSession = new BoxSession(this);
        mSession.setSessionAuthListener(new BoxAuthentication.AuthListener() {
            @Override
            public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {

            }

            @Override
            public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       createOrFindTestFolder();
                   }
               });
            }

            @Override
            public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

            }

            @Override
            public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

            }
        });
        mSession.authenticate(this);
        mFolderApi = new BoxApiFolder(mSession);
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
                mSampleItem = folder;
                if (mSampleItem == null){
                    mCreateSampleFolderContainer.setVisibility(View.VISIBLE);
                    mShareActionsContainer.setVisibility(View.GONE);
                } else {
                    mCreateSampleFolderContainer.setVisibility(View.GONE);
                    mShareActionsContainer.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    public void onCreateSampleClick(final View view){
        createOrFindTestFolder();
    }

    /**
     * The logic required to launch the shared link creation/modification ui.
     * @param view
     */
    public void onShareLinkButtonClick(final View view){
        if (mSampleItem != null)
            startActivityForResult(com.box.androidsdk.share.activities.BoxSharedLinkActivity.getLaunchIntent(this, mSampleItem, mSession), REQUEST_CODE_SHARE_LINK);
    }

    /**
     * The logic required to launch the collaborator creation/modification ui.
     * @param view
     */
    public void onInvitePeopleButtonClick(final View view){
        if (mSampleItem != null) {
           startActivityForResult(com.box.androidsdk.share.activities.BoxInviteCollaboratorsActivity.getLaunchIntent(this, mSampleItem, mSession), REQUEST_CODE_INVITE_PEOPLE);

        }
    }

    /**
     * The logic required to launch the shared link creation/modification ui.
     * @param view
     */
    public void onShareLinkButtonClickNew(final View view){
        if (mSampleItem != null)
            startActivityForResult(BoxSharedLinkActivity.getLaunchIntent(this, mSampleItem, mSession), REQUEST_CODE_SHARE_LINK);
    }

    /**
     * The logic required to launch the collaborator creation/modification ui.
     * @param view
     */
    public void onInvitePeopleButtonClickNew(final View view){
        if (mSampleItem != null) {
            startActivityForResult(BoxInviteCollaboratorsActivity.getLaunchIntent(this, mSampleItem, mSession), REQUEST_CODE_INVITE_PEOPLE);

        }
    }

    private void deleteSampleFolder(){
        mDialog = ProgressDialog.show(MainActivity.this, getText(R.string.boxsdk_Please_wait), getText(R.string.boxsdk_Please_wait));
        new Thread(){
            public void run(){
                try {
                    if (mSampleItem != null){
                        mFolderApi.getDeleteRequest(mSampleItem.getId()).send();
                    } else {
                        BoxIteratorItems items = mFolderApi.getItemsRequest("0").send();
                        for (BoxItem item : items){
                            if (item.getName().equals(SHARE_SAMPLE_FOLDER_NAME)){
                                mSampleItem = (BoxCollaborationItem) item;
                                break;
                            }
                        }
                        if (mSampleItem != null){
                            mFolderApi.getDeleteRequest(mSampleItem.getId()).send();
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
                mSampleItem = (BoxCollaborationItem) new BoxActivity.ResultInterpreter(data).getBoxItem();
                // if your user created or modified a shared link during this flow you can use it for your own purposes.
                BoxSharedLink link = mSampleItem.getSharedLink();
                if (link != null) {
                    Toast.makeText(this, link.getURL(), Toast.LENGTH_LONG).show();
                }
            }
        }
        else if (requestCode == REQUEST_CODE_INVITE_PEOPLE){
            if (data != null){
                // update current data to the latest one returned from the shared link creation.
//                mSampleFolder = (BoxFolder) new BoxActivity.ResultInterpreter(data).getBoxItem();
                BoxCollaborationItem item = (BoxCollaborationItem) new BoxActivity.ResultInterpreter(data).getBoxItem();
                // if your user created or removed collaborations during this flow you can use this list for your own purposes.
                BoxIteratorCollaborations collaborations = new BoxActivity.ResultInterpreter(data).getCollaborations();
                if (collaborations != null) {
                    Toast.makeText(this, "Number of collaborators: " + collaborations.size(), Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.removeSampleFolder) {
            deleteSampleFolder();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
