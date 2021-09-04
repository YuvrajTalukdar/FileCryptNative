package com.yuvraj.filecryptnative;

import android.app.Application;

import java.util.ArrayList;

public class FileCryptNativeApplication extends Application {

    protected ArrayList<explore_vault_fragment.vault_data> vault_data_list=new ArrayList<>();
    protected ArrayList<explore_vault_fragment.vault_data> vault_data_list_temp=new ArrayList<>();
    protected String password=new String();

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
