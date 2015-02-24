package com.example.app.steamnews.Extras;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchNewsTask extends AsyncTask<String, Void, String[]> {
    private final String LOG_TAG = FetchNewsTask.class.getSimpleName();
    private final Context mContext;
    private ArrayAdapter mNewsAdapter;
    public FetchNewsTask(Context context, ArrayAdapter ada) {
        mContext = context;
        mNewsAdapter = ada;
    }

    private String[] GetNewsFromJSON(String newsJsonStr, int num_of_news) throws JSONException {


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

        String[] news = new String[num_of_news] ;

        JSONObject json = new JSONObject(newsJsonStr);
        JSONObject newsJSON = json.getJSONObject(NEWS_LIST_NAME);
        JSONObject appid = newsJSON.getJSONObject(NEWS_APPID);
        JSONArray newsArray = newsJSON.getJSONArray(NEWS_LIST_ARRAY);
        for (int i=0; i<num_of_news; i++)
        {
            JSONObject one_news = newsArray.getJSONObject(i);

            String date = one_news.getString(NEWS_DATE);
            String title = one_news.getString(NEWS_TITLE);
            String author = one_news.getString(NEWS_AUTHER);
            String contents = one_news.getString(NEWS_CONTENT);
            String feed_label = one_news.getString(NEWS_TYPE);

            news[i] = title;
        }
        return news;
    }
    @Override
    protected String[] doInBackground(String... params) {

        // If there's no app id, there's nothing to look up.
        if (params.length == 0) {
            return null;
        }

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
                    .appendQueryParameter(GAME_ID_PARAM, params[0])
                    .appendQueryParameter(NEWS_COUNT_PARAM, Integer.toString(num_of_news))
                    .appendQueryParameter(NEWS_MAX_LENGTH_PARAM, Integer.toString(max_length))
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .build();

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

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
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

        try {
            return GetNewsFromJSON(newsJsonStr, num_of_news);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            mNewsAdapter.clear();
            for(String NewsTitleStr : result) {
                mNewsAdapter.add(NewsTitleStr);
            }
            // New data is back from the server.  Hooray!
        }
    }
}
