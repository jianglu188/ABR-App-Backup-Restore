package com.appisode.appbackuprestore.model;

import android.graphics.drawable.Drawable;

import java.io.File;

public class BackupModel {

    private String app_name;
    private String packgae_name;
    private String version_name;
    private int version_code;
    private Drawable app_icon;
    private long app_memory;
    private boolean checked = false;
    private boolean exist = false;
    private File file;

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        setApp_memory(this.file.length());
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public long getApp_memory() {
        return app_memory;
    }

    private void setApp_memory(long app_memory) {
        this.app_memory = app_memory;
    }

    public Drawable getApp_icon() {
        return app_icon;
    }

    public void setApp_icon(Drawable app_icon) {
        this.app_icon = app_icon;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public String getPackgae_name() {
        return packgae_name;
    }

    public void setPackgae_name(String packgae_name) {
        this.packgae_name = packgae_name;
    }
}
