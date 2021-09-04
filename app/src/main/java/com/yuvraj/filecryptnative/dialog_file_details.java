package com.yuvraj.filecryptnative;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class dialog_file_details extends AppCompatDialogFragment {
    private Button ok_button;
    private TextView file_name_textview,file_size_textview,import_date_textview;
    private String file_name,file_size,import_date;

    dialog_file_details(String file_name,String file_size,String import_data)
    {
        this.file_name=file_name;
        this.file_size=file_size;
        this.import_date=import_data;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_file_details,null);
        builder.setView(view);
        ok_button=view.findViewById(R.id.dialog_detail_ok);
        ok_button.setOnClickListener(view1 -> {
            dismiss();
        });
        file_name_textview=view.findViewById(R.id.file_name_textview);
        file_name_textview.setText(file_name);

        file_size_textview=view.findViewById(R.id.file_size_textview);
        file_size_textview.setText(file_size);

        import_date_textview=view.findViewById(R.id.import_date_textview);
        import_date_textview.setText(import_date);

        return builder.create();
    }
}

