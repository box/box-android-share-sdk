package com.box.androidsdk.share.usx.fragments;

import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentCollaborationRolesBinding;
import com.box.androidsdk.share.fragments.CollaborationsFragment;
import com.box.androidsdk.share.vm.ActionbarTitleVM;
import com.box.androidsdk.share.vm.CollaborationsShareVM;
import com.box.androidsdk.share.vm.PresenterData;
import com.box.androidsdk.share.vm.SelectRoleShareVM;
import com.box.androidsdk.share.vm.ShareVMFactory;


public class CollaboratorsRolesFragment extends Fragment {


    private void setTitles() {
        ActionbarTitleVM actionbarTitleVM = ViewModelProviders.of(getActivity()).get(ActionbarTitleVM.class);
        actionbarTitleVM.setTitle(getString(R.string.box_sharesdk_title_access_level));
        actionbarTitleVM.setSubtitle(null);
    }

    public interface RoleUpdateNotifier {
        void setRole(BoxCollaboration.Role role);

        void notifyRemove();
    }

    public static final String TAG = CollaboratorsRolesFragment.class.getName();
    SelectRoleShareVM vm;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        UsxFragmentCollaborationRolesBinding binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_collaboration_roles, container, false);
        View view = binding.getRoot();

        setTitles();

        vm = ViewModelProviders.of(getActivity()).get(SelectRoleShareVM.class);
        binding.setViewModel(vm);
        binding.setRoleUpdateNotifier(new RoleUpdateNotifier() {
            @Override
            public void setRole(BoxCollaboration.Role role) {
                vm.setSelectedRole(role);
            }

            @Override
            public void notifyRemove() {
                showRemoveWarning();
            }
        });

        return view;
    }

    public static CollaboratorsRolesFragment newInstance(){
        return new CollaboratorsRolesFragment();
    }
    private void showRemoveWarning() {
        String deleteDifferentWarning = getResources().getString(R.string.box_sharesdk_warn_remove_different_collaboration_folder, vm.getName(),vm.getCollaboration().getItem().getName());
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.box_sharesdk_title_remove_different_collaboration_folder)
                .setMessage(deleteDifferentWarning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        vm.setRemoveSelected(true);
                        getActivity().onBackPressed();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        // do nothing
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).create();
        dialog.show();
    }

}
