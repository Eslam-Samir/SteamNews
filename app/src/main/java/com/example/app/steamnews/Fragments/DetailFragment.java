package com.example.app.steamnews.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
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

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static final int DETAIL_LOADER = 0;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private ProgressBar progressBar ;

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

    public static final int COL_NEWS_ID = 0;
    public static final int COL_NEWS_GAME_ID = 1;
    public static final int COL_NEWS_DATE = 2;
    public static final int COL_NEWS_TITLE = 3;
    public static final int COL_NEWS_CONTENTS = 4;
    public static final int COL_NEWS_AUTHOR = 5;
    public static final int COL_NEWS_FEED_LABEL = 6;
    public static final int COL_NEWS_URL = 7;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "In onCreateLoader");
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }


        // Return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                News_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }
        String dateString = data.getString(NewsFragment.COL_NEWS_DATE);
        String author = data.getString(NewsFragment.COL_NEWS_AUTHOR);
        String title = data.getString(NewsFragment.COL_NEWS_TITLE);
        String contents = data.getString(NewsFragment.COL_NEWS_CONTENTS);
        String url = data.getString(NewsFragment.COL_NEWS_URL);

        TextView detailContentTextView = (TextView)getView().findViewById(R.id.detail_contents);
        TextView detailTitleTextView = (TextView)getView().findViewById(R.id.detail_title);
        TextView detailAuthorTextView = (TextView)getView().findViewById(R.id.detail_author);
        TextView detailDateTextView = (TextView)getView().findViewById(R.id.detail_date);
        TextView detailUrlTextView = (TextView)getView().findViewById(R.id.detail_url);

        ImageView image = (ImageView) getView().findViewById(R.id.image);
        String imageUrl = Utility.findUrl(contents);

        if(!imageUrl.equals("image not found")){
            RelativeLayout container = (RelativeLayout)getView().findViewById(R.id.image_container);
            progressBar = (ProgressBar)getView().findViewById(R.id.loading_image_progress);
            container.setVisibility(View.VISIBLE);
            loadImage(imageUrl, image);
        }


        detailContentTextView.setText(Utility.removeHtml(contents));
        detailTitleTextView.setText(title);
        detailDateTextView.setText(Utility.getReadableDateString(dateString));
        detailUrlTextView.setText(url);

        if(!author.equals("") && author != null )
        {
            detailAuthorTextView.setText("Author: " + author);
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
                Log.e("ImageLoadingFailed",message);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
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