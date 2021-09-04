package com.yuvraj.filecryptnative;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class adapter_vaultExplorer extends RecyclerView.Adapter<adapter_vaultExplorer.vault_holder>{
    private Context context;
    private ArrayList<explore_vault_fragment.vault_data> vault_data_list;
    public boolean select_mode_on=false;
    public int no_of_item_selected=0;

    public adapter_vaultExplorer(Context context, ArrayList<explore_vault_fragment.vault_data> vault_data_list,adapter_vaultExplorer.explore_vault_adapter_listener listener) {
        this.context = context;
        this.vault_data_list = vault_data_list;
        this.listener=listener;
    }
    @NonNull
    @Override
    public adapter_vaultExplorer.vault_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.explore_vault_icon_layout,parent,false);
        return new adapter_vaultExplorer.vault_holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull adapter_vaultExplorer.vault_holder holder, int position) {
        holder.item_name_textview.setText(vault_data_list.get(position).file_name);
        if(vault_data_list.get(position).icon==null)
        {
            //holder.imageView.clearColorFilter();
            //Glide.with(holder.itemView).load(vault_data_list.get(position).thumb_nail).into(holder.imageView);
            holder.imageView.setImageBitmap(vault_data_list.get(position).thumb_nail);
        }
        else
        {
            holder.imageView.setPadding(20,20,20,20);
            holder.imageView.setImageDrawable(vault_data_list.get(position).icon);
        }
        if(vault_data_list.get(position).selected)
        {   holder.checkImage.setVisibility(View.VISIBLE);}
        else
        {   holder.checkImage.setVisibility(View.INVISIBLE);}
    }

    @Override
    public int getItemCount()
    {   return vault_data_list.size();}

    public interface explore_vault_adapter_listener
    {
        void open_image(int item_id,int index);
        void select_item(int item_id,int index);
        void unselect_item(int item_id,int index);
    }
    private adapter_vaultExplorer.explore_vault_adapter_listener listener;

    public class vault_holder extends RecyclerView.ViewHolder{
        TextView item_name_textview;
        ImageView imageView,checkImage;
        public vault_holder(@NonNull View itemView) {
            super(itemView);
            item_name_textview=itemView.findViewById(R.id.vault_item_name_textview);
            imageView=itemView.findViewById(R.id.thumbnail_icon);
            checkImage=itemView.findViewById(R.id.check_image);

            checkImage.setOnClickListener(view -> {
                no_of_item_selected--;
                if(no_of_item_selected==0)
                {   select_mode_on=false;}
                vault_data_list.get(getAdapterPosition()).selected=false;
                checkImage.setVisibility(View.INVISIBLE);
                listener.unselect_item(vault_data_list.get(getAdapterPosition()).id,getAdapterPosition());
            });
            imageView.setOnClickListener(view -> {
                if(!select_mode_on)
                {
                    if(vault_data_list.get(getAdapterPosition()).thumb_nail!=null)
                    {   listener.open_image(vault_data_list.get(getAdapterPosition()).id,getAdapterPosition());}
                }
                else
                {
                    no_of_item_selected++;
                    vault_data_list.get(getAdapterPosition()).selected=true;
                    checkImage.setVisibility(View.VISIBLE);
                    listener.select_item(vault_data_list.get(getAdapterPosition()).id,getAdapterPosition());
                }
            });
            imageView.setOnLongClickListener(view -> {
                if(!select_mode_on)
                {
                    select_mode_on=true;
                    no_of_item_selected++;
                    vault_data_list.get(getAdapterPosition()).selected=true;
                    checkImage.setVisibility(View.VISIBLE);
                    listener.select_item(vault_data_list.get(getAdapterPosition()).id,getAdapterPosition());
                }
                return  true;
            });
        }
    }
}
