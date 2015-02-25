package com.example.app.steamnews.data;


        import android.content.ComponentName;
        import android.content.ContentUris;
        import android.content.ContentValues;
        import android.content.pm.PackageManager;
        import android.content.pm.ProviderInfo;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.net.Uri;
        import android.os.Build;
        import android.test.AndroidTestCase;
        import android.util.Log;

        import com.example.app.steamnews.data.NewsContract.*;
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                NewsEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                NewsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from News table during delete", 0, cursor.getCount());
        cursor.close();

    }


    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
    */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // NewsProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                NewsProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: NewsProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + NewsContract.CONTENT_AUTHORITY,
                    providerInfo.authority, NewsContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: NewsProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
         This test doesn't touch the database.  It verifies that the ContentProvider returns
         the correct type for each type of URI that it can handle.
    */
    public void testGetType() {
        // content://com.example.app.steamnews/news
        String type = mContext.getContentResolver().getType(NewsEntry.CONTENT_URI);
        // vnd.android.cursor.dir/om.example.app.steamnews/news
        assertEquals("Error: the NewsEntry CONTENT_URI should return NewsEntry.CONTENT_TYPE",
                NewsEntry.CONTENT_TYPE, type);

        String testGameID = "570";
       // content://com.example.app.steamnews/news/570
       type = mContext.getContentResolver().getType(
                NewsEntry.buildNewsWithGameId(testGameID));
        // vnd.android.cursor.dir/com.example.app.steamnews/news/570
        assertEquals("Error: the NewsEntry CONTENT_URI with game id should return NewsEntry.CONTENT_TYPE",
                NewsEntry.CONTENT_TYPE, type);

        long testDate = 1419120000L; // December 21st, 2014
        // content://com.example.app.steamnews/news/570/20140612
        type = mContext.getContentResolver().getType(
                NewsEntry.buildNewsWithGameIdAndDate(testGameID, testDate));
        // vnd.android.cursor.item/com.example.app.steamnews/news/570/1419120000
        assertEquals("Error: the NewsEntry CONTENT_URI with game id and date should return NewsEntry.CONTENT_ITEM_TYPE",
                NewsEntry.CONTENT_ITEM_TYPE, type);

    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicNewsQuery() {
        // insert our test records into the database
        NewsDbHelper dbHelper = new NewsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues newsValues = TestUtilities.createNewsFeed();

        long newsRowId = db.insert(NewsEntry.TABLE_NAME, null, newsValues);
        assertTrue("Unable to Insert NewsEntry into the Database", newsRowId != -1);

        // Test the basic content provider query
        Cursor newsCursor = mContext.getContentResolver().query(
                NewsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor(newsCursor, newsValues);
    }


    public void testUpdateNews() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createNewsFeed();

        Uri newsUri = mContext.getContentResolver().
                insert(NewsEntry.CONTENT_URI, values);
        long newsRowId = ContentUris.parseId(newsUri);

        // Verify we got a row back.
        assertTrue(newsRowId != -1);
        Log.d(LOG_TAG, "New row id: " + newsRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(NewsEntry._ID, newsRowId);
        updatedValues.put(NewsEntry.COLUMN_TITLE, "The new tournament");

        int count = mContext.getContentResolver().update(
                NewsEntry.CONTENT_URI, updatedValues, NewsEntry._ID + "= ?",
                new String[] { Long.toString(newsRowId)});
        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                NewsEntry.CONTENT_URI,
                null,   // projection
                NewsEntry._ID + " = " + newsRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor(
                cursor, updatedValues);

        cursor.close();
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createNewsFeed();

        Uri newsUri = mContext.getContentResolver().insert(NewsEntry.CONTENT_URI, testValues);

        long newsRowId = ContentUris.parseId(newsUri);

        // Verify we got a row back.
        assertTrue(newsRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                NewsEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor(cursor, testValues);

        // Get the joined news and game id
        cursor = mContext.getContentResolver().query(
                NewsEntry.buildNewsWithGameId(TestUtilities.TEST_GAME),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor(cursor, testValues);

        // Get the joined news and game id with a start date
        cursor = mContext.getContentResolver().query(
                NewsEntry.buildNewsWithGameIdAndStartDate(
                        TestUtilities.TEST_GAME, TestUtilities.TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor(cursor, testValues);

        // Get the joined news for a specific date
        cursor = mContext.getContentResolver().query(
                NewsEntry.buildNewsWithGameIdAndDate(TestUtilities.TEST_GAME, 20141205),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor(cursor, testValues);
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertNewsValues(long newsRowId) {
        long currentTestDate = 20141205;
        long millisecondsInADay = 1000*60*60*24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate+= millisecondsInADay ) {
            ContentValues newsValues = new ContentValues();
            newsValues.put(NewsEntry.COLUMN_DATE, currentTestDate);
            newsValues.put(NewsEntry.COLUMN_GAME_ID, "570");
            newsValues.put(NewsEntry.COLUMN_TITLE, "test test");
            newsValues.put(NewsEntry.COLUMN_CONTENTS, "bla bla bla");
            newsValues.put(NewsEntry.COLUMN_AUTHOR, "me");
            newsValues.put(NewsEntry.COLUMN_FEED_LABEL, "f");
            returnContentValues[i] = newsValues;
        }
        return returnContentValues;
    }

    // Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time, so really do implement the
    // BulkInsert ContentProvider function.
    public void testBulkInsert() {
        ContentValues testValues = TestUtilities.createNewsFeed();
        Uri newsUri = mContext.getContentResolver().insert(NewsEntry.CONTENT_URI, testValues);
        long newsRowId = ContentUris.parseId(newsUri);

        ContentValues[] bulkInsertContentValues = createBulkInsertNewsValues(newsRowId);

        int insertCount = mContext.getContentResolver().bulkInsert(NewsEntry.CONTENT_URI, bulkInsertContentValues);


        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                NewsEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                NewsEntry.COLUMN_DATE + " ASC"  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT + 1);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating NewsEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}