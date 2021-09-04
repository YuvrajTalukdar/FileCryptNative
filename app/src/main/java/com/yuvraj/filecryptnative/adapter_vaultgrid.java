package com.yuvraj.filecryptnative;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class adapter_vaultgrid extends RecyclerView.Adapter<adapter_vaultgrid.vault_holder> {

    Context context;
    List<vault_info> vault_info_list;
    public adapter_vaultgrid(Context context, ArrayList<vault_info> vault_info_list, vault_data_adapter_listener listener) {
        this.context = context;
        this.vault_info_list = vault_info_list;
        this.listener=listener;
    }
    @NonNull
    @Override
    public adapter_vaultgrid.vault_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.vault_icon_layout,parent,false);
        return new vault_holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull adapter_vaultgrid.vault_holder holder, int position) {
        holder.vault_name_textview.setText(vault_info_list.get(position).vault_name);
    }

    @Override
    public int getItemCount()
    {   return vault_info_list.size();}

    public interface vault_data_adapter_listener
    {
        void open_vault_dialog(int vault_id,String vault_name);
        void delete_vault_option(int vault_id,String vault_name,View view);
    }
    private vault_data_adapter_listener listener;

    public class vault_holder extends RecyclerView.ViewHolder{
        TextView vault_name_textview;
        LinearLayout vault_icon_layout;
        public vault_holder(@NonNull View itemView) {
            super(itemView);
            vault_name_textview=itemView.findViewById(R.id.vault_name_textview);
            vault_icon_layout=itemView.findViewById(R.id.vault_icon_layout);

            vault_icon_layout.setOnClickListener(view -> {
                listener.open_vault_dialog(vault_info_list.get(getAdapterPosition()).id,vault_info_list.get(getAdapterPosition()).vault_name);
            });
            vault_icon_layout.setOnLongClickListener(view -> {
                listener.delete_vault_option(vault_info_list.get(getAdapterPosition()).id,vault_info_list.get(getAdapterPosition()).vault_name,itemView);
                return true;
            });
        }
    }
}
