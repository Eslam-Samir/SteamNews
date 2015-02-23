package com.example.app.steamnews.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.app.steamnews.DetailActivity;
import com.example.app.steamnews.Extras.FetchNewsTask;
import com.example.app.steamnews.R;

import java.util.ArrayList;

public class NewsFragment extends Fragment {
    ArrayAdapter titles_adapter;
    public NewsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        titles_adapter = new ArrayAdapter(getActivity(), R.layout.list_item_news, R.id.list_item_title, new ArrayList<String>());
        ListView news_list = (ListView) rootView.findViewById(R.id.listview_news);
        news_list.setAdapter(titles_adapter);

        news_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void updateNewsFeed(){
        // 570 is "dota 2" news id
        FetchNewsTask FetchNews = new FetchNewsTask(getActivity(),titles_adapter);
        FetchNews.execute("570");
    }

    @Override
    public void onStart() {
        super.onStart();
        updateNewsFeed();
    }
}
