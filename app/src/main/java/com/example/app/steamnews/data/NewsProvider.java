package com.example.app.steamnews.data;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.app.steamnews.data.NewsContract.NewsEntry;

public class NewsProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private NewsDbHelper mOpenHelper;

    static final int NEWS = 100;
    static final int NEWS_WITH_GAME_ID = 101;
    static final int NEWS_WITH_GAME_ID_AND_DATE = 102;

    //static strings for the selections
    private static final String sGameIdSelection =
            NewsEntry.TABLE_NAME + "." +
            NewsEntry.COLUMN_GAME_ID + " = ? ";

    private static final String sGameIdSelectionWithStartDateSelection =
            NewsEntry.TABLE_NAME + "." +
            NewsEntry.COLUMN_GAME_ID + " = ? AND " +
            NewsEntry.COLUMN_DATE + " >= ? ";

    private static final String sGameIdAndDaySelection =
            NewsEntry.TABLE_NAME + "." +
            NewsEntry.COLUMN_GAME_ID + " = ? AND " +
            NewsEntry.COLUMN_DATE + " = ? ";


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NewsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, NewsContract.PATH_NEWS, NEWS);
        matcher.addURI(authority, NewsContract.PATH_NEWS + "/*", NEWS_WITH_GAME_ID);
        matcher.addURI(authority, NewsContract.PATH_NEWS + "/*/*", NEWS_WITH_GAME_ID_AND_DATE);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new NewsDbHelper(getContext());
        return true;
    }


    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case NEWS:
                return NewsEntry.CONTENT_TYPE;
            case NEWS_WITH_GAME_ID_AND_DATE:
                return NewsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "news/*"
            case NEWS_WITH_GAME_ID_AND_DATE:
            {
                retCursor = getNewsByGameIdAndDate(uri, projection, sortOrder);
                break;
            }
            case NEWS_WITH_GAME_ID:
            {
                retCursor = getNewsByGameId(uri, projection, sortOrder);
                break;
            }
            case NEWS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.NewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getNewsByGameId(Uri uri, String[] projection, String sortOrder) {
        String GameID = NewsContract.NewsEntry.getGameIDFromUri(uri);
        String startDate = NewsContract.NewsEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sGameIdSelection;
            selectionArgs = new String[]{GameID};
        } else {
            selectionArgs = new String[]{GameID, startDate};
            selection = sGameIdSelectionWithStartDateSelection;
        }

        return mOpenHelper.getReadableDatabase().query(
                NewsContract.NewsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getNewsByGameIdAndDate(Uri uri, String[] projection, String sortOrder) {
        String GameID = NewsContract.NewsEntry.getGameIDFromUri(uri);
        String date = NewsContract.NewsEntry.getDateFromUri(uri);
        return mOpenHelper.getReadableDatabase().query(
                NewsContract.NewsEntry.TABLE_NAME,
                projection,
                sGameIdAndDaySelection,
                new String[]{GameID, date},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case NEWS: {
                normalizeDate(values);
                long _id = db.insert(NewsContract.NewsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = NewsContract.NewsEntry.buildNewsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case NEWS:
                rowsDeleted = db.delete(NewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(NewsContract.NewsEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(NewsContract.NewsEntry.COLUMN_DATE);
            values.put(NewsContract.NewsEntry.COLUMN_DATE, NewsContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);
            int rowsUpdated;

            switch (match) {
                case NEWS:
                    rowsUpdated = db.update(NewsEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NEWS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(NewsContract.NewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}