package com.codepath.apps.mysimpletweets.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.fragments.UserHeaderFragment;
import com.codepath.apps.mysimpletweets.fragments.UserTimelineFragment;
import com.codepath.apps.mysimpletweets.models.Tweet;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        String screenName = getIntent().getStringExtra("screen_name");
        Tweet tweet = getIntent().getParcelableExtra("tweet");

        if(savedInstanceState == null){
            UserTimelineFragment fragmentUserTimeLine;
            if(tweet != null){
                fragmentUserTimeLine =  UserTimelineFragment.newInstance(tweet.getUser().getScreenName());
            }else{
                fragmentUserTimeLine =  UserTimelineFragment.newInstance(screenName);
            }

            UserHeaderFragment fragmentUserHeader = UserHeaderFragment.newInstance(screenName,tweet);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flUserHeader,fragmentUserHeader);
            ft.replace(R.id.flUserTimelineContainer,fragmentUserTimeLine);
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login,menu);
        return true;
    }
}
