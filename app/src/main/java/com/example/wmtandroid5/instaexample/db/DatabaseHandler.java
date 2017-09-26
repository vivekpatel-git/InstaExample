package com.example.wmtandroid5.instaexample.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.wmtandroid5.instaexample.bean.InstaBean;
import com.example.wmtandroid5.instaexample.utils.ConstFun;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by techflitter on 26/9/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    public static DatabaseHandler inst;

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "InstaManager";
    //Tables Name
    public static final String TABLE_USERMEDIA = "'usermedia'";
    public static final String TABLE_USERMEDIAWITHLOCATION = "'usermedialocation'";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHandler getInstance(Context context) {
        if (inst == null) {
            inst = new DatabaseHandler(context);
        }
        return inst;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERMDIA_TABLE = "CREATE TABLE " + TABLE_USERMEDIA + "("
                + DataBaseConstant.KEY_ID + " INTEGER PRIMARY KEY," + DataBaseConstant.KEY_IMAGEURL + " INTEGER," + DataBaseConstant.KEY_UPDATEDAT + " TEXT" + ")";
        String CREATE_USERMDIAWITHLOCATION_TABLE = "CREATE TABLE " + TABLE_USERMEDIAWITHLOCATION + "("
                + DataBaseConstant.KEY_ID + " INTEGER PRIMARY KEY," + DataBaseConstant.KEY_IMAGEURL + " INTEGER," + DataBaseConstant.KEY_LATITUDE + " TEXT," + DataBaseConstant.KEY_LONGITUED + " TEXT," + DataBaseConstant.KEY_UPDATEDAT + " TEXT" + ")";

        db.execSQL(CREATE_USERMDIA_TABLE);
        db.execSQL(CREATE_USERMDIAWITHLOCATION_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERMEDIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERMEDIAWITHLOCATION);

        // Create tables again
        onCreate(db);
    }

    // Adding new userMedia
    public void addUserMediaList(List<String> userMediaList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransactionNonExclusive();
        for (String imageUrl : userMediaList) {
            ContentValues values = new ContentValues();
            values.put(DataBaseConstant.KEY_IMAGEURL, imageUrl);
            values.put(DataBaseConstant.KEY_UPDATEDAT, ConstFun.getCurrentDateAndTime());

            // Inserting Row
            db.insert(TABLE_USERMEDIA, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close(); // Closing database connection
    }

    // Adding new userMediaWithLocation
    public void addUserMediaWithLocationList(List<InstaBean> userMediaList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransactionNonExclusive();
        for (InstaBean instaBean : userMediaList) {
            ContentValues values = new ContentValues();
            values.put(DataBaseConstant.KEY_IMAGEURL, instaBean.getUrl());
            values.put(DataBaseConstant.KEY_LATITUDE, instaBean.getLatituted());
            values.put(DataBaseConstant.KEY_LONGITUED, instaBean.getLongituted());
            values.put(DataBaseConstant.KEY_UPDATEDAT, ConstFun.getCurrentDateAndTime());

            // Inserting Row
            db.insert(TABLE_USERMEDIAWITHLOCATION, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close(); // Closing database connection
    }

    // Getting All UserMediaList
    public ArrayList<String> getAllUserMediaList() {
        ArrayList<String> dbOrdersList = new ArrayList<String>();

        String selectQuery = "SELECT  * FROM " + TABLE_USERMEDIA;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                dbOrdersList.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        return dbOrdersList;
    }

    // Getting All UserMediaLocationList
    public ArrayList<InstaBean> getAllUserMediaLocationList() {
        ArrayList<InstaBean> dbOrdersList = new ArrayList<InstaBean>();

        String selectQuery = "SELECT  * FROM " + TABLE_USERMEDIAWITHLOCATION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                InstaBean instaBean = new InstaBean();
                instaBean.setUrl(cursor.getString(1));
                instaBean.setLatituted(Double.parseDouble(cursor.getString(2)));
                instaBean.setLongituted(Double.parseDouble(cursor.getString(3)));
                dbOrdersList.add(instaBean);
            } while (cursor.moveToNext());
        }

        return dbOrdersList;
    }

    public void deleteUserMeidaAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from" + TABLE_USERMEDIA);
        db.close();
    }

    public void deleteUserMeidaLocationAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from" + TABLE_USERMEDIAWITHLOCATION);
        db.close();
    }

    public static class DataBaseConstant {
        public static final String KEY_ID = "id";
        public static final String KEY_IMAGEURL = "image_url";
        public static final String KEY_LATITUDE = "latitude";
        public static final String KEY_LONGITUED = "longitude";
        public static final String KEY_UPDATEDAT = "updated_at";
    }
}
