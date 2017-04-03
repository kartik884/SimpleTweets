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
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.adapters.EndlessRecyclerViewScrollListener;
import com.codepath.apps.mysimpletweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class UserTimelineFragment extends Fragment {
    private TwitterClient client;
    RecyclerView rvUserTimelineList;
    TweetsArrayAdapter aTweets;
    ArrayList<Tweet> tweets;
    EndlessRecyclerViewScrollListener scrollListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(getActivity(),tweets);

        client = TwitterApplication.getRestClient();
        populateUserTimeLine();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_timeline,container,false);

        rvUserTimelineList = (RecyclerView) v.findViewById(R.id.rvUserTimelineList);
        rvUserTimelineList.setAdapter(aTweets);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvUserTimelineList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvUserTimelineList.getContext(),
                linearLayoutManager.getOrientation());
        rvUserTimelineList.addItemDecoration(dividerItemDecoration);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMoreTweets(totalItemsCount);
            }
        };

        rvUserTimelineList.addOnScrollListener(scrollListener);


        return v;
    }

    public static UserTimelineFragment newInstance(String screenName) {
        UserTimelineFragment userTimelineFragment = new UserTimelineFragment();
        Bundle args = new Bundle();
        args.putString("screen_name", screenName);
        userTimelineFragment.setArguments(args);
        return userTimelineFragment;
    }

    public void populateUserTimeLine(){
        Log.d("DEBUG","Calling PopulateTimeLine");
        String screenName = getArguments().getString("screen_name");
        client.getUserTimeLine(screenName,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("DEBUG",response.toString());

                ArrayList<Tweet> tweetsList = Tweet.fromJSONArray(response);
                tweets.addAll(tweetsList);
                aTweets.notifyDataSetChanged();

                // save mentions to database
                //saveToDataBase(tweets);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //get Mentions from database
                //getOfflineTweets();
                Toast.makeText(getContext(),"FAILED TO GET USERTIMELINE",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadMoreTweets(int totalItemCount){
        Tweet tweet = tweets.get(totalItemCount-1);
        long maxId = tweets.get(totalItemCount-1).getUid();

        client.getUserTimeLineByMaxId(tweet.getUser().getScreenName(),maxId,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                ArrayList<Tweet> tempTweets = Tweet.fromJSONArray(response);
                tweets.addAll(tempTweets);
                aTweets.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getContext(),"Failed to get tweets",Toast.LENGTH_SHORT).show();
                //swipeContainer.setRefreshing(false);
            }
        });
    }
}
