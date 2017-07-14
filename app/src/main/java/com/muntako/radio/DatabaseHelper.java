package com.muntako.radio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.muntako.radio.model.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ADMIN on 26-May-17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DATABASE_NAME = "database";
    private static int DATABASE_VERSION = 1;
    private static String TABLE_NAME = "radios";
    private static String ID = "id";
    private static String NAME = "name";
    private static String SITE_ADD = "site_add";
    private static String URL_STREAM_STEREO = "url_stream_stereo";
    private static String URL_STREM_MONO = "url_stream_mono";
    private static String STAT_STEREO = "stat_stereo";
    private static String STAT_MONO = "stat_mono";
    private static String PATH_LOGO = "pathlogo";
    private static String KOTA = "kota";
    private static String FREKUENSI = "frekuensi";
    private static String BAND = "band";
    private static String KATEGORI = "kategori";
    private static String KODE_PROVINSI = "kode_provinsi";
    private static String NAMA_PROVINSI = "nama_provinsi";
    private static String FAVORITE = "favorited";


    private static String createTableApp = "create table " + TABLE_NAME + " ("
            + ID + " INTEGER, "
            + NAME + " text,"
            + SITE_ADD + " text,"
            + URL_STREAM_STEREO + " text,"
            + URL_STREM_MONO + " text,"
            + STAT_MONO + " INTEGER,"
            + STAT_STEREO + " INTEGER,"
            + PATH_LOGO + " text,"
            + KOTA + " text,"
            + FREKUENSI + " text,"
            + BAND + " text,"
            + KATEGORI + " text,"
            + KODE_PROVINSI + " text,"
            + NAMA_PROVINSI + " text,"
            + FAVORITE + " INTEGER);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableApp);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void CreateRadio(List<Channel> channels) {
        for (Channel c : channels) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID, c.getId());
            contentValues.put(NAME, c.getName());
            contentValues.put(SITE_ADD, c.getSiteAdd());
            contentValues.put(URL_STREAM_STEREO, c.getUrlStreamStereo());
            contentValues.put(URL_STREM_MONO, c.getUrlStreamMono());
            contentValues.put(STAT_MONO, c.getStatMono());
            contentValues.put(STAT_STEREO, c.getStatStereo());
            contentValues.put(PATH_LOGO, c.getPathlogo());
            contentValues.put(KOTA, c.getKota());
            contentValues.put(BAND, c.getBand());
            contentValues.put(FREKUENSI, c.getFrekuensi());
            contentValues.put(KATEGORI, c.getKategori());
            contentValues.put(KODE_PROVINSI, c.getKodepropinsi());
            contentValues.put(NAMA_PROVINSI, c.getNamapropinsi());
            contentValues.put(FAVORITE, c.getFavorited());
            db.insert(TABLE_NAME, null, contentValues);
        }
    }

    public List<Channel> getAllChannel() {
        List<Channel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_NAME + " WHERE " + FAVORITE + " = 0", null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Channel ch = new Channel();
                ch.setId(c.getInt(c.getColumnIndex(ID)));
                ch.setName(c.getString(c.getColumnIndex(NAME)));
                ch.setSiteAdd(c.getString(c.getColumnIndex(SITE_ADD)));
                ch.setUrlStreamStereo(c.getString(c.getColumnIndex(URL_STREAM_STEREO)));
                ch.setUrlStreamMono(c.getString(c.getColumnIndex(URL_STREM_MONO)));
                ch.setStatMono(c.getInt(c.getColumnIndex(STAT_MONO)));
                ch.setStatStereo(c.getInt(c.getColumnIndex(STAT_STEREO)));
                ch.setPathlogo(c.getString(c.getColumnIndex(PATH_LOGO)));
                ch.setKota(c.getString(c.getColumnIndex(KOTA)));
                ch.setFrekuensi(c.getString(c.getColumnIndex(FREKUENSI)));
                ch.setBand(c.getString(c.getColumnIndex(BAND)));
                ch.setKategori(c.getString(c.getColumnIndex(KATEGORI)));
                ch.setKodepropinsi(c.getColumnName(c.getColumnIndex(KODE_PROVINSI)));
                ch.setNamapropinsi(c.getString(c.getColumnIndex(NAMA_PROVINSI)));
                ch.setFavorited(c.getInt(c.getColumnIndex(FAVORITE)));
                list.add(ch);
            } while (c.moveToNext());
        }
        return list;
    }

    public List<Channel> getFavChannel() {
        List<Channel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_NAME + " WHERE " + FAVORITE + " = 2", null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Channel ch = new Channel();
                ch.setId(c.getInt(c.getColumnIndex(ID)));
                ch.setName(c.getString(c.getColumnIndex(NAME)));
                ch.setSiteAdd(c.getString(c.getColumnIndex(SITE_ADD)));
                ch.setUrlStreamStereo(c.getString(c.getColumnIndex(URL_STREAM_STEREO)));
                ch.setUrlStreamMono(c.getString(c.getColumnIndex(URL_STREM_MONO)));
                ch.setStatMono(c.getInt(c.getColumnIndex(STAT_MONO)));
                ch.setStatStereo(c.getInt(c.getColumnIndex(STAT_STEREO)));
                ch.setPathlogo(c.getString(c.getColumnIndex(PATH_LOGO)));
                ch.setKota(c.getString(c.getColumnIndex(KOTA)));
                ch.setFrekuensi(c.getString(c.getColumnIndex(FREKUENSI)));
                ch.setBand(c.getString(c.getColumnIndex(BAND)));
                ch.setKategori(c.getString(c.getColumnIndex(KATEGORI)));
                ch.setKodepropinsi(c.getColumnName(c.getColumnIndex(KODE_PROVINSI)));
                ch.setNamapropinsi(c.getString(c.getColumnIndex(NAMA_PROVINSI)));
                ch.setFavorited(c.getInt(c.getColumnIndex(FAVORITE)));
                list.add(ch);
            } while (c.moveToNext());
        }
        return list;
    }

    public boolean updateChannel(int id, boolean favorite) {
        int fav = 1;
        if (favorite) {
            fav = 2;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("UPDATE "+ TABLE_NAME +" SET " + FAVORITE + " = "+fav
                +" WHERE "+ID + " = " + id);
        return true;
    }

    public boolean deleteChannel(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE from "+TABLE_NAME+" WHERE "+ID + " = " + id);
        return true;
    }

}