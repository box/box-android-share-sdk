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
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.FragmentCollaborationRolesBinding;
import com.box.androidsdk.share.vm.SelectRoleShareVM;

import java.util.ArrayList;
import java.util.List;


public class CollaboratorsRolesFragment extends Fragment {



    SelectRoleShareVM vm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCollaborationRolesBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_collaboration_roles, container, false);

        View view = binding.getRoot();

        vm = ViewModelProviders.of(getActivity()).get(SelectRoleShareVM.class);
        binding.setViewModel(vm);
        binding.setRoleUpdateNotifier(vm::setSelectedRole);
        return view;
    }

    public static CollaboratorsRolesFragment newInstance(BoxCollaborationItem item) {
        return new CollaboratorsRolesFragment();
    }
    public interface RoleUpdateNotifier {
        void setRole(BoxCollaboration.Role role);
    }

}
