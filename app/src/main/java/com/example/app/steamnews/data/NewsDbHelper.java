package com.example.app.steamnews.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.app.steamnews.data.NewsContract.*;
/**
 * Manages a local database for news data.
 */
public class NewsDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "news.db";

    public NewsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_NEWS_TABLE = "CREATE TABLE " + NewsEntry.TABLE_NAME + " (" +

                NewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                NewsEntry.COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                NewsEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                NewsEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                NewsEntry.COLUMN_CONTENTS + " TEXT NOT NULL," +
                NewsEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                NewsEntry.COLUMN_FEED_LABEL + " TEXT NOT NULL " +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_NEWS_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NewsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}