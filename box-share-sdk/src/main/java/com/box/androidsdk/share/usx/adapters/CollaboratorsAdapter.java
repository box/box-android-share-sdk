package com.box.androidsdk.share.usx.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.DataBindingUtil;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxJsonObject;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxListItemCollaborationBinding;
import com.box.androidsdk.share.vm.BaseShareVM;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class CollaboratorsAdapter extends BaseAdapter {

    private ArrayList<BoxCollaboration> mItems = new ArrayList<BoxCollaboration>();
    private Context mContext;
    private final BoxCollaborator mAnotherPersonCollaborator;

    String userId;

    private BaseShareVM mBaseShareVM;

    public CollaboratorsAdapter(Context context, BaseShareVM baseShareVM) {
        super();
        mContext = context;
        mBaseShareVM = baseShareVM;
        // This item is used for displaying users that do not have a box account have been invited as a collaborator
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(BoxCollaborator.FIELD_NAME, mContext.getString(R.string.box_sharesdk_another_person));
        mAnotherPersonCollaborator = new BoxUser(jsonObject);
    }

    public List<BoxCollaboration> getBoxCollaborationList() {
        return mItems;
    }

    public BoxItem getShareItem() {
        return mBaseShareVM.getShareItem();
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
        if (getShareItem().getPermissions().contains(BoxItem.Permission.CAN_INVITE_COLLABORATOR)) {
            return true;
        }

        // In absence of permission, user can change permission only for self
        BoxCollaboration collaboration = mItems.get(position);
        if (collaboration != null && collaboration.getAccessibleBy() != null &&
                collaboration.getAccessibleBy().getId().equals(mBaseShareVM.getUserId())) {
            return true;
        }
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UsxListItemCollaborationBinding binding;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.usx_list_item_collaboration, parent, false);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        } else {
            binding = (UsxListItemCollaborationBinding) convertView.getTag();
        }
        BoxCollaboration collaboration = mItems.get(position);
        if (collaboration != null) {
            BoxCollaborator collaborator = collaboration.getAccessibleBy();
            String name;
            if (collaborator == null) {
                String email = collaboration.getInviteEmail();
                JsonObject jsonObject = new JsonObject();
                jsonObject.add(BoxCollaborator.FIELD_NAME, email);
                BoxCollaborator emailCollab = new BoxUser(jsonObject);
                if (!(email == null && email.isEmpty())) {
                    name = email;
                    binding.collaboratorInitials.loadUser(emailCollab, mBaseShareVM.getAvatarController());
                } else {
                    name = mContext.getString(R.string.box_sharesdk_another_person);
                    binding.collaboratorInitials.loadUser(mAnotherPersonCollaborator, mBaseShareVM.getAvatarController());
                }
            } else {
                name = collaborator.getName();
                binding.collaboratorInitials.loadUser(collaborator, mBaseShareVM.getAvatarController());
            }
            String description = collaboration.getStatus() == BoxCollaboration.Status.ACCEPTED ?
                    CollaborationUtils.getRoleName(mContext, collaboration.getRole()) :
                    CollaborationUtils.getCollaborationStatusText(mContext, collaboration.getStatus());
            binding.collaboratorRoleTitle.setText(name);
            binding.collaboratorRole.setText(description);
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

    public synchronized void setItems(List<BoxCollaboration> items) {
        mItems.clear();
        mItems.addAll(items);
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
}
