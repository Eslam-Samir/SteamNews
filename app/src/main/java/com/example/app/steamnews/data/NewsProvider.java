package com.example.app.steamnews.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.app.steamnews.data.NewsContract.NewsEntry;

import static com.example.app.steamnews.data.NewsProvider.NEWS;

public class NewsProvider extends ContentProvider {
    static final int NEWS = 100;
    static final int NEWS_WITH_GAME_ID = 101;
    static final int NEWS_WITH_GAME_ID_AND_NEWS_ID = 102;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NewsContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, NewsContract.PATH_NEWS, NEWS);
        matcher.addURI(authority, NewsContract.PATH_NEWS + "/#", NEWS_WITH_GAME_ID); //Game ID is an integer not string
        matcher.addURI(authority, NewsContract.PATH_NEWS + "/#/#", NEWS_WITH_GAME_ID_AND_NEWS_ID);
        return matcher;
    }

    //static strings for the selections
    private static final String sGameIdSelection = NewsEntry.TABLE_NAME + "." +
                                                   NewsEntry.COLUMN_GAME_ID + " = ? ";

    private static final String sGameIdWithNewsIdSelection =
            NewsEntry.TABLE_NAME + "." +
                    NewsEntry._ID + " = ? AND " +
                    NewsEntry.COLUMN_GAME_ID + " = ? ";

    private NewsDbHelper mOpenHelper;

    // Holds the database object
    private SQLiteDatabase db;

    /*
    * Always return true, indicating that the
    * provider loaded correctly.
    */
    @Override
    public boolean onCreate() {
        mOpenHelper = new NewsDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         /*
         * Choose the table to query and a sort order based on the code returned for the incoming URI.
         */
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            case NEWS:
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

            // If the incoming URI was for a table
            case NEWS_WITH_GAME_ID:
                retCursor = getNewsByGameId(uri, projection, sortOrder);
                break;

            // If the incoming URI was for an item in a table
            case NEWS_WITH_GAME_ID_AND_NEWS_ID:
                retCursor = getNewsByGameIdAndNewsID(uri, projection, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getNewsByGameIdAndNewsID(Uri uri, String[] projection, String sortOrder) {
        String id = NewsEntry.getIdFromUri(uri);
        String GameID = NewsEntry.getGameIDFromUri(uri);

        String selection = sGameIdWithNewsIdSelection;
        String[] selectionArgs = new String[]{id,GameID};

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

    private Cursor getNewsByGameId(Uri uri, String[] projection, String sortOrder) {
        String GameID = NewsEntry.getGameIDFromUri(uri);

        String selection = sGameIdSelection;
        String[] selectionArgs = new String[]{GameID};

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

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = mOpenHelper.getWritableDatabase();
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

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case NEWS:
                rowsUpdated = db.update(NewsEntry.TABLE_NAME, values, selection, selectionArgs);
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
    public Uri insert(Uri uri, ContentValues values) {
        db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case NEWS: {
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
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case NEWS:
                return NewsEntry.CONTENT_TYPE;
            case NEWS_WITH_GAME_ID:
                return NewsEntry.CONTENT_TYPE;
            case NEWS_WITH_GAME_ID_AND_NEWS_ID:
                return NewsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

}
