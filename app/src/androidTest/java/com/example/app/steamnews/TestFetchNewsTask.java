package com.example.app.steamnews;


import android.database.Cursor;
import android.test.AndroidTestCase;

import com.example.app.steamnews.Extras.FetchNewsTask;
import com.example.app.steamnews.data.NewsContract.NewsEntry;

public class TestFetchNewsTask extends AndroidTestCase{
    static final String ADD_GAME_ID = "570";
    static final String ADD_TITLE = "test test";
    static final String ADD_CONTENT= "bla bla bla";
    static final String ADD_AUTHOR = "me";
    static final String ADD_FEEDLABEL = "f";
    static final String ADD_DATE = "20141205";


    public void testAddNews() {
        // start from a clean state
        getContext().getContentResolver().delete(NewsEntry.CONTENT_URI,
                NewsEntry.COLUMN_GAME_ID + " = ?",
                new String[]{ADD_GAME_ID});

        FetchNewsTask fnt = new FetchNewsTask(getContext(), null);
        long newsId = fnt.addNews(ADD_GAME_ID, ADD_TITLE,
                ADD_CONTENT, ADD_AUTHOR, ADD_FEEDLABEL, ADD_DATE);

        // does addnews return a valid record ID?
        assertFalse("Error: addNews returned an invalid ID on insert",
                newsId == -1);

        // test all this twice
        for ( int i = 0; i < 2; i++ ) {

            // does the ID point to our feed?
            Cursor newsCursor = getContext().getContentResolver().query(
                    NewsEntry.CONTENT_URI,
                    new String[]{
                            NewsEntry._ID,
                            NewsEntry.COLUMN_GAME_ID,
                            NewsEntry.COLUMN_TITLE,
                            NewsEntry.COLUMN_CONTENTS,
                            NewsEntry.COLUMN_AUTHOR,
                            NewsEntry.COLUMN_FEED_LABEL,
                            NewsEntry.COLUMN_DATE
                    },
                    NewsEntry.COLUMN_GAME_ID + " = ?",
                    new String[]{ADD_GAME_ID},
                    null);

            // these match the indices of the projection
            if (newsCursor.moveToFirst()) {
                assertEquals("Error: the queried value of newsId does not match the returned value" +
                        "from addLocation", newsCursor.getLong(0), newsId);
                assertEquals("Error: the queried value of game id is incorrect",
                        newsCursor.getString(1), ADD_GAME_ID);
                assertEquals("Error: the queried value of title is incorrect",
                        newsCursor.getString(2), ADD_TITLE);
                assertEquals("Error: the queried value of content is incorrect",
                        newsCursor.getString(3), ADD_CONTENT);
                assertEquals("Error: the queried value of author is incorrect",
                        newsCursor.getString(4), ADD_AUTHOR);
                assertEquals("Error: the queried value of feed label is incorrect",
                        newsCursor.getString(5), ADD_FEEDLABEL);
                assertEquals("Error: the queried value of date is incorrect",
                        newsCursor.getString(6), ADD_DATE);

            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a news query",
                    newsCursor.moveToNext());

            // add the feed again
            long newFeedId = fnt.addNews(ADD_GAME_ID, ADD_TITLE,
                    ADD_CONTENT, ADD_AUTHOR, ADD_FEEDLABEL, ADD_DATE);

            assertEquals("Error: inserting a feed again should return the same ID",
                    newsId, newFeedId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(NewsEntry.CONTENT_URI,
                NewsEntry.COLUMN_GAME_ID + " = ?",
                new String[]{ADD_GAME_ID});
    }

}