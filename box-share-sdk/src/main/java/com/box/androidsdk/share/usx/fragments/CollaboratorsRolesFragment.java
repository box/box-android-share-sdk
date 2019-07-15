package com.box.androidsdk.share.usx.fragments;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentCollaborationRolesBinding;
import com.box.androidsdk.share.utils.FragmentTitle;
import com.box.androidsdk.share.vm.SelectRoleShareVM;


public class CollaboratorsRolesFragment extends Fragment implements FragmentTitle {


    public interface RoleUpdateNotifier {
        void setRole(BoxCollaboration.Role role);
    }

    public static final String TAG = CollaboratorsRolesFragment.class.getName();
    SelectRoleShareVM vm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        UsxFragmentCollaborationRolesBinding binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_collaboration_roles, container, false);
        View view = binding.getRoot();

        vm = ViewModelProviders.of(getActivity()).get(SelectRoleShareVM.class);
        binding.setViewModel(vm);
        binding.setRoleUpdateNotifier(vm::setSelectedRole);
        return view;
    }

    @Override
    public int getFragmentTitle() {
        return R.string.box_sharesdk_title_access_level;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public int getFragmentSubtitle() {
        return -1;
    }

    public static CollaboratorsRolesFragment newInstance() {
        return new CollaboratorsRolesFragment();
    }
}
