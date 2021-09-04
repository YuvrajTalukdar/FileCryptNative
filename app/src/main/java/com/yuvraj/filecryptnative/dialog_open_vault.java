package com.yuvraj.filecryptnative;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class dialog_open_vault extends AppCompatDialogFragment {
    private Button ok_button,cancel_button;
    private EditText password;
    private TextView vaultName;
    private dialog_open_vault_listener listener;
    private String vault_name;
    public dialog_open_vault(String vault_name)
    {
        this.vault_name=vault_name;
    }
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_open_vault,null);
        builder.setView(view);
        ok_button=view.findViewById(R.id.openvault_ok_button);
        ok_button.setOnClickListener(view1 -> {
            if(vaultName.getText().length()==0)
            {   Toast.makeText(getActivity(), "Enter Vault Name", Toast.LENGTH_SHORT).show();}
            else if(password.getText().length()==0)
            {   Toast.makeText(getActivity(), "Enter Password", Toast.LENGTH_SHORT).show();}
            else
            {
                listener.open_vault(vaultName.getText().toString(),password.getText().toString());
            }
        });
        cancel_button=view.findViewById(R.id.openvault_cancel_button);
        cancel_button.setOnClickListener(view1 -> {
            dismiss();
        });
        vaultName=view.findViewById(R.id.openvault_name_textview);
        vaultName.setText(vault_name);
        password=view.findViewById(R.id.openvault_password_edittext);

        return builder.create();
    }

    interface dialog_open_vault_listener
    {
        void open_vault(String vault_name,String password);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof dialog_open_vault.dialog_open_vault_listener)
        {
            listener=(dialog_open_vault.dialog_open_vault_listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement dialog_open_vault_listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}

