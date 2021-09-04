package com.yuvraj.filecryptnative;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;

public class explore_vault_fragment extends Fragment {

    private int data_size=0;
    private MainActivity mainActivity;
    private Vault_Explore_Fragment_Listener listener;
    private TextView exploreVaultStatus;
    private RecyclerView recyclerView;
    private adapter_vaultExplorer adapter;
    private GridLayoutManager gridLayoutManager;

    public explore_vault_fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {   super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_explore_vault, container, false);
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                System.out.println("check5");
                return null;
            }
        });
        exploreVaultStatus=view.findViewById(R.id.vault_explorer_text);
        mainActivity=(MainActivity)getActivity();
        set_vault_status();

        recyclerView=view.findViewById(R.id.vault_explorer);
        adapter=new adapter_vaultExplorer(getContext(),mainActivity.vault_data_list,new adapter_vaultExplorer.explore_vault_adapter_listener(){
            @Override
            public void open_image(int item_id,int index) {
                listener.view_image(item_id,index);
            }

            @Override
            public void select_item(int item_id, int index) {
                listener.select_item(item_id,index);
            }

            @Override
            public void unselect_item(int item_id, int index) {
                listener.unselect_item(item_id,index);
            }
        });
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density  = getResources().getDisplayMetrics().density;
        float dpWidth  = outMetrics.widthPixels / density;
        int columns = Math.round(dpWidth/100);
        gridLayoutManager = new GridLayoutManager(getContext(),columns);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(gridLayoutManager);

        return view;
    }

    interface Vault_Explore_Fragment_Listener
    {
        void view_image(int item_id, int index);
        boolean is_vault_open();
        void select_item(int item_id, int index);
        void unselect_item(int item_id, int index);
    }

    public void set_vault_status()
    {   //System.out.println("check56");
        if(mainActivity.vault_data_list.size()>0)
        {   exploreVaultStatus.setVisibility(View.INVISIBLE);}
        else
        {
            if(listener.is_vault_open())
            {   exploreVaultStatus.setText("Vault Empty");}
            else
            {   exploreVaultStatus.setText("Vault Closed");}
            exploreVaultStatus.setVisibility(View.VISIBLE);
        }
    }

    public void select_all_item(boolean select)
    {
        adapter.select_mode_on=select;
        if(select)
        {   adapter.no_of_item_selected=mainActivity.vault_data_list.size();}
        else
        {   adapter.no_of_item_selected=0;}
        //adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof explore_vault_fragment.Vault_Explore_Fragment_Listener)
        {
            listener=(explore_vault_fragment.Vault_Explore_Fragment_Listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement Vault_Explore_Fragment_Listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        set_vault_status();
        if(mainActivity.vault_data_list.size()!=data_size)
        {
            System.out.println("refresh recycler view");
            adapter.notifyDataSetChanged();
        }
    }

    public void notify_change()
    {   adapter.notifyDataSetChanged();}

    public  void notify_change(int start,int end)
    {   adapter.notifyItemRangeChanged(start,end-start);}

    public void notify_range_deleted(int start,int end)
    {   adapter.notifyItemRangeRemoved(start,end-start);}

    public void notify_item_removed(int index)
    {   adapter.notifyItemRemoved(index);}

    public void notify_item_inserted(int index)
    {   adapter.notifyItemInserted(index);}

    public void notify_item_changed(int index)
    {   adapter.notifyItemChanged(index);}

    public void notify_item_range_inserted(int start,int end)
    {   adapter.notifyItemRangeInserted(start,end-start);}

    static class vault_data
    {
        int id;
        String file_name;
        String encrypted_file_path;
        String encrypted_file_name;
        Bitmap thumb_nail=null;
        //byte[] thumb_nail=null;
        Drawable icon=null;
        boolean selected=false;
    }
}