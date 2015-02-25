package com.example.app.steamnews.data;


        import android.content.UriMatcher;
        import android.net.Uri;
        import android.test.AndroidTestCase;

/*
    Uncomment this class when you are ready to test your UriMatcher.  Note that this class utilizes
    constants that are declared with package protection inside of the UriMatcher, which is why
    the test must be in the same data package as the Android app code.  Doing the test this way is
    a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final long TEST_DATE = 1419033600L;  // December 20th, 2014
    private static final String TEST_GAME_ID = "570";

    // content://com.example.app.steamnews/news"
    private static final Uri TEST_NEWS_DIR = NewsContract.NewsEntry.CONTENT_URI;
    private static final Uri TEST_NEWS_WITH_GAME_ID_DIR = NewsContract.NewsEntry.buildNewsWithGameId(TEST_GAME_ID);
    private static final Uri TEST_NEWS_WITH_GAME_ID_AND_DATE_DIR = NewsContract.NewsEntry.buildNewsWithGameIdAndDate(TEST_GAME_ID, TEST_DATE);

    public void testUriMatcher() {
        UriMatcher testMatcher = NewsProvider.buildUriMatcher();

        assertEquals("Error: The NEWS URI was matched incorrectly.",
                testMatcher.match(TEST_NEWS_DIR), NewsProvider.NEWS);
        assertEquals("Error: The NEWS WITH GAME ID URI was matched incorrectly.",
                testMatcher.match(TEST_NEWS_WITH_GAME_ID_DIR), NewsProvider.NEWS_WITH_GAME_ID);
        assertEquals("Error: The NEWS WITH GAME ID AND DATE URI was matched incorrectly.",
                testMatcher.match(TEST_NEWS_WITH_GAME_ID_AND_DATE_DIR), NewsProvider.NEWS_WITH_GAME_ID_AND_DATE);

    }
}