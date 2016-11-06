package com.appisode.appbackuprestore.data;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.appisode.appbackuprestore.model.BackupModel;
import com.appisode.appbackuprestore.model.RestoreModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<RestoreModel> loadBackupAPK(Context ctx) {
        List<RestoreModel> appList = new ArrayList<>();
        File root = new File(Constant.BACKUP_FOLDER);
        if (root.exists() && root.isDirectory()) {
            for (File f : root.listFiles()) {
                if ( f.length() > 0 && f.getPath().endsWith(".apk") ) {
                    String filePath = f.getPath();
                    PackageInfo pk = ctx.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
                    if (pk != null) {
                        ApplicationInfo info = pk.applicationInfo;
                        if ( Build.VERSION.SDK_INT >= 8 ) {
                            info.sourceDir = filePath;
                            info.publicSourceDir = filePath;
                        }
                        Drawable icon = info.loadIcon(ctx.getPackageManager());
                        RestoreModel app = new RestoreModel();
                        app.setIcon(icon);
                        app.setFile(f);
                        app.setPath(filePath);
                        app.setName(f.getName());
                        appList.add(app);
                    }
                }
            }
        }
       return appList;
    }

    public static List<BackupModel> backupExistChecker(List<BackupModel> backups, Context ctx){
        File root = new File(Constant.BACKUP_FOLDER);
        if (root.exists() && root.isDirectory()) {
            for (File f : root.listFiles()) {
                if ( f.length() > 0 && f.getPath().endsWith(".apk") ) {
                    for (int i = 0; i < backups.size(); i++) {
                        String name = backups.get(i).getApp_name()+"_"+backups.get(i).getVersion_name()+".apk";
                        if(f.getName().equals(name)){
                            backups.get(i).setExist(true);
                            break;
                        }
                    }
                }
            }
        }
        return backups;
    }

    public static boolean cekConnection(Context context){
        ConnectionDetector conn = new ConnectionDetector(context);
        if(conn.isConnectingToInternet()){
            return true;
        }else{
            return false;
        }
    }

}
