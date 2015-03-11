package com.example.app.steamnews.Extras;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app.steamnews.Fragments.NewsFragment;
import com.example.app.steamnews.R;

public class NewsAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_FIRST_ITEM = 0;
    private static final int VIEW_TYPE_OTHER_ITEM = 1;

    private boolean mUseFirstItemLayout = true;

    public NewsAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_FIRST_ITEM: {
                layoutId = R.layout.list_item_news_first;
                break;
            }
            case VIEW_TYPE_OTHER_ITEM: {
                layoutId = R.layout.list_item_news;
                break;
            }
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_FIRST_ITEM: {
                ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
                TextView gameNameView = (TextView) view.findViewById(R.id.list_item_game_name);
                String GameID = cursor.getString(NewsFragment.COL_NEWS_GAME_ID);
                if(Utility.selectText(context,GameID).equals("no_match"))
                {
                    gameNameView.setText("");
                }
                else
                {
                    gameNameView.setText(Utility.selectText(context,cursor.getString(NewsFragment.COL_NEWS_GAME_ID)));
                }
                if(Utility.selectIcon(GameID) != 0)
                {
                    iconView.setImageResource(Utility.selectIcon(GameID));
                }
                break;
            }
        }

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

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseFirstItemLayout) ? VIEW_TYPE_FIRST_ITEM : VIEW_TYPE_OTHER_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public void setUseFirstItemLayout(boolean UseFirstItemLayout) {
        mUseFirstItemLayout = UseFirstItemLayout;
    }
}
