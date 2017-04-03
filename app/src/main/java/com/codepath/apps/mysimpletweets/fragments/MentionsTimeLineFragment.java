package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by knyamagoudar on 4/1/17.
 */

public class MentionsTimeLineFragment extends Fragment {

    RecyclerView rvMentions;
    TwitterClient client;
    ArrayList<Tweet> mentions;
    TweetsArrayAdapter aTweets;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mentions_list,container,false);
        rvMentions = (RecyclerView) v.findViewById(R.id.rvmentionsList);
        rvMentions.setAdapter(aTweets);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity() );
        rvMentions.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvMentions.getContext(),
                linearLayoutManager.getOrientation());
        rvMentions.addItemDecoration(dividerItemDecoration);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mentions = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(getActivity(),mentions);

        client = TwitterApplication.getRestClient();
        populateMentions();
    }

    public void populateMentions(){
        Log.d("DEBUG","Calling PopulateTimeLine");
        client.getMentions(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("DEBUG",response.toString());

                ArrayList<Tweet> tweets = Tweet.fromJSONArray(response);
                mentions.addAll(tweets);
                aTweets.notifyDataSetChanged();

                // save mentions to database
                //saveToDataBase(tweets);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //get Mentions from database
                //getOfflineTweets();
            }
        });
    }
}
