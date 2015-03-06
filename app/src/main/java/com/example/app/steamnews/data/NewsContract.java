package com.example.app.steamnews.data;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class NewsContract {
/*
    public static final int COL_NEWS_ID = 0;
    public static final int COL_NEWS_GAME_ID = 1;
    public static final int COL_NEWS_DATE = 2;
    public static final int COL_NEWS_TITLE = 3;
    public static final int COL_NEWS_CONTENTS = 4;
    public static final int COL_NEWS_AUTHOR = 5;
    public static final int COL_NEWS_FEED_LABEL = 6;
    public static final int COL_NEWS_URL = 7;
    public static final int COL_NEWS_ONLINE_FEED_ID = 8;
*/
    public static final String CONTENT_AUTHORITY = "com.example.app.steamnews";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_NEWS = "news";

    public static final class NewsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEWS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;

        public static final String TABLE_NAME = "news";

        public static final String COLUMN_GAME_ID = "game_id";

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE = "date";

        // the title of the news
        public static final String COLUMN_TITLE = "title";

        // contents of the news
        public static final String COLUMN_CONTENTS = "contents";

        // author of the news
        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_FEED_LABEL = "feed_label";

        public static final String COLUMN_ONLINE_FEED_ID = "feed_id";

        //external urls
        public static final String COLUMN_URL = "url";

        public static Uri buildNewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildNewsWithGameId(String GameID) {
            return CONTENT_URI.buildUpon().appendPath(GameID).build();
        }

        public static Uri buildNewsWithGameIdAndNewsID(String GameID, long ID) {
            return CONTENT_URI.buildUpon().appendPath(GameID).appendPath(Long.toString(ID)).build();
        }

        public static String getGameIDFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }
}