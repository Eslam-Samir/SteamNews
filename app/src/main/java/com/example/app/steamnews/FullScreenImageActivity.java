package com.example.app.steamnews;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.app.steamnews.Extras.TouchImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FullScreenImageActivity extends ActionBarActivity{

    private ProgressBar loading_bar ;
    private Bitmap showedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        final TouchImageView image = (TouchImageView) findViewById(R.id.full_image);
        loading_bar = (ProgressBar) findViewById(R.id.fullimage_progressBar);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("imageUrl")) {
            String imageUrl = intent.getStringExtra("imageUrl");
            ImageLoader imageLoader = ImageLoader.getInstance();
            ImageLoaderConfiguration mImageLoaderConfig =
                    new ImageLoaderConfiguration.Builder(this)
                            .denyCacheImageMultipleSizesInMemory()
                            .build();
            imageLoader.init(mImageLoaderConfig);
            DisplayImageOptions defaultOptions =
                    new DisplayImageOptions.Builder()
                            .cacheInMemory()
                            .cacheOnDisc()
                            .build();

            imageLoader.displayImage(imageUrl, image, defaultOptions,new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    loading_bar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
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
                    Log.e("ImageLoadingFailed", message);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    loading_bar.setVisibility(View.GONE);
                    showedImage = bitmap;
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fullscreen_image, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_image_save :
                if(showedImage != null) {
                    saveTask save = new saveTask();
                    save.execute(showedImage);
                }
                break ;
        }
        return super.onOptionsItemSelected(item);
    }

    //AsyncTask to save the image
    private class saveTask extends AsyncTask<Bitmap, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected Bitmap doInBackground(Bitmap... params) {

            Bitmap bitmap = params[0];

            // path to /sdcard/sdcard0/DCIM/Steam News/
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/DCIM/Steam News");
            myDir.mkdirs();

            //creates a unique name for every image based on date of saving the image
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy-hhmmss-SSS");
            String imageName = simpleDateFormat.format(new Date()) + ".jpg" ;
            File imageFile = new File (myDir, imageName);

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    //refresh the gallery
                    galleryAddPic(imageFile);
                    //clear the file output stream
                    fileOutputStream.flush();
                    fileOutputStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Toast toast = Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT) ;
            toast.show();
        }
    }

    //scans the gallery to add the saved image to it
    private void galleryAddPic(File imageFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        getApplicationContext().sendBroadcast(mediaScanIntent);
    }

}


