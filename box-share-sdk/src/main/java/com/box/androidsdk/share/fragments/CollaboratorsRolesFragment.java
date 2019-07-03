package com.box.androidsdk.share.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.activities.BoxInviteCollaboratorsActivity;
import com.box.androidsdk.share.databinding.FragmentCollaborationRolesBinding;
import com.box.androidsdk.share.vm.SelectRoleShareVM;


public class CollaboratorsRolesFragment extends Fragment {


    public static final String TAG = CollaboratorsRolesFragment.class.getName();
    SelectRoleShareVM vm;
    private BoxFragment.ActionBarTitleChanger mActionBarTitleChanger;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCollaborationRolesBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_collaboration_roles, container, false);
        View view = binding.getRoot();
        mActionBarTitleChanger.setTitle(getString(R.string.box_sharesdk_title_access_level));

        vm = ViewModelProviders.of(getActivity()).get(SelectRoleShareVM.class);
        binding.setViewModel(vm);
        binding.setRoleUpdateNotifier(vm::setSelectedRole);
        return view;
    }

    public static CollaboratorsRolesFragment newInstance() {
        return new CollaboratorsRolesFragment();
    }

    public void setActionBarTitleChanger(BoxFragment.ActionBarTitleChanger changer) {
        this.mActionBarTitleChanger = changer;
    }
    public interface RoleUpdateNotifier {
        void setRole(BoxCollaboration.Role role);
    }

}
