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

import com.box.androidsdk.share.R;
import com.box.androidsdk.share.internal.BoxInvitee;
import com.box.androidsdk.share.internal.BoxListInvitees;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

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
        View rowView = LayoutInflater.from(mContext).inflate(R.layout.list_item_collaboration_invitee, null);
        BoxInvitee invitee = mItems.get(position);

        ViewHolder holder = (ViewHolder) rowView.getTag();
        if (holder == null) {
            TextView nameView = (TextView)rowView.findViewById(R.id.collaboration_invitee_name);
            TextView emailView = (TextView)rowView.findViewById(R.id.collaboration_invitee_email);
            holder = new ViewHolder(nameView, emailView);
        }

        holder.getNameView().setText(invitee.getName());
        holder.getEmailView().setText(invitee.getEmail());
        return rowView;
    }

    @Override
    public Filter getFilter() {
        return mInviteeFilter;
    }

    public void setInvitees(BoxListInvitees invitees) {
        mInvitees.clear();
        for (BoxInvitee invitee: invitees) {
            mInvitees.add(invitee);
        }
    }


    public static class ViewHolder {
        private TextView mNameView;
        private TextView mEmailView;

        public ViewHolder(TextView name, TextView email) {
            mNameView = name;
            mEmailView = email;
        }

        public TextView getNameView() {
            return mNameView;
        }

        public TextView getEmailView() {
            return mEmailView;
        }
    }


    private boolean isReadContactsPermissionAvailable() {
        String readContactsPerm = "android.permission.READ_CONTACTS";
        int res = mContext.checkCallingOrSelfPermission(readContactsPerm);
        return res == PackageManager.PERMISSION_GRANTED;
    }
}
