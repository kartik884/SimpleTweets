package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class TweetDetail extends AppCompatActivity {


    TwitterClient client;
    TextView tvName;
    TextView tvScreenName;
    TextView tvBody;
    ImageView ivProfilePic;
    ImageView ivTweetImage;
    Button btSendTweet;
    Tweet tweet;
    EditText etReplyTweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        client = TwitterApplication.getRestClient();

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tweet = getIntent().getParcelableExtra("tweet");
        setUpView(tweet);

    }

    public void setUpView(final Tweet tweet){
        tvName = (TextView) findViewById(R.id.tvTweetDetailName);
        tvScreenName = (TextView) findViewById(R.id.tvTweetDetailScreenName);
        tvBody = (TextView) findViewById(R.id.tvTweetDetailBody);
        ivProfilePic = (ImageView) findViewById(R.id.ivTeetDetailProfilePic);
        ivTweetImage = (ImageView) findViewById(R.id.ivTweetDetailImage);
        btSendTweet = (Button) findViewById(R.id.btTweetDetailSubmit);
        etReplyTweet = (EditText) findViewById(R.id.etTweetDetailReply);

        btSendTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = etReplyTweet.getText().toString();
                if(!text.isEmpty()){
                    client.replyToTweet(text,tweet.getUid(),new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet tweet = Tweet.fromJSON(response);
                            Intent data = new Intent();
                            // Pass relevant data back as a result
                            data.putExtra("tweet", tweet);
                            data.putExtra("code", 200); // ints work too
                            // Activity finished ok, return the data
                            setResult(RESULT_OK, data); // set result code and bundle data for response
                            finish();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Toast.makeText(getContext(),"Tweet post failed",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(getContext(),"Tweet Cannot be empty",Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvBody.setText(tweet.getBody());
        tvName.setText(tweet.getUser().getName());
        tvScreenName.setText(tweet.getUser().getScreenName());

        Glide.with(getContext()).load(tweet.getUser().getProfileImageUrl()).bitmapTransform
                (new RoundedCornersTransformation(getContext(),4, 4)).into(ivProfilePic);

        if(!tweet.getImageUrl().isEmpty()){
            Glide.with(getContext()).load(tweet.getImageUrl()).bitmapTransform
                    (new RoundedCornersTransformation(getContext(),4, 4)).into(ivTweetImage);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
}
