package com.example.app.steamnews.data;


import android.net.Uri;
import android.test.AndroidTestCase;


public class TestNewsContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_NEWS_GAME_ID = "570";
    private static final long TEST_NEWS_DATE = 1419033600L;  // December 20th, 2014
    private static final String TEST_NORMALIZED_DATE = Long.toString(NewsContract.normalizeDate(TEST_NEWS_DATE)); //result 1375200000

    public void testBuildWeatherLocation() {
        Uri newsUri = NewsContract.NewsEntry.buildNewsWithGameIdAndDate(TEST_NEWS_GAME_ID, TEST_NEWS_DATE);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildNewsWithGameIdAndDate in " +
                        "NewsContract.",
                newsUri);
        assertEquals("Error: Game ID not properly appended to the end of the Uri",
                TEST_NEWS_GAME_ID, newsUri.getPathSegments().get(1));
        assertEquals("Error: date not properly appended to the end of the Uri",
                TEST_NORMALIZED_DATE, newsUri.getPathSegments().get(2));
        assertEquals("Error: Game ID and date Uri doesn't match our expected result",
                newsUri.toString(),
                "content://com.example.app.steamnews/news/570/1375200000");
    }
}