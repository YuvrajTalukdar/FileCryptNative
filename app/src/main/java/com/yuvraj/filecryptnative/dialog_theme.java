package com.yuvraj.filecryptnative;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;

public class dialog_theme extends AppCompatDialogFragment {
    private Button ok_button;
    private CheckBox green,red,blue,grey,pink,violet;
    private ArrayList<CheckBox> checkbox_list;
    private theme_dialog_listener listener;
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_theme,null);
        builder.setView(view);
        ok_button=view.findViewById(R.id.theme_ok_button);
        ok_button.setOnClickListener(view1 -> {
            dismiss();
        });
        checkbox_list=new ArrayList();
        green=view.findViewById(R.id.green_color_scheme);
        green.setOnClickListener(view1 -> {
            select_theme(0);
        });
        checkbox_list.add(green);
        red=view.findViewById(R.id.red_color_scheme);
        red.setOnClickListener(view1 -> {
            select_theme(1);
        });
        checkbox_list.add(red);
        pink=view.findViewById(R.id.pink_color_scheme);
        pink.setOnClickListener(view1 -> {
            select_theme(2);
        });
        checkbox_list.add(pink);
        blue=view.findViewById(R.id.blue_color_scheme);
        blue.setOnClickListener(view1 -> {
            select_theme(3);
        });
        checkbox_list.add(blue);
        grey=view.findViewById(R.id.grey_color_scheme);
        grey.setOnClickListener(view1 -> {
            select_theme(4);
        });
        checkbox_list.add(grey);
        violet=view.findViewById(R.id.violet_color_scheme);
        violet.setOnClickListener(view1 -> {
            select_theme(5);
        });
        checkbox_list.add(violet);
        int current_theme_code=listener.get_theme_code();
        for(int a=0;a<checkbox_list.size();a++)
        {
            if(a==current_theme_code)
            {   checkbox_list.get(a).setChecked(true);break;}
        }

        return builder.create();
    }

    void select_theme(int theme_code)
    {
        for(int a=0;a<checkbox_list.size();a++)
        {
            if(a==theme_code)
            {   checkbox_list.get(a).setChecked(true);}
            else
            {   checkbox_list.get(a).setChecked(false);}
        }
        listener.theme_selected(theme_code);
    }

    interface theme_dialog_listener
    {
        void theme_selected(int theme_code);
        int get_theme_code();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof dialog_theme.theme_dialog_listener)
        {
            listener=(dialog_theme.theme_dialog_listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement theme_dialog_listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}

