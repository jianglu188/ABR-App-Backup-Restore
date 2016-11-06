package com.appisode.appbackuprestore.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appisode.appbackuprestore.R;
import com.appisode.appbackuprestore.adapter.BackupListAdapter;
import com.appisode.appbackuprestore.data.AppConfig;
import com.appisode.appbackuprestore.data.Constant;
import com.appisode.appbackuprestore.data.Utils;
import com.appisode.appbackuprestore.model.BackupModel;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BackupFragment extends Fragment {
    private ProgressBar progressBar;
    private ListView listView;
    private View view;
    public BackupListAdapter bAdapter;
    private PackageManager pm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_backup, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogAppOption(i);
            }
        });
        pm = (PackageManager) getActivity().getApplicationContext().getPackageManager();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh(false);
    }

    public void refresh(boolean fab_flag) {
        if (taskRunning) {
            Snackbar.make(view, "Task still running", Snackbar.LENGTH_SHORT).show();
        } else {
            new AppListLoaderTask(fab_flag).execute();
        }
    }

    private boolean taskRunning = false;

    private class AppListLoaderTask extends AsyncTask<String, String, String> {
        private String status = "";
        private List<BackupModel> app_list = new ArrayList<>();
        private boolean fab_flag = false;


        public AppListLoaderTask(boolean fab_flag) {
            this.fab_flag = fab_flag;
        }

        @Override
        protected void onPreExecute() {
            taskRunning = true;
            app_list.clear();
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                List<PackageInfo> packs = pm.getInstalledPackages(0);
                for (int i = 0; i < packs.size(); i++) {
                    PackageInfo p = packs.get(i);
                    if ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        continue;
                    }
                    BackupModel app = new BackupModel();
                    app.setApp_name(p.applicationInfo.loadLabel(pm).toString());
                    app.setPackgae_name(p.packageName);
                    app.setVersion_name(p.versionName);
                    app.setVersion_code(p.versionCode);
                    app.setApp_icon(p.applicationInfo.loadIcon(pm));
                    app.setFile(new File(p.applicationInfo.publicSourceDir));
                    app_list.add(app);
                }
                status = "success";
            } catch (Exception e) {
                status = "failed";
            }
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            progressBar.setVisibility(View.GONE);
            if (status.equals("success")) {
                Collections.sort(app_list, new Comparator<BackupModel>() {
                    @Override
                    public int compare(BackupModel b_1, BackupModel b_2) {
                        String l1 = b_1.getApp_name().toLowerCase();
                        String l2 = b_2.getApp_name().toLowerCase();
                        return l1.compareTo(l2);
                    }
                });
                app_list = Utils.backupExistChecker(app_list, getActivity());
                bAdapter = new BackupListAdapter(getActivity(), app_list);
                listView.setAdapter(bAdapter);
                setMultipleChoice();
            } else {
                Snackbar.make(view, "Failed load your applications!", Snackbar.LENGTH_SHORT).show();
            }
            taskRunning = false;
            if (fab_flag) {
                Snackbar.make(view, "Refresh finished", Snackbar.LENGTH_SHORT).show();
            }
            super.onProgressUpdate(values);
        }
    }

    private class FileSaveTask extends AsyncTask<Void, Integer, File> {

        private ProgressDialog progress;
        private List<BackupModel> selected_app;

        public FileSaveTask(List<BackupModel> selected_app) {
            this.selected_app = selected_app;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(getActivity());
            progress.setMessage("App Backup");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setMax(selected_app.size());
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected File doInBackground(Void... params) {
            int i = 0;
            File outputFile = null;
            while (selected_app.size() > i) {
                String filename = selected_app.get(i).getApp_name() + "_" + selected_app.get(i).getVersion_name() + ".apk";
                outputFile = new File(Constant.BACKUP_FOLDER);
                if (!outputFile.exists()) {
                    outputFile.mkdirs();
                }
                File apk = new File(outputFile.getPath() + "/" + filename);
                try {
                    apk.createNewFile();
                    InputStream in = new FileInputStream(selected_app.get(i).getFile());
                    OutputStream out = new FileOutputStream(apk);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                publishProgress(i);
                i++;
            }
            return outputFile;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progress.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(File result) {
            if (progress != null) {
                progress.dismiss();
            }

            if (result != null) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setCancelable(false);
                alert.setTitle("Backup Completed");
                alert.setMessage("App Location: " + Constant.BACKUP_FOLDER);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dg, int arg1) {
                        bAdapter.resetSelected();
                        bAdapter.notifyDataSetChanged();
                        refresh(false);
                        dg.dismiss();
                    }
                });
                alert.show();
            } else {
                Toast.makeText(getActivity(), "App backup failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean mode_checkall = false;

    private void toogleCheckAll() {
        mode_checkall = !mode_checkall;
        for (int i = 0; i < bAdapter.getCount(); i++) {
            listView.setItemChecked(i, mode_checkall);
        }
        if (mode_checkall) {
            bAdapter.selectAll();
        } else {
            bAdapter.resetSelected();
        }
    }

    private void setMultipleChoice() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        // Capture ListView item click
        listView.setMultiChoiceModeListener(multiChoiceModeListener);
    }

    public ActionMode getActionMode() {
        return act_mode;
    }

    private ActionMode act_mode = null;
    private MultiChoiceModeListener multiChoiceModeListener = new MultiChoiceModeListener() {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            final int checkedCount = listView.getCheckedItemCount();
            mode.setTitle(checkedCount + " selected");
            //Toast.makeText(getActivity().getApplicationContext(), checkedCount + " selected", Toast.LENGTH_SHORT).show();
            bAdapter.setSelected(position, checked);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.action_check_all:
                    toogleCheckAll();
                    return true;
                case R.id.action_backup:
                    new FileSaveTask(bAdapter.getSelected()).execute();
                    return true;
                case R.id.action_uninstall:
                    uninstallApp(bAdapter.getSelected());
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.backup_context_menu, menu);
            mode.setTitle(listView.getCheckedItemCount() + " conversation selected");
            act_mode = mode;
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for (int i = 0; i < bAdapter.getCount(); i++) {
                listView.setItemChecked(i, mode_checkall);
            }
            bAdapter.resetSelected();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return false;
        }
    };

    private void dialogAppOption(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final BackupModel m = bAdapter.getItem(position);
        builder.setTitle("What would you like to do?");
        ListView listView = new ListView(getActivity());
        listView.setPadding(25, 25, 25, 25);
        String[] stringArray = new String[]{"Backup", "Uninstall", "Details"};
        listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, stringArray));
        builder.setView(listView);
        final AppCompatDialog dialog = builder.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog.dismiss();
                List<BackupModel> selected_app = new ArrayList<>();
                selected_app.add(m);
                switch (i) {
                    case 0:
                        //backup
                        new FileSaveTask(selected_app).execute();
                        break;
                    case 1:
                        //uninstall
                        uninstallApp(selected_app);
                        break;

                    case 2:
                        //Details
                        showInstalledAppDetails(m.getPackgae_name());
                        break;
                }
            }
        });

        dialog.show();
    }

    private void uninstallApp(List<BackupModel> selected_app) {
        for (BackupModel b : selected_app) {
            Uri uri = Uri.fromParts("package", b.getPackgae_name(), null);
            Intent it = new Intent(Intent.ACTION_DELETE, uri);
            startActivity(it);
        }
    }

    private void showInstalledAppDetails(String packageName) {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
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
