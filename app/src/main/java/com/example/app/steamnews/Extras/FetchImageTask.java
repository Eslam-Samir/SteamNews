package com.example.app.steamnews.Extras;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

public class FetchImageTask extends AsyncTask<String,Void,Bitmap> {
    private ImageView imageView;

    public FetchImageTask(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String[] params) {
        try {
            String imageUrl = params[0];
            InputStream inputStream = (InputStream) new URL(imageUrl).getContent();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            //The new size we want to scale to
            final int REQUIRED_SIZE=70;

            //Find the correct scale value. It should be the power of 2.
            int scale=1;
            while(options.outWidth/scale/2>=REQUIRED_SIZE && options.outHeight/scale/2>=REQUIRED_SIZE)
                scale*=2;

            //Decode with inSampleSize
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize=scale;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,options2);
            return bitmap ;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(bitmap != null)
        {
        imageView.setImageBitmap(bitmap);
        }
    }
}
