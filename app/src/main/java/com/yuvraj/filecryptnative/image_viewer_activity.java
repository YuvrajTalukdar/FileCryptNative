package com.yuvraj.filecryptnative;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class image_viewer_activity extends AppCompatActivity {

    private SharedPreferences settings_reader;
    private ActionBar actionBar;
    private String file_name,encrypted_file_path;
    private boolean display_actionBar=true;
    private String password;
    private AES aes_handler;
    SubsamplingScaleImageView imageView;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings_reader = getSharedPreferences("settings", Context.MODE_PRIVATE);
        set_theme(settings_reader.getInt("color_scheme_code", 3));
        setContentView(R.layout.activity_image_viewer);

        aes_handler=new AES();
        file_name=getIntent().getStringExtra("file_name");
        encrypted_file_path=getIntent().getStringExtra("encrypted_file_path");
        password=getIntent().getStringExtra("password");

        actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Spannable title = new SpannableString(file_name);
        Map<String,Integer> map=get_color_id();
        title.setSpan(new ForegroundColorSpan(map.get("MediumColor")), 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        actionBar.setTitle(title);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(view -> {
            if(display_actionBar)
            {
                actionBar.hide();
                display_actionBar=false;
            }
            else
            {
                actionBar.show();
                display_actionBar=true;
            }
        });
        get_image_bitmap_starter();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Task<Bitmap> get_image_bitmap()
    {
        return Tasks.call(mExecutor,()->{
            short first_chunk_size=8224;
            short size=8208;
            byte[] first_chunk_buffer=new byte[first_chunk_size];
            byte[] buffer=new byte[size];
            Bitmap bitmap;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileInputStream fin = new FileInputStream(encrypted_file_path);
                BufferedInputStream bins = new BufferedInputStream(fin);
                //first chunk
                bins.read(first_chunk_buffer);
                baos.write(aes_handler.decrypt_bytes(first_chunk_buffer, password));
                //rest of the data
                while (bins.read(buffer) != -1) {
                    baos.write(aes_handler.decrypt_bytes(buffer, password));
                }
                bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
                baos.close();
                aes_handler.shutdown_byte_operations();
                fin.close();
                bins.close();

                return bitmap;
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return null;
            }
        });
    }

    private void get_image_bitmap_starter() {
        progressDialog=new ProgressDialog(this,R.style.ProgressBar);
        progressDialog.setTitle("Progress");
        progressDialog.setMessage("Opening Image");
        progressDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        progressDialog.setCancelable(false);
        get_image_bitmap().addOnCompleteListener(task -> {
            imageView.setImage(ImageSource.bitmap(task.getResult()));
            progressDialog.dismiss();
        });
    }

    private void set_theme(int theme_code)
    {
        if(theme_code==0)
        {   setTheme(R.style.DarkGreenTheme);}
        else if(theme_code==1)
        {    setTheme(R.style.DarkRedTheme);}
        else if(theme_code==2)
        {   setTheme(R.style.DarkPinkTheme);}
        else if(theme_code==3)
        {   setTheme(R.style.DarkBlueTheme);}
        else if(theme_code==4)
        {   setTheme(R.style.DarkGreyTheme);}
        else if(theme_code==5)
        {   setTheme(R.style.DarkVioletTheme);}
    }

    private Map<String,Integer> get_color_id()
    {
        Map<String,Integer> map=new HashMap<>();

        TypedValue typedValue1 = new TypedValue();
        this.getTheme().resolveAttribute(R.attr.DeepColor, typedValue1, true);
        map.put("DeepColor", ContextCompat.getColor(this, typedValue1.resourceId));

        TypedValue typedValue2 = new TypedValue();
        this.getTheme().resolveAttribute(R.attr.MediumColor, typedValue2, true);
        map.put("MediumColor",ContextCompat.getColor(this, typedValue2.resourceId));

        return map;
    }
}