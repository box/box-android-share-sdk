package com.box.androidsdk.share.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class CollaboratorsAdapter extends BaseAdapter {

    private static final int[] THUMB_COLORS = new int[] { 0xff9e9e9e, 0xff63d6e4, 0xffff5f5f, 0xff7ed54a, 0xffaf21f4,
            0xffff9e57, 0xffe54343, 0xff5dc8a7, 0xfff271a4, 0xff2e71b6, 0xffe26f3c, 0xff768fba, 0xff56c156, 0xffefcf2e,
            0xff4dc6fc, 0xff501785, 0xffee6832, 0xffffb11d, 0xffde7ff1 };

    private ArrayList<BoxCollaboration> mItems = new ArrayList<BoxCollaboration>();
    private HashMap<String, Integer> mPositionMap = new HashMap<String, Integer>();
    private Context mContext;

    public CollaboratorsAdapter(Context context) {
        super();
        mContext = context;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_collaboration, parent, false);
            holder = new ViewHolder();
            holder.nameView = (TextView) convertView.findViewById(R.id.collaborator_role_title);
            holder.roleView = (TextView) convertView.findViewById(R.id.collaborator_role);
            holder.initialsView = (TextView) convertView.findViewById(R.id.collaborator_initials);
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
                setInitialsThumb(holder.initialsView, "");
            } else {
                name = collaborator.getName();
                setInitialsThumb(holder.initialsView, name);
            }
            String description = collaboration.getStatus() == BoxCollaboration.Status.ACCEPTED ?
                    CollaborationUtils.getRoleName(mContext, collaboration.getRole()) :
                    CollaborationUtils.getCollaborationStatusText(mContext, collaboration.getStatus());
            holder.nameView.setText(name);
            holder.collaboration = collaboration;
            holder.roleView.setText(description);
        }

        return convertView;
    }

    public synchronized void setItems(Collection<BoxCollaboration> items) {
        mItems.clear();
        mPositionMap.clear();
        BoxCollaboration[] itemsArr = items.toArray(new BoxCollaboration[items.size()]);
        for (int i = 0; i < itemsArr.length; ++i) {
            mItems.add(itemsArr[i]);
            mPositionMap.put(itemsArr[i].getId(), i);
        }
        notifyDataSetChanged();
    }

    public synchronized void update(BoxCollaboration item) {
        Integer position = mPositionMap.get(item.getId());
        if (position != null) {
            mItems.set(position.intValue(), item);
        }
        notifyDataSetChanged();
    }

    public synchronized void delete(String collabId) {
        Integer position = mPositionMap.get(collabId);
        if (position != null) {
            mItems.remove(position.intValue());
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        public TextView nameView;
        public TextView roleView;
        public TextView initialsView;
        public BoxCollaboration collaboration;
    }

    public void setInitialsThumb(TextView initialsView, String fullName) {
        char initial1 = '\u0000';
        char initial2 = '\u0000';
        if (fullName != null) {
            String[] nameParts = fullName.split(" ");
            if (nameParts[0].length() > 0) {
                initial1 = nameParts[0].charAt(0);
            }
            if (nameParts.length > 1) {
                initial2 = nameParts[nameParts.length - 1].charAt(0);
            }
        }
        Drawable drawable = initialsView.getResources().getDrawable(R.drawable.thumb_background);
        drawable.setColorFilter(THUMB_COLORS[(initial1 + initial2) % THUMB_COLORS.length], PorterDuff.Mode.MULTIPLY);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            initialsView.setBackground(drawable);
        } else {
            initialsView.setBackgroundDrawable(drawable);
        }
        initialsView.setText(initial1 + "" + initial2);
        initialsView.setTextAppearance(mContext, R.style.TextAppearance_AppCompat_Subhead);
        initialsView.setTextColor(mContext.getResources().getColor(R.color.box_sharesdk_background));
    }
}
