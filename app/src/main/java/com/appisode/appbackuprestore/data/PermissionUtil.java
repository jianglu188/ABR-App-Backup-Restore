package com.appisode.appbackuprestore.data;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;


public abstract class PermissionUtil {

    private static AlertDialog alertDialog = null;

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void goToPermissionSettingScreen(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static boolean isGroupPermissionGranted(Activity activity, String[] permission) {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if (permission.length == 0) return false;
            for (String s : permission) {
                if (ActivityCompat.checkSelfPermission(activity, s) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean showRequestPermissionRationale(Activity activity, String[] permission) {
        if (permission.length == 0) return false;
        for (String s : permission) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, s)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllPermissionGranted(Activity activity) {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            String[] permission = Constant.ALL_REQUIRED_PERMISSION;
            if (permission.length == 0) return false;
            for (String s : permission) {
                if (ActivityCompat.checkSelfPermission(activity, s) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
