package com.box.androidsdk.share.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.share.R;

public class InviteCollaboratorsFragment extends BoxFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_collaborators, container, false);
        return view;
    }

    public static InviteCollaboratorsFragment newInstance(BoxFolder folder, BoxSession session) {
        Bundle args = BoxFragment.getBundle(folder, session.getUserId());
        InviteCollaboratorsFragment fragment = new InviteCollaboratorsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
