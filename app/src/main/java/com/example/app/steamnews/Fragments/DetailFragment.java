package com.example.app.steamnews.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.app.steamnews.Extras.Utility;
import com.example.app.steamnews.FullScreenImageActivity;
import com.example.app.steamnews.R;
import com.example.app.steamnews.data.NewsContract;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

@SuppressWarnings("deprecation")
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    public static final String DETAIL_URI = "URI";
    private Uri mUri;
    private static final int DETAIL_LOADER = 0;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String News_SHARE_HASHTAG = "#SteamNewsApp" ;
    private String shareNewsString;
    private ProgressBar progressBar ;
    private RelativeLayout container;
    private ShareActionProvider mShareActionProvider;
    private String gameID ;
    private static final String[] News_COLUMNS = {
            NewsContract.NewsEntry.TABLE_NAME + "." + NewsContract.NewsEntry._ID,
            NewsContract.NewsEntry.COLUMN_GAME_ID,
            NewsContract.NewsEntry.COLUMN_DATE,
            NewsContract.NewsEntry.COLUMN_TITLE,
            NewsContract.NewsEntry.COLUMN_CONTENTS,
            NewsContract.NewsEntry.COLUMN_AUTHOR,
            NewsContract.NewsEntry.COLUMN_FEED_LABEL,
            NewsContract.NewsEntry.COLUMN_URL,
    };

//  public static final int COL_NEWS_ID = 0;
    public static final int COL_NEWS_GAME_ID = 1;
    public static final int COL_NEWS_DATE = 2;
    public static final int COL_NEWS_TITLE = 3;
    public static final int COL_NEWS_CONTENTS = 4;
    public static final int COL_NEWS_AUTHOR = 5;
//  public static final int COL_NEWS_FEED_LABEL = 6;
    public static final int COL_NEWS_URL = 7;
//  public static final int COL_NEWS_ONLINE_FEED_ID = 8;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Bundle arguments = getArguments();
        if (arguments != null) {
                 mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (shareNewsString != null) {
            mShareActionProvider.setShareIntent(createShareNewsIntent());
        }
    }

    private Intent createShareNewsIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareNewsString + News_SHARE_HASHTAG);
        return shareIntent;
    }

    public void onGameChanged( String newGameID ) {
        Uri uri = mUri;
        if (null != uri) {
            long id = Long.valueOf(NewsContract.NewsEntry.getIdFromUri(uri));
            mUri = NewsContract.NewsEntry.buildNewsWithGameIdAndNewsID(newGameID, id);
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "In onCreateLoader");
        if ( null != mUri ) {
            // Return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    News_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }
        gameID = data.getString(COL_NEWS_GAME_ID);
        String dateString = data.getString(COL_NEWS_DATE);
        String author = data.getString(COL_NEWS_AUTHOR);
        String title = data.getString(COL_NEWS_TITLE);
        String contents = data.getString(COL_NEWS_CONTENTS);
        String url = data.getString(COL_NEWS_URL);

        @SuppressWarnings("ConstantConditions")
        TextView detailContentTextView = (TextView)getView().findViewById(R.id.detail_contents);
        TextView detailTitleTextView = (TextView)getView().findViewById(R.id.detail_title);
        TextView detailAuthorTextView = (TextView)getView().findViewById(R.id.detail_author);
        TextView detailDateTextView = (TextView)getView().findViewById(R.id.detail_date);
        TextView detailUrlTextView = (TextView)getView().findViewById(R.id.detail_url);
        progressBar = (ProgressBar)getView().findViewById(R.id.loading_image_progress);
        container = (RelativeLayout) getView().findViewById(R.id.image_container);

        ImageView image = (ImageView) getView().findViewById(R.id.image);
        String imageUrl = Utility.findUrl(contents);

        if(!imageUrl.equals("image not found")){
            loadImage(imageUrl, image);
        }
        else{
            container.setBackgroundColor(0xFFFFFF);
            image.setImageResource(Utility.selectIcon(gameID));
        }


        detailContentTextView.setText(Utility.removeHtml(contents));
        detailTitleTextView.setText(title);
        detailDateTextView.setText(Utility.getReadableDateString(dateString));
        detailUrlTextView.setText(url);

        if(!author.equals(""))
        {
            detailAuthorTextView.setText("Author: " + author);
        }

        shareNewsString = Utility.selectText(getActivity(),gameID) + " News \n"
                + Utility.getReadableDateString(dateString)+ "\n"
                + title + "\n"
                + "Link: " + url + "\n" ;

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareNewsIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    private void loadImage(final String imageUrl, final ImageView image){
        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration mImageLoaderConfig =
                new ImageLoaderConfiguration.Builder(getActivity())
                        .denyCacheImageMultipleSizesInMemory()
                        .build();
        imageLoader.init(mImageLoaderConfig);
        DisplayImageOptions defaultOptions =
                new DisplayImageOptions.Builder()
                        .cacheInMemory()
                        .cacheOnDisc()
                        .showImageOnFail(Utility.selectIcon(gameID))
    //                  .showImageForEmptyUri(R.drawable.empty_photo)
    //                  .showStubImage(R.drawable.empty_photo)
                        .build();

        imageLoader.displayImage(imageUrl, image, defaultOptions,new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
                String message = "";
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "Input/Output error";
                        break;
                    case DECODING_ERROR:
                        message = "Image can't be decoded";
                        break;
                    case NETWORK_DENIED:
                        message = "Downloads are denied";
                        break;
                    case OUT_OF_MEMORY:
                        message = "Out Of Memory error";
                        break;
                    case UNKNOWN:
                        message = "Unknown error";
                        break;
                }
                container.setMinimumWidth(50);
                container.setMinimumHeight(50);
                container.setBackgroundColor(0xFFFFFF);
                Log.e("ImageLoadingFailed",message);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                container.setMinimumWidth(50);
                container.setMinimumHeight(50);
                progressBar.setVisibility(View.GONE);
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
                        intent.putExtra("imageUrl",imageUrl);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        });

    }
}