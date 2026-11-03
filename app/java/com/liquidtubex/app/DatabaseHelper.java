package com.liquidtubex.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * On-device storage for saved videos, history, likes, playlists, search history
 * and settings. Nothing here leaves the device.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "liquidtubex.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE videos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "video_id TEXT UNIQUE," +
                "title TEXT," +
                "url TEXT," +
                "thumbnail TEXT," +
                "channel TEXT," +
                "duration TEXT," +
                "added_at INTEGER)");

        db.execSQL("CREATE TABLE history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "video_id TEXT," +
                "title TEXT," +
                "url TEXT," +
                "thumbnail TEXT," +
                "watched_at INTEGER)");

        db.execSQL("CREATE TABLE likes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "video_id TEXT UNIQUE," +
                "title TEXT," +
                "url TEXT," +
                "thumbnail TEXT)");

        db.execSQL("CREATE TABLE playlists (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "created_at INTEGER)");

        db.execSQL("CREATE TABLE playlist_videos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "playlist_id INTEGER," +
                "video_id TEXT," +
                "title TEXT," +
                "url TEXT," +
                "thumbnail TEXT)");

        db.execSQL("CREATE TABLE search_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "query TEXT," +
                "searched_at INTEGER)");

        db.execSQL("CREATE TABLE settings (" +
                "setting_key TEXT PRIMARY KEY," +
                "setting_value TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /** Save a video. */
    public void saveVideo(String id, String title, String url, String thumbnail) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("video_id", id);
        values.put("title", title);
        values.put("url", url);
        values.put("thumbnail", thumbnail);
        values.put("added_at", System.currentTimeMillis());

        db.insertWithOnConflict("videos", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /** Add to history. */
    public void addHistory(String id, String title, String url, String thumbnail) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("video_id", id);
        values.put("title", title);
        values.put("url", url);
        values.put("thumbnail", thumbnail);
        values.put("watched_at", System.currentTimeMillis());

        db.insert("history", null, values);
        db.close();
    }

    /** Like a video. */
    public void likeVideo(String id, String title, String url, String thumbnail) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("video_id", id);
        values.put("title", title);
        values.put("url", url);
        values.put("thumbnail", thumbnail);

        db.insertWithOnConflict("likes", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /** Remove a like. */
    public void unlikeVideo(String id) {

        SQLiteDatabase db = getWritableDatabase();
        db.delete("likes", "video_id=?", new String[]{id});
        db.close();
    }

    /** Remember a search. */
    public void saveSearch(String query) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("query", query);
        values.put("searched_at", System.currentTimeMillis());

        db.insert("search_history", null, values);
        db.close();
    }

    /** Create a playlist and return its id. */
    public long createPlaylist(String name) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("created_at", System.currentTimeMillis());

        long id = db.insert("playlists", null, values);
        db.close();

        return id;
    }

    /** Save a setting. */
    public void saveSetting(String key, String value) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("setting_key", key);
        values.put("setting_value", value);

        db.insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /** Read a setting, or null when it was never written. */
    public String getSetting(String key) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "settings",
                new String[]{"setting_value"},
                "setting_key=?",
                new String[]{key},
                null, null, null
        );

        String value = null;

        if (cursor.moveToFirst()) {
            value = cursor.getString(0);
        }

        cursor.close();
        db.close();

        return value;
    }
}
