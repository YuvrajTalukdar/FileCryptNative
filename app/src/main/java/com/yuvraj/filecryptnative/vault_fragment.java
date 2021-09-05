package com.yuvraj.filecryptnative;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class vault_fragment extends Fragment {

    private Vault_Fragment_Listener listener;
    private RecyclerView recyclerView;
    private TextView vault_grid_text;
    private database db;
    private ArrayList<vault_info> vault_info_list;
    private adapter_vaultgrid adapter;
    private GridLayoutManager gridLayoutManager;
    private MainActivity mainActivity;

    public vault_fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {   super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_vault, container, false);

        mainActivity=(MainActivity)getActivity();
        db=new database(v.getContext());
        vault_info_list= mainActivity.vault_info_list;

        vault_grid_text=v.findViewById(R.id.vault_grid_text);
        recyclerView=v.findViewById(R.id.vault_grid);
        adapter=new adapter_vaultgrid(v.getContext(), vault_info_list, new adapter_vaultgrid.vault_data_adapter_listener() {
            @Override
            public void open_vault_dialog(int vault_id,String vault_name) {
                listener.open_vault_dialog(vault_id,vault_name);
            }

            @Override
            public void delete_vault_option(int vault_id,String vault_name,View view) {
                if(!mainActivity.is_vault_open()) {
                    PopupMenu popup = new PopupMenu(getContext(), view);
                    popup.getMenuInflater().inflate(R.menu.delete_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            delete_vault_dialog(vault_id, vault_name);
                            return true;
                        }
                    });
                    popup.show();
                }
                else
                {   Toast.makeText(getContext(), "Close already open vault first", Toast.LENGTH_SHORT).show();}
            }
        });
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density  = getResources().getDisplayMetrics().density;
        float dpWidth  = outMetrics.widthPixels / density;
        int columns = Math.round(dpWidth/100);
        gridLayoutManager=new GridLayoutManager(v.getContext(),columns);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        set_status();

        return v;
    }

    void add_data(vault_info vaultInfo)
    {
        vault_info_list.add(vaultInfo);
        adapter.notifyDataSetChanged();
    }

    void delete_vault_dialog(int info_id,String vault_name)
    {
        Map<String,Integer> map=get_color_id();
        String deep_color= String.format("#%06X", (0xFFFFFF & map.get("DeepColor")));
        String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
        map.clear();
        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getContext());
        materialAlertDialogBuilder.setTitle(Html.fromHtml("<font color="+medium_color+">Delete Vault</font>"));
        materialAlertDialogBuilder.setMessage(Html.fromHtml("<font color="+deep_color+">Do you want to delete vault '"+vault_name+"' ?</font>"));
        materialAlertDialogBuilder.setBackground(getContext().getDrawable(R.drawable.grey_background));
        materialAlertDialogBuilder.setPositiveButton(Html.fromHtml("<font color="+medium_color+">Yes</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                delete_vault(info_id,vault_name);
            }
        });
        materialAlertDialogBuilder.setNegativeButton(Html.fromHtml("<font color="+medium_color+">No</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        materialAlertDialogBuilder.show();
    }

    void delete_vault(int info_id,String vaultName)
    {
        for(int a=0;a<vault_info_list.size();a++)
        {
            if(vault_info_list.get(a).id==info_id)
            {   vault_info_list.remove(a);break;}
        }
        adapter.notifyDataSetChanged();
        db.delete_vault(info_id);
        //delete the vault folder
        String appPath = getContext().getApplicationContext().getFilesDir().getAbsolutePath();
        appPath=appPath+"/"+vaultName;
        File vaultDir = new File(appPath);
        try{
            File fileList[]=vaultDir.listFiles();
            for(int a=0;a<fileList.length;a++)
            {   fileList[a].delete();}
            vaultDir.delete();
        }
        catch (Exception e)
        {}
        if(vault_info_list.size()==0)
        {   set_status();}
        Toast.makeText(getContext(), "Vault Deleted", Toast.LENGTH_SHORT).show();
    }

    public Map<String,Integer> get_color_id()
    {
        Map<String,Integer> map=new HashMap<>();

        TypedValue typedValue1 = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.DeepColor, typedValue1, true);
        map.put("DeepColor", ContextCompat.getColor(getContext(), typedValue1.resourceId));

        TypedValue typedValue2 = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.MediumColor, typedValue2, true);
        map.put("MediumColor",ContextCompat.getColor(getContext(), typedValue2.resourceId));

        return map;
    }

    void set_status()
    {
        if(vault_info_list.size()!=0)
        {   vault_grid_text.setVisibility(View.INVISIBLE);}
        else
        {   vault_grid_text.setVisibility(View.VISIBLE);}
    }

    public void notify_item_removed(int index)
    {   adapter.notifyItemRemoved(index);}

    public void notify_change()
    {   adapter.notifyDataSetChanged();}

    interface Vault_Fragment_Listener
    {
        void open_vault_dialog(int vault_id,String vault_name);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof vault_fragment.Vault_Fragment_Listener)
        {
            listener=(vault_fragment.Vault_Fragment_Listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement Vault_Fragment_Listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}