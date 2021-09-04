package com.yuvraj.filecryptnative;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class dialog_create_vault extends AppCompatDialogFragment {
    private Button ok_button,cancel_button;
    private EditText vaultName,password,confirm_password;
    private dialog_create_vault_listener listener;
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_create_vault,null);
        builder.setView(view);
        ok_button=view.findViewById(R.id.createvault_ok_button);
        ok_button.setOnClickListener(view1 -> {
            if(vaultName.getText().length()==0)
            {   Toast.makeText(getActivity(), "Enter Vault Name", Toast.LENGTH_SHORT).show();}
            else if(password.getText().length()==0)
            {   Toast.makeText(getActivity(), "Enter Password", Toast.LENGTH_SHORT).show();}
            else if(confirm_password.getText().length()==0)
            {   Toast.makeText(getActivity(), "Confirm Password", Toast.LENGTH_SHORT).show();}
            else if(!password.getText().toString().equals(confirm_password.getText().toString()))
            {   Toast.makeText(getActivity(), "Please recheck your password", Toast.LENGTH_SHORT).show();}
            else
            {   listener.create_vault(vaultName.getText().toString(),password.getText().toString());}
        });
        cancel_button=view.findViewById(R.id.createvault_cancel_button);
        cancel_button.setOnClickListener(view1 -> {
            dismiss();
        });
        vaultName=view.findViewById(R.id.vaultname_edittext);
        password=view.findViewById(R.id.password_edittext);
        confirm_password=view.findViewById(R.id.confirm_pass_edittext);

        return builder.create();
    }

    interface dialog_create_vault_listener
    {
        void create_vault(String vault_name,String password);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof dialog_create_vault.dialog_create_vault_listener)
        {
            listener=(dialog_create_vault.dialog_create_vault_listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement dialog_create_vault_listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}

