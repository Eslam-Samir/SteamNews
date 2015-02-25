package com.example.app.steamnews.Extras;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.app.steamnews.R;
import com.example.app.steamnews.data.NewsContract.NewsEntry;


public class NewsAdapter extends CursorAdapter {
    public NewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int idx_title = cursor.getColumnIndex(NewsEntry.COLUMN_TITLE);
        int idx_content = cursor.getColumnIndex(NewsEntry.COLUMN_CONTENTS);
        int idx_date = cursor.getColumnIndex(NewsEntry.COLUMN_DATE);
        int idx_author = cursor.getColumnIndex(NewsEntry.COLUMN_AUTHOR);

        return Utility.formatDate(cursor.getLong(idx_date)) +
                " - " + cursor.getString(idx_title);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_news, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        TextView tv = (TextView) view.findViewById(R.id.list_item_title);
        tv.setText(convertCursorRowToUXFormat(cursor));
    }
}
