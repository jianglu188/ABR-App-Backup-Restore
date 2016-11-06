package com.appisode.appbackuprestore.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.appisode.appbackuprestore.R;
import com.appisode.appbackuprestore.adapter.RestoreListAdapter;
import com.appisode.appbackuprestore.data.AppConfig;
import com.appisode.appbackuprestore.data.Utils;
import com.appisode.appbackuprestore.model.RestoreModel;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class RestoreFragment extends Fragment {
    private ProgressBar progressBar;
    private ListView listView;
    private View view;
    public RestoreListAdapter rAdapter;
    private List<RestoreModel> apkList = new ArrayList<>();
    private LinearLayout lyt_not_found;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_restore, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        lyt_not_found = (LinearLayout) view.findViewById(R.id.lyt_not_found);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogApkFileOption(i);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    public void refreshList() {
        apkList = Utils.loadBackupAPK(getActivity());
        rAdapter = new RestoreListAdapter(getActivity(), apkList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        // Capture ListView item click
        listView.setMultiChoiceModeListener(multiChoiceModeListener);
        listView.setAdapter(rAdapter);
        if (apkList.size() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }

    }

    private boolean mode_checkall = false;

    private void toogleCheckAll() {
        mode_checkall = !mode_checkall;
        for (int i = 0; i < rAdapter.getCount(); i++) {
            listView.setItemChecked(i, mode_checkall);
        }
        if (mode_checkall) {
            rAdapter.selectAll();
        } else {
            rAdapter.resetSelected();
        }
    }

    public ActionMode getActionMode() {
        return act_mode;
    }

    private ActionMode act_mode = null;
    private AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            final int checkedCount = listView.getCheckedItemCount();
            mode.setTitle(checkedCount + " selected");
            //Toast.makeText(getActivity().getApplicationContext(), checkedCount + " selected", Toast.LENGTH_SHORT).show();
            rAdapter.setSelected(position, checked);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.action_check_all:
                    toogleCheckAll();
                    return true;
                case R.id.action_restore:
                    restoreApkFiles(rAdapter.getSelected());
                    return true;
                case R.id.action_delete:
                    deleteApkFiles(rAdapter.getSelected());
                    refreshList();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.restore_context_menu, menu);
            mode.setTitle(listView.getCheckedItemCount() + " conversation selected");
            act_mode = mode;
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //MainActivity.toolbar.setVisibility(View.VISIBLE);
            // TODO Auto-generated method stub
            // bAdapter.removeSelection();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return false;
        }
    };

    private void dialogApkFileOption(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final RestoreModel r = rAdapter.getItem(position);
        builder.setTitle("What would you like to do?");
        ListView listView = new ListView(getActivity());
        listView.setPadding(25, 25, 25, 25);
        String[] stringArray = new String[]{"Restore", "Share", "Delete file"};
        listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, stringArray));
        builder.setView(listView);
        final AppCompatDialog dialog = builder.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog.dismiss();
                List<RestoreModel> selected_apk = new ArrayList<>();
                selected_apk.add(r);
                switch (i) {
                    case 0:
                        restoreApkFiles(selected_apk);
                        //restore
                        break;
                    case 1:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        Uri fileUri = Uri.fromFile(r.getFile());
                        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        intent.setType("*/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                        //share
                        break;
                    case 2:
                        deleteApkFiles(selected_apk);
                        refreshList();
                        //Delete file
                        break;
                }
            }
        });

        dialog.show();
    }

    private void restoreApkFiles(List<RestoreModel> apklist) {
        for (RestoreModel restr : apklist) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.setDataAndType(Uri.fromFile(restr.getFile()), "application/vnd.android.package-archive");
            startActivity(intent);
        }
    }

    private void deleteApkFiles(List<RestoreModel> apklist) {
        for (RestoreModel restr : apklist) {
            if (restr.getFile().exists()) {
                restr.getFile().delete();
            }
        }
    }

    // give bottom space
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AdView mAdView = (AdView) view.findViewById(R.id.ad_view);
        if(AppConfig.ENABLE_ADSENSE && Utils.cekConnection(getActivity())){
            mAdView.setVisibility(View.VISIBLE);
        }else{
            mAdView.setVisibility(View.GONE);
        }
    }

}
