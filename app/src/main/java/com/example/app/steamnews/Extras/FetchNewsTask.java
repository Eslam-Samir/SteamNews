package com.example.app.steamnews.Extras;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.app.steamnews.data.NewsContract.NewsEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class FetchNewsTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = FetchNewsTask.class.getSimpleName();
    private final Context mContext;
    public FetchNewsTask(Context context) {
        mContext = context;
    }

    private void GetNewsFromJSON(String newsJsonStr, int num_of_news) throws JSONException {

    /*  JSON Object appnews
        {
            JSON Array newsitems
            {
                JSON Object title
                JSON Object author
                JSON Object contents
                JSON Object feedlabel
                JSON Object date
            }
        } */
        final String NEWS_LIST_NAME = "appnews";
        final String NEWS_APPID = "appid";
        final String NEWS_LIST_ARRAY = "newsitems";
        final String NEWS_TITLE = "title";
        final String NEWS_AUTHER = "author";
        final String NEWS_CONTENT = "contents";
        final String NEWS_TYPE = "feedlabel";
        final String NEWS_DATE = "date";
        final String NEWS_URL = "url";

        JSONObject json = new JSONObject(newsJsonStr);
        JSONObject newsJSON = json.getJSONObject(NEWS_LIST_NAME);
        String gameid = newsJSON.getString(NEWS_APPID);
        JSONArray newsArray = newsJSON.getJSONArray(NEWS_LIST_ARRAY);
        for (int i=0; i<num_of_news; i++)
        {
            JSONObject one_news = newsArray.getJSONObject(i);

            long date = one_news.getLong(NEWS_DATE);
            String title = one_news.getString(NEWS_TITLE);
            String author = one_news.getString(NEWS_AUTHER);
            String contents = one_news.getString(NEWS_CONTENT);
            String feed_label = one_news.getString(NEWS_TYPE);
            String url = one_news.getString(NEWS_URL);
            long newsID = addNews(gameid,title,contents,author,feed_label,date,url);
            Log.d(LOG_TAG, Long.toString(newsID));
        }
    }
    @Override
    protected Void doInBackground(String... params) {
        String GAMEID = Utility.getPreferredGame(mContext);
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String newsJsonStr = null;

        String format = "json";
        int num_of_news = 14;
        int max_length = 400;

        try {
            // Construct the URL for the news query
            // example: http://api.steampowered.com/ISteamNews/GetNewsForApp/v0002/?appid=570&count=10&maxlength=400&format=json
            final String STEAM_NEWS_BASE_URL = "http://api.steampowered.com/ISteamNews/GetNewsForApp/v0002/?";
            final String GAME_ID_PARAM = "appid";
            final String NEWS_COUNT_PARAM = "count";
            final String NEWS_MAX_LENGTH_PARAM = "maxlength";
            final String FORMAT_PARAM = "format";

            Uri builtUri = Uri.parse(STEAM_NEWS_BASE_URL).buildUpon()
                    .appendQueryParameter(GAME_ID_PARAM, GAMEID)
                    .appendQueryParameter(NEWS_COUNT_PARAM, Integer.toString(num_of_news))
                    .appendQueryParameter(NEWS_MAX_LENGTH_PARAM, Integer.toString(max_length))
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .build();
            Log.e("main",builtUri.toString());
            URL url = new URL(builtUri.toString());

            // Create the request to steampowered, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {

                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            newsJsonStr = buffer.toString();
            GetNewsFromJSON(newsJsonStr, num_of_news);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    public long addNews(String GameID, String title, String content, String author, String feed_label, long date, String url) {
        long newsId;

        Log.v(LOG_TAG, "inserting " + title + ", with content: " + content);

        // First, check if the news with this title exists in the db
        Cursor newsCursor = mContext.getContentResolver().query(
                NewsEntry.CONTENT_URI,
                new String[]{NewsEntry._ID},
                NewsEntry.COLUMN_TITLE + " = ?",
                new String[]{title},
                null);
        if (newsCursor.moveToFirst()) {
            int newsIdIndex = newsCursor.getColumnIndex(NewsEntry._ID);
            newsId = newsCursor.getLong(newsIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues newsValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            newsValues.put(NewsEntry.COLUMN_TITLE, title);
            newsValues.put(NewsEntry.COLUMN_GAME_ID, GameID);
            newsValues.put(NewsEntry.COLUMN_CONTENTS, content);
            newsValues.put(NewsEntry.COLUMN_AUTHOR, author);
            newsValues.put(NewsEntry.COLUMN_DATE, Utility.getDbDateString(new Date(date * 1000L)));
            newsValues.put(NewsEntry.COLUMN_FEED_LABEL, feed_label);
            newsValues.put(NewsEntry.COLUMN_URL, url);

            Log.v(LOG_TAG, newsValues.toString());
            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(NewsEntry.CONTENT_URI,newsValues);
            Log.v(LOG_TAG, insertedUri.toString());
            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            newsId = ContentUris.parseId(insertedUri);
        }

        // Always close our cursor
        if (newsCursor !=  null) newsCursor.close();

        // Wait, that worked?  Yes!
        return newsId;
    }
}
