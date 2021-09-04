package com.yuvraj.filecryptnative;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class dialog_rename extends AppCompatDialogFragment {
    private Button ok_button,cancel_button;
    private TextView orig_textview;
    private EditText new_name_edittext;
    private rename_dialog_listener listener;
    int item_index;
    private String item_name;

    dialog_rename(int item_index,String item_name)
    {
        this.item_index=item_index;
        this.item_name=item_name;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_rename,null);
        builder.setView(view);
        cancel_button=view.findViewById(R.id.rename_cancel_button);
        cancel_button.setOnClickListener(view1 -> {
            dismiss();
        });
        ok_button=view.findViewById(R.id.rename_ok_button);
        ok_button.setOnClickListener(view1 -> {
            listener.rename_file(item_index,new_name_edittext.getText().toString());
            dismiss();
        });

        orig_textview=view.findViewById(R.id.orig_name_textview);
        orig_textview.setText(item_name);

        new_name_edittext=view.findViewById(R.id.new_name_edittext);

        return builder.create();
    }

    interface rename_dialog_listener
    {
        void rename_file(int item_index,String new_name);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof dialog_theme.theme_dialog_listener)
        {
            listener=(dialog_rename.rename_dialog_listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement rename_dialog_listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}

