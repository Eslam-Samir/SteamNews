package com.example.app.steamnews.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import com.example.app.steamnews.*;
import com.example.app.steamnews.Extras.FetchNewsTask;
import com.example.app.steamnews.Extras.NewsAdapter;
import com.example.app.steamnews.Extras.Utility;
import com.example.app.steamnews.R;
import com.example.app.steamnews.data.NewsContract;
import com.example.app.steamnews.data.NewsContract.NewsEntry;

public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int NEWS_LOADER = 0;
    private String GAMEID ;
    NewsAdapter titles_adapter;

    private static final String[] News_COLUMNS = {
            NewsEntry.TABLE_NAME + "." + NewsEntry._ID,
            NewsEntry.COLUMN_GAME_ID,
            NewsEntry.COLUMN_DATE,
            NewsEntry.COLUMN_TITLE,
            NewsEntry.COLUMN_CONTENTS,
            NewsEntry.COLUMN_AUTHOR,
            NewsEntry.COLUMN_FEED_LABEL,
            NewsEntry.COLUMN_URL,
    };

    public static final int COL_NEWS_ID = 0;
    public static final int COL_NEWS_GAME_ID = 1;
    public static final int COL_NEWS_DATE = 2;
    public static final int COL_NEWS_TITLE = 3;
    public static final int COL_NEWS_CONTENTS = 4;
    public static final int COL_NEWS_AUTHOR = 5;
    public static final int COL_NEWS_FEED_LABEL = 6;
    public static final int COL_NEWS_URL = 7;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);


        titles_adapter = new NewsAdapter(getActivity(), null, 0);
        ListView news_list = (ListView) rootView.findViewById(R.id.listview_news);
        news_list.setAdapter(titles_adapter);
        news_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                long News_Id = cursor.getLong(COL_NEWS_ID);
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class).setData(NewsEntry.buildNewsWithGameIdAndNewsID(GAMEID,News_Id));
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    private void updateNewsFeed(){
        FetchNewsTask FetchNews = new FetchNewsTask(getActivity());
        FetchNews.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateNewsFeed();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (GAMEID != null && !GAMEID.equals(Utility.getPreferredGame(getActivity()))) {
            getLoaderManager().restartLoader(NEWS_LOADER, null, this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(NEWS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        GAMEID = Utility.getPreferredGame(getActivity());
        // Sort order:  Descending, by date.
        String sortOrder = NewsContract.NewsEntry.COLUMN_DATE + " DESC";
        Uri newsWithGameIdUri = NewsContract.NewsEntry.buildNewsWithGameId(GAMEID);
        return new CursorLoader(
                getActivity(),
                newsWithGameIdUri,
                News_COLUMNS,
                null,
                null,
                sortOrder
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.
        titles_adapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        titles_adapter.swapCursor(null);
    }
}
