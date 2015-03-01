package com.example.app.steamnews.Extras;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.app.steamnews.Fragments.NewsFragment;
import com.example.app.steamnews.R;

public class NewsAdapter extends CursorAdapter {

    public NewsAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_news, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read date from cursor
        String dateString = cursor.getString(NewsFragment.COL_NEWS_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateString));

        // Read title from cursor
        String title = cursor.getString(NewsFragment.COL_NEWS_TITLE);
        // Find TextView and set title on it
        viewHolder.titleView.setText(title);
    }

    public static class ViewHolder {
        public final TextView dateView;
        public final TextView titleView;

        public ViewHolder(View view) {
            dateView = (TextView) view.findViewById(R.id.list_item_date);
            titleView = (TextView) view.findViewById(R.id.list_item_title);
        }
    }
}
