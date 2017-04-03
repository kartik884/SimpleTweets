package com.codepath.apps.mysimpletweets.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.activities.ProfileActivity;
import com.codepath.apps.mysimpletweets.activities.TweetDetail;
import com.codepath.apps.mysimpletweets.adapters.EndlessRecyclerViewScrollListener;
import com.codepath.apps.mysimpletweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by knyamagoudar on 3/31/17.
 */

public class TweetsListFragment extends Fragment {



    ArrayList<Tweet> tweets;
    TweetsArrayAdapter aTweets;
    RecyclerView lvTweets;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;
    User currentUser;
    TwitterClient client;
    private final int REQUEST_CODE = 20;

    //inflation logic
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tweets_list,parent,false);

        lvTweets = (RecyclerView) v.findViewById(R.id.lvTweets);
        lvTweets.setAdapter(aTweets);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity() );
        lvTweets.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(lvTweets.getContext(),
                linearLayoutManager.getOrientation());
        lvTweets.addItemDecoration(dividerItemDecoration);

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
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

        client = TwitterApplication.getRestClient();

        lvTweets.addOnScrollListener(scrollListener);

        return v;
    }


    //creation life cycle event
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(getActivity(),tweets);

        aTweets.setOnProfileClickListener(new TweetsArrayAdapter.OnProfileClickListner(){
            @Override
            public void onProfileClick(View itemView, int position) {
                Tweet tweet = tweets.get(position);
                Intent i = new Intent(getActivity(), ProfileActivity.class);
                i.putExtra("tweet",tweet);
                startActivity(i);
            }
        });

        aTweets.setOnItemClickListener(new TweetsArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Tweet tweet = tweets.get(position);
                Intent i = new Intent(getActivity(), TweetDetail.class);
                i.putExtra("tweet",tweet);
                startActivityForResult(i,REQUEST_CODE);
            }
        });
    }

    public void addAll(List<Tweet> tweetsList){
        tweets.addAll(tweetsList);
        aTweets.notifyDataSetChanged();
    }

    public void setCurrentUser(User user){
        currentUser = user;
    }

    public void getNewTweets(){
        long sinceId = tweets.get(0).getUid();

        client.getNewTweets(sinceId,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                ArrayList<Tweet> tempTweets = Tweet.fromJSONArray(response);
                tweets.addAll(0,tempTweets);
                aTweets.notifyDataSetChanged();
                Toast.makeText(getContext(),"Refreshed with new Tweets",Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
                if(tempTweets.size() > 0){
                    saveToDataBase(tempTweets);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getContext(),"Failed to get tweets",Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
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
                Toast.makeText(getContext(),"Failed to get tweets",Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
            }
        });
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
                        Toast.makeText(getContext(),"Failed to save tweets",Toast.LENGTH_SHORT).show();
                    }
                })
                .success(new Transaction.Success() {
                    @Override
                    public void onSuccess(Transaction transaction) {
                        Toast.makeText(getContext(),"Saved tweets",Toast.LENGTH_SHORT).show();
                    }
                }).build().execute();
    }

}
