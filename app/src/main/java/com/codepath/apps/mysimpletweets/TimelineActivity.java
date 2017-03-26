package com.codepath.apps.mysimpletweets;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.adapters.EndlessRecyclerViewScrollListener;
import com.codepath.apps.mysimpletweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ComposeFragment.TweetFragmentListner {

    TwitterClient client;
    ArrayList<Tweet> tweets;
    TweetsArrayAdapter aTweets;
    private RecyclerView lvTweets;
    private User currentUser;
    private EndlessRecyclerViewScrollListener scrollListener;

    private SwipeRefreshLayout swipeContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        lvTweets = (RecyclerView) findViewById(R.id.lvTweets);
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(this,tweets);
        lvTweets.setAdapter(aTweets);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lvTweets.setLayoutManager(linearLayoutManager);

//        lvTweets.setOnScrollListener(new EndlessScrollListener(5,0) {
//            @Override
//            public boolean onLoadMore(int page, int totalItemsCount) {
//                loadMoreTweets(totalItemsCount);
//                return true;
//            }
//        });


        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                getNewTweets();
            }
        });


        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMoreTweets(totalItemsCount);
            }
        };

        lvTweets.addOnScrollListener(scrollListener);
        client = TwitterApplication.getRestClient(); //returns a single client
        getCurrentUser();
        populateTimeLine();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    public void onSendAction(MenuItem mi) {

        Toast.makeText(getApplicationContext(), "Compose tweet", Toast.LENGTH_SHORT).show();

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        ComposeFragment filterFragment = ComposeFragment.newInstance(currentUser);
        filterFragment.show(fm,"fragment_compose");
    }

    public void getCurrentUser(){
        client.verifyCredentials(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                currentUser = User.fromJSON(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(),"Failed to get the user details",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void populateTimeLine(){

        Log.d("DEBUG","Calling PopulateTimeLine");
        client.getHomeTimeLine(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("DEBUG",response.toString());

                //deserialize json
                //create models and them to adapter
                //load data into listview
                int tweetsLen = tweets.size();
                tweets.addAll(Tweet.fromJSONArray(response));


                aTweets.notifyItemRangeChanged(tweetsLen,tweets.size());
                Log.d("DEBUG","ON SUCCESS");

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG",errorResponse.toString());
            }
        });
    }

    public void loadMoreTweets(int totalItemCount){
        long maxId = tweets.get(totalItemCount-1).getUid();

        client.getOldTweets(maxId,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                tweets.addAll(Tweet.fromJSONArray(response));
                aTweets.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(),"Failed to get tweets",Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
            }
        });
    }

    public void getNewTweets(){
        long sinceId = tweets.get(0).getUid();

        client.getNewTweets(sinceId,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                tweets.addAll(0,Tweet.fromJSONArray(response));
                aTweets.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(),"Refreshed with new Tweets",Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(),"Failed to get tweets",Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
            }
        });
    }

    @Override
    public void onFragmentInteraction(Tweet tweet) {

        tweets.add(0,tweet);
        aTweets.notifyDataSetChanged();
    }
}
