package com.yuvraj.filecryptnative;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class database extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="database.db";
    private static final String table_name="vault_table",id="ID",name="VAULT_NAME";
    private Context context;

    public database(@Nullable Context context)//ok check
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        this.context=context;

        SQLiteDatabase db = getReadableDatabase();
        String table_create_query="CREATE TABLE IF NOT EXISTS "+table_name+"("+id+" INTEGER PRIMARY KEY AUTOINCREMENT, "+name+" TEXT);";
        db.execSQL(table_create_query);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String table_create_query="CREATE TABLE IF NOT EXISTS "+table_name+"("+id+" INTEGER PRIMARY KEY AUTOINCREMENT, "+name+" TEXT);";
        db.execSQL(table_create_query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+table_name);
        onCreate(db);
    }

    public void add_vault(String vault_name)//ok check
    {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(name,vault_name);
        db.insert(table_name,null,contentValues);
        db.close();
    }

    public void delete_vault(int vault_id)
    {
        SQLiteDatabase db = getReadableDatabase();
        db.delete(table_name,id+" = '"+vault_id+"'",null);
        db.close();
    }

    @SuppressLint("Range")
    public ArrayList<vault_info> get_all_data()//ok check
    {
        ArrayList<vault_info> vault_info_list=new ArrayList();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c=db.query(table_name,null,null,null,null,null,null);
        c.moveToFirst();
        while(!c.isAfterLast())
        {
            vault_info data=new vault_info();
            data.id=c.getInt(c.getColumnIndex(id));
            data.vault_name=c.getString(c.getColumnIndex(name));
            vault_info_list.add(data);
            c.moveToNext();
        }
        c.close();
        db.close();
        return vault_info_list;
    }

    @SuppressLint("Range")
    public vault_info get_last_entered_data()
    {
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + table_name;
        Cursor c = db.rawQuery(selectQuery,null);
        vault_info vaultInfo=new vault_info();
        c.moveToLast();
        vaultInfo.id=c.getInt(c.getColumnIndex(id));
        vaultInfo.vault_name=c.getString(c.getColumnIndex(name));

        return vaultInfo;
    }

    public boolean is_vault_name_present(String vault_name)
    {
        SQLiteDatabase db = getReadableDatabase();
        String query="SELECT * FROM "+table_name+" WHERE "+name+"='"+vault_name+"'";
        Cursor c = db.rawQuery(query,null);
        boolean present=false;
        c.moveToFirst();
        while(!c.isAfterLast())
        {   present=true;break;}
        db.close();
        return present;
    }
}

class vault_info
{
    int id;
    String vault_name;
}
