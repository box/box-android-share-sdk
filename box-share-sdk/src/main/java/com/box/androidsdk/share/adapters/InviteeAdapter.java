package com.box.androidsdk.share.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.internal.models.BoxInvitee;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;

public class InviteeAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private final ArrayList<BoxInvitee> mInvitees = new ArrayList<BoxInvitee>();
    private final ArrayList<BoxInvitee> mItems = new ArrayList<BoxInvitee>();

    private Filter mInviteeFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null) {
                return results;
            }

            ArrayList<BoxInvitee> filteredList = new ArrayList<BoxInvitee>();
            for (BoxInvitee invitee : mInvitees) {
                if (invitee != null &&
                        (invitee.getName() != null && invitee.getName().toLowerCase().contains(constraint.toString().toLowerCase())) ||
                        (invitee.getEmail() != null && invitee.getEmail().toLowerCase().contains(constraint.toString().toLowerCase()))) {
                    filteredList.add(invitee);
                }
            }

            // append additional contacts from the phone contacts
            if (isReadContactsPermissionAvailable()) {

                String filter = ContactsContract.CommonDataKinds.Email.DATA + " LIKE '%" + constraint + "%' OR " +
                        ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + constraint + "%'";
                Cursor contactsCursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, filter, null, null);
                while (contactsCursor.moveToNext()) {
                    String name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String email = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    if (name.contains(constraint) || email.contains(constraint)) {
                        JsonObject object = new JsonObject();
                        object.add(BoxInvitee.FIELD_NAME, name);
                        object.add(BoxInvitee.FIELD_EMAIL, email);
                        filteredList.add(new BoxInvitee(object));
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mItems.clear();
            if (results != null && results.count > 0) {
                mItems.addAll((ArrayList<BoxInvitee>) results.values);
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };

    public InviteeAdapter(Context context) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_collaboration_invitee, null);
            TextView nameView = (TextView)convertView.findViewById(R.id.collaboration_invitee_name);
            TextView emailView = (TextView)convertView.findViewById(R.id.collaboration_invitee_email);
            TextView initialsView = (TextView) convertView.findViewById(R.id.collaborator_initials);
            holder = new ViewHolder(nameView, emailView, initialsView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BoxInvitee invitee = mItems.get(position);
        holder.getNameView().setText(invitee.getName());
        holder.getEmailView().setText(invitee.getEmail());
        CollaborationUtils.setInitialsThumb(mContext, holder.getInitialsView(), invitee.getName());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mInviteeFilter;
    }

    public void setInvitees(BoxIteratorInvitees invitees) {
        mInvitees.clear();
        for (BoxInvitee invitee: invitees) {
            mInvitees.add(invitee);
        }
    }


    public static class ViewHolder {
        private TextView mNameView;
        private TextView mEmailView;
        private TextView mInitials;

        public ViewHolder(TextView name, TextView email, TextView initials) {
            mNameView = name;
            mEmailView = email;
            mInitials = initials;
        }

        public TextView getNameView() {
            return mNameView;
        }

        public TextView getEmailView() {
            return mEmailView;
        }

        public TextView getInitialsView() { return mInitials; }
    }


    private boolean isReadContactsPermissionAvailable() {
        String readContactsPerm = "android.permission.READ_CONTACTS";
        int res = mContext.checkCallingOrSelfPermission(readContactsPerm);
        return res == PackageManager.PERMISSION_GRANTED;
    }
}
