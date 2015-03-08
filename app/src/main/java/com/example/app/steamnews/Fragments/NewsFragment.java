package com.example.app.steamnews.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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

//TODO use services (syncadapter) to get notification
public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int NEWS_LOADER = 0;
    private String GAMEID ;
    private NewsAdapter titles_adapter;
    private ListView news_list;
    private static final String NUM_OF_NEWS_KEY = "num_of_news";
    private static final String SELECTED_KEY = "selected_position";

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
    public static final int COL_NEWS_ONLINE_FEED_ID = 8;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        titles_adapter = new NewsAdapter(getActivity(), null, 0);
        news_list = (ListView) rootView.findViewById(R.id.listview_news);
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
                NewsContract.mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            NewsContract.mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(NUM_OF_NEWS_KEY)) {
            NewsContract.num_of_news = savedInstanceState.getInt(NUM_OF_NEWS_KEY);
        }

        if (NewsContract.mPosition != ListView.INVALID_POSITION ) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            news_list.smoothScrollToPosition(NewsContract.mPosition);
        }
        return rootView;
    }

    private void updateNewsFeed(){
        FetchNewsTask FetchNews = new FetchNewsTask(getActivity());
        FetchNews.execute(NewsContract.num_of_news);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(GAMEID == null) {
            GAMEID = Utility.getPreferredGame(getActivity());
        }
        if(NewsContract.mPosition == 0)
        {
            news_list.setSelection(0);
        }
        updateNewsFeed();
    }

    @Override
    public void onDestroy() {
        NewsContract.num_of_news = 10;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (GAMEID != null && !GAMEID.equals(Utility.getPreferredGame(getActivity()))) {
            getLoaderManager().restartLoader(NEWS_LOADER, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (NewsContract.mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, NewsContract.mPosition);
        }
        if(NewsContract.num_of_news != 10)
        {
            outState.putInt(NUM_OF_NEWS_KEY, NewsContract.num_of_news);
        }
        super.onSaveInstanceState(outState);
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
        String sortOrder = NewsContract.NewsEntry.COLUMN_DATE + " DESC LIMIT " + NewsContract.num_of_news;
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

        news_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            int currentFirstVisibleItem;
            int currentVisibleItemCount;
            int currentTotalItemCount;
            int currentScrollState;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.currentScrollState = scrollState;
                if (currentVisibleItemCount + currentFirstVisibleItem >= currentTotalItemCount) {
                    this.isScrollCompleted();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.currentTotalItemCount = totalItemCount;
            }
            /*detects if there's been a scroll which has completed */
            private void isScrollCompleted() {
                if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE) {
                    NewsContract.num_of_news = NewsContract.num_of_news + 5;
                    updateNewsFeed();
                    getLoaderManager().restartLoader(NEWS_LOADER, null, NewsFragment.this);
                }
            }
        });

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        titles_adapter.swapCursor(null);
    }
}
