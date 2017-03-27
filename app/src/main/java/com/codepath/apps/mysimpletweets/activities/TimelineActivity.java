package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.adapters.EndlessRecyclerViewScrollListener;
import com.codepath.apps.mysimpletweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.fragments.ComposeFragment;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.Tweet_Table;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

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
    private final int REQUEST_CODE = 20;
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


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(lvTweets.getContext(),
                linearLayoutManager.getOrientation());
        lvTweets.addItemDecoration(dividerItemDecoration);

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

        aTweets.setOnItemClickListener(new TweetsArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Tweet tweet = tweets.get(position);
                Intent i = new Intent(TimelineActivity.this, TweetDetail.class);
                i.putExtra("tweet",tweet);
                startActivityForResult(i,REQUEST_CODE);
            }
        });

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
                saveToDataBase(tweets);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                getOfflineTweets();
            }
        });
    }

    public void loadMoreTweets(int totalItemCount){
        long maxId = tweets.get(totalItemCount-1).getUid();

        client.getOldTweets(maxId,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                ArrayList<Tweet> tempTweets = Tweet.fromJSONArray(response);
                tweets.addAll(tempTweets);
                aTweets.notifyDataSetChanged();
                saveToDataBase(tempTweets);
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

                ArrayList<Tweet> tempTweets = Tweet.fromJSONArray(response);
                tweets.addAll(0,tempTweets);
                aTweets.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(),"Refreshed with new Tweets",Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
                if(tempTweets.size() > 0){
                    saveToDataBase(tempTweets);
                }
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

    public void saveToDataBase(ArrayList<Tweet> tweetsArray){

        FlowManager.getDatabase(com.codepath.apps.mysimpletweets.database.MyDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<Tweet>() {
                            @Override
                            public void processModel(Tweet tweet, DatabaseWrapper wrapper) {
                                // do work here -- i.e. user.delete() or user.update()
                                User user = tweet.getUser();
                                user.save();
                                tweet.save();
                            }
                        }).addAll(tweetsArray).build())
        .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {
                        Toast.makeText(getApplicationContext(),"Failed to save tweets",Toast.LENGTH_SHORT).show();
                    }
                })
                .success(new Transaction.Success() {
                    @Override
                    public void onSuccess(Transaction transaction) {
                        Toast.makeText(getApplicationContext(),"Saved tweets",Toast.LENGTH_SHORT).show();
                    }
                }).build().execute();
    }


    public void getOfflineTweets(){
        Toast.makeText(getApplicationContext(),"Failed to Connect to internet",Toast.LENGTH_SHORT).show();
        tweets.addAll(SQLite.select().
                from(Tweet.class).where().orderBy(Tweet_Table.createdAt,false).queryList());
        aTweets.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK ) {
            Tweet tweet = data.getParcelableExtra("tweet");
            tweets.add(0,tweet);
            aTweets.notifyItemChanged(0);
        }
    }
}
