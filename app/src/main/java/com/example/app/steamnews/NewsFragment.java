package com.example.app.steamnews;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewsFragment extends Fragment {

    public NewsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        String[] titles = {"Dota 2 Update - February 20th 2015",
                "Three Lane Highway: the problem with Year Beast isn't the cost, it's the reward",
                "Dota 2 Update - February 18th 2015",
                "Dote Night: Seriously, Puck This",
                "Dota 2 Update - February 16th 2015",
        };

        List<String> titles_arraylist = new ArrayList<String>(Arrays.asList(titles));
        ArrayAdapter titles_adapter = new ArrayAdapter(getActivity(), R.layout.list_item_news, R.id.list_item_title, titles_arraylist);
        ListView news_list = (ListView) rootView.findViewById(R.id.listview_news);
        news_list.setAdapter(titles_adapter);
        return rootView;
    }


}
