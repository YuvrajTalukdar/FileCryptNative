package com.yuvraj.filecryptnative;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class dialog_about extends AppCompatDialogFragment {
    private Button ok_button;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_about,null);
        builder.setView(view);
        ok_button=view.findViewById(R.id.about_dialog_ok_button);
        ok_button.setOnClickListener(view1 -> {
            dismiss();
        });

        return builder.create();
    }
}

