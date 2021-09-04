package com.yuvraj.filecryptnative;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
    dialog_theme.theme_dialog_listener,
    dialog_create_vault.dialog_create_vault_listener,
    vault_fragment.Vault_Fragment_Listener,
    dialog_open_vault.dialog_open_vault_listener,
    explore_vault_fragment.Vault_Explore_Fragment_Listener,
    dialog_rename.rename_dialog_listener{

    //fragment
    vault_fragment vaultFragment;
    explore_vault_fragment exploreVaultFragment;
    //dialogs
    private dialog_theme themeDialog;
    private dialog_create_vault createVaultDialog;
    private dialog_open_vault openVaultDialog;
    private dialog_rename renameDialog;
    private dialog_file_details fileDetailsDialog;
    //appbar
    private View search_appbar;
    private EditText search_edit_text;
    private View options_appbar;
    private CheckBox select_all_item;
    private TextView selected_item_count;
    private ImageButton rename_button,delete_button,move_out_button,about_button;
    private ActionBar actionBar;
    //tabs
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private fragment_adapter fragmentAdapter;
    private int tab_id=0;
    //fab animations
    private Animation toBottom,fromBottom,rotateOpen,rotateClose;
    private boolean fab_clicked=false;
    FloatingActionButton fab,add_file_fab,lock_vault_fab;
    TextView lock_vault_textview,add_file_textview;
    //preference vars
    private SharedPreferences settings_reader;
    private SharedPreferences.Editor settings_editor;
    //other vars
    protected ArrayList<vault_info> vault_info_list;
    protected ArrayList<vault_info> vault_info_list_temp=new ArrayList<>();
    protected ArrayList<explore_vault_fragment.vault_data> vault_data_list;
    protected ArrayList<explore_vault_fragment.vault_data> vault_data_list_temp;
    private database db;
    private AES aes_handler;
    private String password,vault_path="";
    FileCryptNativeApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings_reader = getSharedPreferences("settings", Context.MODE_PRIVATE);
        set_theme(settings_reader.getInt("color_scheme_code", 3));
        save_theme_preference(settings_reader.getInt("color_scheme_code",3));
        setContentView(R.layout.activity_main);

        db=new database(this);
        aes_handler=new AES();
        vault_info_list=db.get_all_data();

        //initializing search appbar
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        search_appbar = inflator.inflate(R.layout.search_appbar, null);
        options_appbar = inflator.inflate(R.layout.options_appbar, null);
        initialize_search_appbar();

        //initializing tabs
        viewPager2 = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentAdapter = new fragment_adapter(fragmentManager,getLifecycle());
        viewPager2.setAdapter(fragmentAdapter);
        tabLayout.addTab(tabLayout.newTab().setText("Vaults"));
        tabLayout.addTab(tabLayout.newTab().setText("Vault Explorer"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0)
                {
                    if(selected_item_index_list.size()>0)
                    {   initialize_search_appbar();}
                    if(fab_clicked)
                    {   onFabButtonClicked();}
                }
                else if(tab.getPosition()==1)
                {
                    if(selected_item_index_list.size()>0) {
                        initialize_option_app_bar();
                        selected_item_count.setText(Integer.toString(selected_item_index_list.size()));
                    }
                }
                tab_id=tab.getPosition();
                viewPager2.setCurrentItem(tab.getPosition());
                if(tab_id==0)
                {   search_edit_text.setText(search_vault);}
                else if(tab_id==1)
                {
                    if(selected_item_index_list.size()==0)
                    {   search_edit_text.setText(search_file);}
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        //initializing fab & animation
        rotateOpen = AnimationUtils.loadAnimation(this,R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this,R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this,R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this,R.anim.to_bottom_anim);
        fab=findViewById(R.id.fab);
        add_file_fab=findViewById(R.id.add_file_fab);
        add_file_fab.setOnClickListener(view -> {
            if(selected_item_index_list.size()==0) {
                Intent add_file_intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                add_file_intent.setType("*/*");
                add_file_intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                add_file_saf.launch(add_file_intent);
            }
            else
            {   Toast.makeText(this, "Unselect all files first", Toast.LENGTH_SHORT).show();}
        });
        add_file_textview=findViewById(R.id.add_files_textView);
        lock_vault_fab=findViewById(R.id.lock_vault_fab);
        lock_vault_fab.setOnClickListener(view -> {
            lock_vault();
        });
        lock_vault_textview=findViewById(R.id.lock_vault_textView);
        fab.setOnClickListener(view -> {
            onFabButtonClicked();
        });

        application=(FileCryptNativeApplication) getApplicationContext();
        vault_data_list=application.vault_data_list;
        vault_data_list_temp=application.vault_data_list_temp;
        password=application.password;
        System.out.println("temp_size=== "+vault_data_list_temp.size());
        System.out.println("pass=== "+password);
    }
    /*--------------------------------------------------------------------Option Menu----------------------------------------------------------------------------------------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.context_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.theme_item)
        {
            themeDialog=new dialog_theme();
            themeDialog.show(getSupportFragmentManager(),"theme_dialog");
        }
        else if(id == R.id.about_item)
        {   System.out.println("about!!!");}
        return super.onOptionsItemSelected(item);
    }

    /*-------------------------------------------------------------------Theme Dialog----------------------------------------------------------------------------------------*/

    @Override
    public void theme_selected(int theme_code)
    {
        save_theme_preference(theme_code);
        recreate();
    }
    @Override
    public int get_theme_code()
    {   return settings_reader.getInt("color_scheme_code", 3);}

    private void save_theme_preference(int theme_code)
    {
        settings_reader = getSharedPreferences("settings", Context.MODE_PRIVATE);
        settings_editor = getSharedPreferences("settings",Context.MODE_PRIVATE).edit();
        settings_editor.putInt("color_scheme_code", theme_code);
        settings_editor.apply();
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

    /*-------------------------------------------------------------------create vault dialog --------------------------------------------------------------------------------*/

    @Override
    public void create_vault(String vault_name, String password) {
        if(!db.is_vault_name_present(vault_name)) {
            db.add_vault(vault_name);
            vault_info vaultInfo = db.get_last_entered_data();
            vaultFragment = (vault_fragment) getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
            vaultFragment.add_data(vaultInfo);
            //create folder and pass_check_file
            String appPath = this.getApplicationContext().getFilesDir().getAbsolutePath();
            appPath = appPath + "/" + vault_name;
            File file = new File(appPath);
            file.mkdir();
            appPath = appPath + "/pass_check";
            File passCheckFile = new File(appPath);
            try {
                passCheckFile.createNewFile();
                FileWriter passCheckFileWriter = new FileWriter(appPath);
                passCheckFileWriter.write(aes_handler.encrypt(password, password));
                passCheckFileWriter.close();
            } catch (Exception e) { }
            createVaultDialog.dismiss();
        }
        else
        {   Toast.makeText(this, "Vault named '"+vault_name+"' already present", Toast.LENGTH_SHORT).show();}
    }

    /*-------------------------------------------------------------------Open vault dialog functions--------------------------------------------------------------------------------*/

    @Override
    public void open_vault(String vault_name, String password) {
        String appPath = this.getApplicationContext().getFilesDir().getAbsolutePath();
        String pass_check_path=appPath+"/"+vault_name+"/pass_check";
        try{
            BufferedReader br = new BufferedReader(new FileReader(pass_check_path));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String encrypted_text = sb.toString();
            br.close();
            AES.aes_data decrypted_data=aes_handler.decrypt(encrypted_text,password);
            if(decrypted_data.get_decryption_success_status())
            {
                this.password=password;
                application.password=password;
                openVaultDialog.dismiss();
                this.vault_path=appPath+"/"+vault_name;
                load_vault();
            }
            else
            {   Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();}
        }
        catch(Exception e)
        {   e.printStackTrace();}
    }
    @Override
    public void open_vault_dialog(int vault_id, String vault_name) {
        if(password.length()==0) {
            openVaultDialog = new dialog_open_vault(vault_name);
            openVaultDialog.show(getSupportFragmentManager(), "open_vault_dialog");
        }
        else
        {    Toast.makeText(this, "Close already open vault first", Toast.LENGTH_SHORT).show();}
    }

    /*-------------------------------------------------------------------FAB Button functions--------------------------------------------------------------------------------*/

    private void lock_vault()
    {
        fab_animation();

        application.password="";
        password="";
        vault_path="";
        int end=vault_data_list.size();
        vault_data_list.clear();
        vault_data_list_temp.clear();
        exploreVaultFragment= (explore_vault_fragment) getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
        exploreVaultFragment.set_vault_status();
        exploreVaultFragment.select_all_item(false);
        exploreVaultFragment.notify_range_deleted(0,end);
        selected_item_index_list.clear();
        all_item_selected=false;
        initialize_search_appbar();
    }

    private ActivityResultLauncher<Intent> add_file_saf = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                ClipData clipDate=data.getClipData();
                ArrayList<Uri> fileUriList=new ArrayList();
                if(clipDate==null)
                {   fileUriList.add(data.getData());}
                else
                {
                    for(int a=0;a<clipDate.getItemCount();a++)
                    {   fileUriList.add(clipDate.getItemAt(a).getUri());}
                }
                onFabButtonClicked();
                add_files(fileUriList);
            }
        }
    );

    public boolean is_vault_open()
    {
        if(password.length()>0)
        {   return true;}
        else
        {   return false;}
    }

    private void onFabButtonClicked()
    {
        if(tab_id==0)
        {
            createVaultDialog=new dialog_create_vault();
            createVaultDialog.show(getSupportFragmentManager(),"create_vault_dialog");
        }
        else if(tab_id==1)
        {
            if(password.length()>0)
            {   fab_animation();}
            else
            {   Toast.makeText(this, "Open a vault first", Toast.LENGTH_SHORT).show();}
        }
    }

    private void fab_animation()
    {
        if(!fab_clicked)
        {
            add_file_fab.setVisibility(View.VISIBLE);
            add_file_textview.setVisibility(View.VISIBLE);
            lock_vault_fab.setVisibility(View.VISIBLE);
            lock_vault_textview.setVisibility(View.VISIBLE);

            add_file_fab.startAnimation(fromBottom);
            add_file_textview.startAnimation(fromBottom);
            lock_vault_fab.startAnimation(fromBottom);
            lock_vault_textview.startAnimation(fromBottom);
            fab.startAnimation(rotateOpen);

            lock_vault_fab.setClickable(true);
            add_file_fab.setClickable(true);
        }
        else
        {
            add_file_fab.setVisibility(View.INVISIBLE);
            add_file_textview.setVisibility(View.INVISIBLE);
            lock_vault_fab.setVisibility(View.INVISIBLE);
            lock_vault_textview.setVisibility(View.INVISIBLE);

            add_file_fab.startAnimation(toBottom);
            add_file_textview.startAnimation(toBottom);
            lock_vault_fab.startAnimation(toBottom);
            lock_vault_textview.startAnimation(toBottom);
            fab.startAnimation(rotateClose);

            lock_vault_fab.setClickable(false);
            add_file_fab.setClickable(false);
        }
        fab_clicked = !fab_clicked;
    }

    /*-------------------------------------------------------------------Search appbar functions---------------------------------------------------------------------------------*/
    private String search_file="",search_vault="";
    private short delay = 800;
    private long last_text_edit = 0;
    private Handler handler = new Handler();
    private Runnable input_finish_checker = () -> {
        if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
            search();
        }
    };
    private void search()
    {
        if(viewPager2.getCurrentItem()==0)
        {
            vault_info_list.addAll(vault_info_list_temp);
            vault_info_list_temp.clear();
            if(search_vault.length()==0)
            {
                vaultFragment=(vault_fragment)getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
                vaultFragment.notify_change();
            }
            else
            {
                for (int a = vault_info_list.size() - 1; a >= 0; a--)
                {
                    if (!vault_info_list.get(a).vault_name.toLowerCase().contains(search_vault))
                    {
                        vault_info_list_temp.add(vault_info_list.get(a));
                        vault_info_list.remove(a);
                    }
                }
                vaultFragment=(vault_fragment)getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
                vaultFragment.notify_change();
            }
        }
        else if(viewPager2.getCurrentItem()==1 && password.length()>0)
        {
            vault_data_list.addAll(vault_data_list_temp);
            vault_data_list_temp.clear();
            if(search_file.length()==0)
            {
                exploreVaultFragment=(explore_vault_fragment)getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
                exploreVaultFragment.notify_change();
            }
            else
            {
                for(int a=vault_data_list.size()-1;a>=0;a--)
                {
                    if(!vault_data_list.get(a).file_name.toLowerCase().contains(search_file))
                    {
                        vault_data_list_temp.add(vault_data_list.get(a));
                        vault_data_list.remove(a);
                    }
                }
                exploreVaultFragment=(explore_vault_fragment)getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
                exploreVaultFragment.notify_change();
            }
        }
    }
    private void initialize_search_appbar()
    {
        actionBar.setCustomView(search_appbar);
        search_edit_text=findViewById(R.id.search_edittext);
        search_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //search_text=charSequence.toString();
                if(tab_id==0)
                {   search_vault=charSequence.toString();}
                else if(tab_id==1)
                {   search_file=charSequence.toString();}
                handler.removeCallbacks(input_finish_checker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                last_text_edit = System.currentTimeMillis();
                handler.postDelayed(input_finish_checker, delay);
            }
        });
    }
    /*-------------------------------------------------------------------Item selection functions--------------------------------------------------------------------------------*/

    private ArrayList<Integer> selected_item_index_list=new ArrayList<>();
    private boolean all_item_selected=false;
    @Override
    public void select_item(int item_id, int index) {
        if(selected_item_index_list.size()==0)
        {   initialize_option_app_bar();}
        else
        {
            selected_item_count.setText(Integer.toString(selected_item_index_list.size()+1));
            if(selected_item_index_list.size()==1)
            {
                about_button.setVisibility(View.GONE);
                rename_button.setVisibility(View.GONE);
            }
        }
        selected_item_index_list.add(index);
    }
    @Override
    public void unselect_item(int item_id, int index) {
        for(int a=0;a<selected_item_index_list.size();a++)
        {
            if(selected_item_index_list.get(a)==index)
            {   selected_item_index_list.remove(a);break;}
        }
        if(selected_item_index_list.size()==0)
        {   initialize_search_appbar();}
        else
        {
            if(selected_item_index_list.size()==1)
            {
                about_button.setVisibility(View.VISIBLE);
                rename_button.setVisibility(View.VISIBLE);
            }
            selected_item_count.setText(Integer.toString(selected_item_index_list.size()));
        }
    }
    private void initialize_option_app_bar()
    {
        actionBar.setCustomView(options_appbar);
        selected_item_count=findViewById(R.id.selected_item_count);
        selected_item_count.setText("1");
        select_all_item=findViewById(R.id.select_all_checkbox);
        select_all_item.setOnClickListener(view -> {
            if(!all_item_selected)
            {
                selected_item_index_list.clear();
                for(int a=0;a<vault_data_list.size();a++)
                {
                    selected_item_index_list.add(a);
                    vault_data_list.get(a).selected=true;
                }
                about_button.setVisibility(View.GONE);
                rename_button.setVisibility(View.GONE);
                all_item_selected=true;
                selected_item_count.setText(Integer.toString(vault_data_list.size()));
                exploreVaultFragment= (explore_vault_fragment) getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
                exploreVaultFragment.select_all_item(true);
                exploreVaultFragment.notify_change();
            }
            else
            {
                selected_item_index_list.clear();
                for(int a=0;a<vault_data_list.size();a++)
                {   vault_data_list.get(a).selected=false;}
                about_button.setVisibility(View.VISIBLE);
                rename_button.setVisibility(View.VISIBLE);
                all_item_selected=false;
                selected_item_count.setText("1");
                exploreVaultFragment= (explore_vault_fragment) getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
                exploreVaultFragment.select_all_item(false);
                exploreVaultFragment.notify_change();
                selected_item_count.setText(Integer.toString(1));
                initialize_search_appbar();
            }
        });
        rename_button=findViewById(R.id.rename_button);
        rename_button.setOnClickListener(view -> {
            renameDialog = new dialog_rename(selected_item_index_list.get(0),vault_data_list.get(selected_item_index_list.get(0)).file_name);
            renameDialog.show(getSupportFragmentManager(),"rename_dialog");
        });
        delete_button=findViewById(R.id.delete_button);
        delete_button.setOnClickListener(view -> {
            Map<String,Integer> map=get_color_id();
            String deep_color= String.format("#%06X", (0xFFFFFF & map.get("DeepColor")));
            String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
            map.clear();
            final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
            materialAlertDialogBuilder.setTitle(Html.fromHtml("<font color="+medium_color+">Delete Files</font>"));
            materialAlertDialogBuilder.setMessage(Html.fromHtml("<font color="+deep_color+">Do you want to delete "+selected_item_index_list.size()+" file ?</font>"));
            materialAlertDialogBuilder.setBackground(this.getDrawable(R.drawable.grey_background));
            materialAlertDialogBuilder.setPositiveButton(Html.fromHtml("<font color="+medium_color+">Yes</font>"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    delete_file();
                }
            });
            materialAlertDialogBuilder.setNegativeButton(Html.fromHtml("<font color="+medium_color+">No</font>"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            materialAlertDialogBuilder.show();
        });
        about_button=findViewById(R.id.file_about_button);
        about_button.setOnClickListener(view -> {
            File file=new File(vault_data_list.get(selected_item_index_list.get(0)).encrypted_file_path);
            Date date=new Date(file.lastModified());
            Double size_raw=(file.length()/(1048576.0));
            Double truncatedDouble = BigDecimal.valueOf(size_raw).setScale(3, RoundingMode.HALF_UP).doubleValue();
            String size=truncatedDouble+" MB";
            fileDetailsDialog = new dialog_file_details(vault_data_list.get(selected_item_index_list.get(0)).file_name,size,date.toString());
            fileDetailsDialog.show(getSupportFragmentManager(),"file_detail_dialog");
        });
        move_out_button=findViewById(R.id.file_move_button);
        move_out_button.setOnClickListener(view -> {
            Intent move_file_out_intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            move_file_out_saf.launch(move_file_out_intent);
        });
    }


    /*-------------------------------------------------------------------Vault file functions------------------------------------------------------------------------------------*/
    private String get_file_from_path(String path)
    {
        String file_name="";
        for(int a=path.length()-1;a>=0;a--)
        {
            if(path.charAt(a)=='/')
            {   break;}
            else
            {   file_name=path.charAt(a)+file_name;}
        }
        return file_name;
    }

    private boolean is_image(String file_name)
    {
        if(file_name.toLowerCase().contains(".jpg")||
           file_name.toLowerCase().contains(".png")||
           file_name.toLowerCase().contains(".bmp")||
           file_name.toLowerCase().contains(".jpeg")||
           file_name.toLowerCase().contains(".gif"))
        {   return true;}
        else
        {   return false;}
    }

    private Drawable get_icon(String file_name)
    {
        int imageResource;
        if(file_name.toLowerCase().contains("pdf"))
        {   imageResource = getResources().getIdentifier("@drawable/icon_pdf", null, getPackageName());}
        else if(file_name.toLowerCase().contains("avi")||
                file_name.toLowerCase().contains("mp4")||
                file_name.toLowerCase().contains("webm")||
                file_name.toLowerCase().contains("mkv"))
        {   imageResource = getResources().getIdentifier("@drawable/icon_video", null, getPackageName());}
        else if(file_name.toLowerCase().contains("mp3")||
                file_name.toLowerCase().contains("mp4a")||
                file_name.toLowerCase().contains("acc"))
        {   imageResource = getResources().getIdentifier("@drawable/icon_audio", null, getPackageName());}
        else
        {   imageResource = getResources().getIdentifier("@drawable/icon_document", null, getPackageName());}

        return getResources().getDrawable(imageResource,getTheme());
    }

    private void load_vault()
    {
        short first_chunk_size=8224;
        short size=8208;
        byte[] first_chunk_buffer=new byte[first_chunk_size];
        byte[] buffer=new byte[size];
        DocumentFile vault_dir=DocumentFile.fromFile(new File(vault_path));
        DocumentFile[] file_list = vault_dir.listFiles();
        for(int a=0;a<file_list.length;a++)
        {
            explore_vault_fragment.vault_data data=new explore_vault_fragment.vault_data();
            if(!file_list[a].getName().equals("pass_check"))
            {
                try{
                    if(vault_data_list.size()==0)
                    {   data.id=0;}
                    else
                    {   data.id=vault_data_list.get(vault_data_list.size()-1).id+1;}
                    data.encrypted_file_name=file_list[a].getName();
                    data.file_name=aes_handler.decrypt(file_list[a].getName(),password).get_decrypted_data();
                    data.encrypted_file_path=vault_path+"/"+data.encrypted_file_name;
                    //get thumbnail
                    if(is_image(data.file_name))
                    {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        InputStream ins=getContentResolver().openInputStream(file_list[a].getUri());
                        BufferedInputStream bins = new BufferedInputStream(ins);
                        //first chunk
                        bins.read(first_chunk_buffer);
                        baos.write(aes_handler.decrypt_bytes(first_chunk_buffer,password));
                        //rest of the data
                        while(bins.read(buffer)!=-1)
                        {   baos.write(aes_handler.decrypt_bytes(buffer,password));}
                        //Bitmap bmp= BitmapFactory.decodeByteArray(baos.toByteArray(), 0,baos.toByteArray().length);
                        data.thumb_nail=Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(baos.toByteArray(), 0,baos.toByteArray().length),80,80,false);
                        baos.close();

                        aes_handler.shutdown_byte_operations();
                        ins.close();
                        bins.close();
                    }
                    else
                    {   data.icon=get_icon(data.file_name);}
                }
                catch(Exception e)
                {   e.printStackTrace();}
                vault_data_list.add(data);
            }
        }
    }

    private boolean is_file_present(String new_file_name)
    {
        boolean is_present=false;
        for(int a=0;a<vault_data_list.size();a++)
        {
            if(vault_data_list.get(a).file_name.compareTo(new_file_name)==0)
            {   is_present=true;break;}
        }
        return is_present;
    }

    private void add_files(ArrayList<Uri> uri_list)
    {
        short size=8192;
        byte[] buffer=new byte[size];
        boolean is_image;
        int start=vault_data_list.size()-1;
        ByteArrayOutputStream baos;
        for(int a=0;a<uri_list.size();a++)
        {
            try {
                DocumentFile documentFile =  DocumentFile.fromSingleUri(this,uri_list.get(a));
                if(!is_file_present(documentFile.getName()))
                {
                    is_image = false;
                    if (is_image(documentFile.getName())) {
                        is_image = true;
                    }
                    //fill up the data
                    explore_vault_fragment.vault_data data = new explore_vault_fragment.vault_data();
                    if (vault_data_list.size() == 0) {
                        data.id = 0;
                    } else {
                        data.id = vault_data_list.get(vault_data_list.size() - 1).id;
                    }
                    data.file_name = documentFile.getName();

                    baos = new ByteArrayOutputStream();
                    InputStream ins = getContentResolver().openInputStream(uri_list.get(a));
                    BufferedInputStream bins = new BufferedInputStream(ins);
                    data.encrypted_file_name = aes_handler.encrypt(documentFile.getName(), password);
                    data.encrypted_file_path = vault_path + "/" + data.encrypted_file_name;
                    FileOutputStream fots=new FileOutputStream(data.encrypted_file_path);
                    BufferedOutputStream bops = new BufferedOutputStream(fots);
                    while (bins.read(buffer) != -1) {
                        if (is_image) {
                            baos.write(buffer);
                        }
                        byte[] encryptedByte = aes_handler.encrypt_bytes(buffer, password);
                        //System.out.println("length="+encryptedByte.length);
                        bops.write(encryptedByte);
                    }
                    if (is_image) {
                        data.thumb_nail = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length), 80, 80, false);
                    } else {
                        data.icon = get_icon(data.file_name);
                    }
                    vault_data_list.add(data);
                    fots.close();
                    baos.close();
                    bops.close();
                    ins.close();
                    bins.close();
                    aes_handler.shutdown_byte_operations();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        exploreVaultFragment= (explore_vault_fragment) getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
        if(start==0)
        {   exploreVaultFragment.set_vault_status();}
        exploreVaultFragment.notify_item_range_inserted(0,vault_data_list.size()-1);

    }

    private void delete_file()
    {
        exploreVaultFragment= (explore_vault_fragment) getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
        for(int a=0;a<selected_item_index_list.size();a++)
        {
            File file=new File(vault_data_list.get(selected_item_index_list.get(a)).encrypted_file_path);
            file.delete();
            int index=selected_item_index_list.get(a);
            vault_data_list.remove(index);
            exploreVaultFragment.notify_item_removed(selected_item_index_list.get(a));
        }
        about_button.setVisibility(View.VISIBLE);
        rename_button.setVisibility(View.VISIBLE);
        initialize_search_appbar();
        exploreVaultFragment.select_all_item(false);
        //exploreVaultFragment.notify_change();
        Toast.makeText(this, selected_item_index_list.size()+" File Deleted", Toast.LENGTH_SHORT).show();
        selected_item_index_list.clear();
        all_item_selected=false;
    }

    private String get_extension(String file_name)
    {
        String ext="";
        for(int a=file_name.length()-1;a>=0;a--)
        {
            if(file_name.charAt(a)=='.')
            {   break;}
            else
            {   ext=file_name.charAt(a)+ext;}
        }
        if(ext.length()==file_name.length())
        {   return "";}
        else
        {   return "."+ext;}
    }

    @Override
    public void rename_file(int file_index,String new_name)
    {
        String ext=get_extension(vault_data_list.get(file_index).file_name);
        vault_data_list.get(file_index).file_name=new_name+ext;
        vault_data_list.get(file_index).encrypted_file_name=aes_handler.encrypt(vault_data_list.get(file_index).file_name,password);
        DocumentFile documentFile=DocumentFile.fromFile(new File(vault_data_list.get(file_index).encrypted_file_path));
        documentFile.renameTo(vault_data_list.get(file_index).encrypted_file_name);
        vault_data_list.get(file_index).encrypted_file_path=vault_path="/"+vault_data_list.get(file_index).encrypted_file_name;
        exploreVaultFragment= (explore_vault_fragment) getSupportFragmentManager().findFragmentByTag("f" + viewPager2.getCurrentItem());
        exploreVaultFragment.select_all_item(false);
        exploreVaultFragment.notify_item_changed(file_index);
        Toast.makeText(this, "File Renamed", Toast.LENGTH_SHORT).show();
    }

    private ActivityResultLauncher<Intent> move_file_out_saf = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    move_file_out(result.getData().getData());
                }
                else
                {   Toast.makeText(this, "Destination not selected", Toast.LENGTH_SHORT).show();}
            }
    );

    private void move_file_out(Uri destination_uri)
    {
        short first_chunk_size=8224;
        short size=8208;
        byte[] first_chunk_buffer=new byte[first_chunk_size];
        byte[] buffer=new byte[size];
        DocumentFile dest=DocumentFile.fromTreeUri(this,destination_uri);
        this.grantUriPermission(getPackageName(), destination_uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        for(int a=0;a<selected_item_index_list.size();a++)
        {
            try {
                FileInputStream fins=new FileInputStream(vault_data_list.get(selected_item_index_list.get(a)).encrypted_file_path);
                BufferedInputStream bins = new BufferedInputStream(fins);
                DocumentFile new_out_file=dest.createFile("",vault_data_list.get(selected_item_index_list.get(a)).file_name);
                OutputStream ops=getContentResolver().openOutputStream(new_out_file.getUri());
                BufferedOutputStream bops = new BufferedOutputStream(ops);
                bins.read(first_chunk_buffer);
                bops.write(aes_handler.decrypt_bytes(first_chunk_buffer,password));
                while (bins.read(buffer) != -1) {
                    byte[] encryptedByte = aes_handler.decrypt_bytes(buffer, password);
                    //System.out.println("length="+encryptedByte.length);
                    bops.write(encryptedByte);
                }
                fins.close();
                bops.close();
                bins.close();
                ops.close();
                aes_handler.shutdown_byte_operations();
            }
            catch(Exception e)
            {   e.printStackTrace();}
        }
        Toast.makeText(this, "Files moved out", Toast.LENGTH_SHORT).show();
        delete_file();
    }

    @Override
    public void view_image(int item_id, int index) {
        if(is_image(vault_data_list.get(index).file_name)) {
            Intent intent = new Intent(this, image_viewer_activity.class);
            intent.putExtra("file_name", vault_data_list.get(index).file_name);
            intent.putExtra("encrypted_file_path", vault_data_list.get(index).encrypted_file_path);
            intent.putExtra("password", password);
            startActivity(intent);
        }
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