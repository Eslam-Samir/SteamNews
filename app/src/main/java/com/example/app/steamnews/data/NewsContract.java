package com.example.app.steamnews.data;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

public class NewsContract {
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

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE = "date";

        // the title of the news
        public static final String COLUMN_TITLE = "title";

        // contents of the news
        public static final String COLUMN_CONTENTS = "contents";

        // author of the news
        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_FEED_LABEL = "feed_label";

        public static final String COLUMN_GAME_ID = "game_id";

        public static Uri buildNewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildNewsWithGameId(String GameID) {
            Uri uri = CONTENT_URI.buildUpon().appendPath(GameID).build();
            return uri;
        }

        public static String getGameIDFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
