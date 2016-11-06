package com.appisode.appbackuprestore.model;

import android.graphics.drawable.Drawable;

import java.io.File;

public class RestoreModel {

    private String name;
    private String path;
    private File file;
    private Drawable icon;
    private long app_memory;
    private boolean checked;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        setApp_memory(this.file.length());
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public long getApp_memory() {
        return app_memory;
    }

    private void setApp_memory(long app_memory) {
        this.app_memory = app_memory;
    }
}
