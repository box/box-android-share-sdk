package com.box.androidsdk.share.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.views.BoxAvatarView;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.ShareController;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;

public class CollaboratorsAdapter extends BaseAdapter {

    private ArrayList<BoxCollaboration> mItems = new ArrayList<BoxCollaboration>();
    private Context mContext;
    private BoxCollaborationItem mFolder;
    private ShareController mController;
    private final BoxCollaborator mAnotherPersonCollaborator;


    public CollaboratorsAdapter(Context context, BoxCollaborationItem collaborationItem, ShareController controller) {
        super();
        mContext = context;
        mFolder = collaborationItem;
        mController = controller;
        // This item is used for displaying users that do not have a box account have been invited as a collaborator
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(BoxCollaborator.FIELD_NAME, mContext.getString(R.string.box_sharesdk_another_person));
        mAnotherPersonCollaborator = new BoxUser(jsonObject);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        // User is allowed to change permissions if he has invite collaborator permissions
        if (mFolder.getPermissions().contains(BoxItem.Permission.CAN_INVITE_COLLABORATOR)) {
            return true;
        }

        // In absence of permission, user can change permission only for self
        BoxCollaboration collaboration = mItems.get(position);
        if (collaboration != null && collaboration.getAccessibleBy() != null &&
                collaboration.getAccessibleBy().getId().equals(mController.getCurrentUserId())) {
            return true;
        }

        return false;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_collaboration, parent, false);
            holder = new ViewHolder();
            holder.nameView = (TextView) convertView.findViewById(R.id.collaborator_role_title);
            holder.roleView = (TextView) convertView.findViewById(R.id.collaborator_role);
            holder.initialsView = (BoxAvatarView) convertView.findViewById(R.id.collaborator_initials);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        BoxCollaboration collaboration = mItems.get(position);
        if (collaboration != null) {
            BoxCollaborator collaborator = collaboration.getAccessibleBy();
            String name;
            if (collaborator == null) {
                name = mContext.getString(R.string.box_sharesdk_another_person);
                holder.initialsView.loadUser(mAnotherPersonCollaborator, mController.getAvatarController());
            } else {
                name = collaborator.getName();
                holder.initialsView.loadUser(collaborator, mController.getAvatarController());
            }
            String description = collaboration.getStatus() == BoxCollaboration.Status.ACCEPTED ?
                    CollaborationUtils.getRoleName(mContext, collaboration.getRole()) :
                    CollaborationUtils.getCollaborationStatusText(mContext, collaboration.getStatus());
            holder.nameView.setText(name);
            holder.collaboration = collaboration;
            holder.roleView.setText(description);
        }

        if (isEnabled(position)){
            convertView.setAlpha(1f);
        } else {
            convertView.setAlpha(.25f);
        }

        return convertView;
    }

    public synchronized void setItems(BoxIteratorCollaborations items) {
        mItems.clear();
        for (int i = 0; i < items.size(); i++) {
            mItems.add(items.get(i));
        }
        notifyDataSetChanged();
    }

    public synchronized void update(BoxCollaboration item) {
        Integer position = getPosition(item.getId());
        if (position != null) {
            mItems.set(position.intValue(), item);
        }
        notifyDataSetChanged();
    }

    public synchronized void delete(String collabId) {
        Integer position = getPosition(collabId);
        if (position != null) {
            mItems.remove(position.intValue());
        }
        notifyDataSetChanged();
    }

    public Integer getPosition(String id) {
        Integer position = null;
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).getId().equals(id)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public static class ViewHolder {
        public TextView nameView;
        public TextView roleView;
        public BoxAvatarView initialsView;
        public BoxCollaboration collaboration;
    }
}
