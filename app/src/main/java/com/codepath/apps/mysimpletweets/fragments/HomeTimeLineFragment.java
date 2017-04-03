package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.Tweet_Table;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by knyamagoudar on 4/1/17.
 */

public class HomeTimeLineFragment extends TweetsListFragment{


    TwitterClient client;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = TwitterApplication.getRestClient(); //returns a single client
        getCurrentUser();
        populateTimeLine();
    }

    public void getCurrentUser(){
        client.verifyCredentials(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                setCurrentUser(User.fromJSON(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getContext(),"Failed to get the user details",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void populateTimeLine(){

        Log.d("DEBUG","Calling PopulateTimeLine");
        client.getHomeTimeLine(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("DEBUG",response.toString());

                ArrayList<Tweet> tweets = Tweet.fromJSONArray(response);
                addAll(tweets);
                saveToDataBase(tweets);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                getOfflineTweets();
            }
        });
    }


    public void getOfflineTweets(){
        Toast.makeText(getContext(),"Failed to Connect to internet",Toast.LENGTH_SHORT).show();
        addAll(SQLite.select().
                from(Tweet.class).where().orderBy(Tweet_Table.createdAt,false).queryList());
    }


}
