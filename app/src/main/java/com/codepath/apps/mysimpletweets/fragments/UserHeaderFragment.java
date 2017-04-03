package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by knyamagoudar on 4/2/17.
 */

public class UserHeaderFragment extends Fragment {


    TwitterClient client;
    ImageView ivProfilePic;
    TextView tvScreenName;
    TextView tvDescription;
    TextView tvFollowers;
    TextView tvFriends;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_header,container,false);
        ivProfilePic = (ImageView) v.findViewById(R.id.ivUserProfilePic);
        tvScreenName =  (TextView) v.findViewById(R.id.tvUserProfileName);
        tvDescription = (TextView) v.findViewById(R.id.tvTagLine);
        tvFollowers = (TextView) v.findViewById(R.id.tvFollowers);
        tvFriends = (TextView) v.findViewById(R.id.tvFriends);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = TwitterApplication.getRestClient();
        Tweet tweet = getArguments().getParcelable("tweet");
        if(tweet == null){
            client.verifyCredentials(new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    User user = User.fromJSON(response);
                    populateHeader(user);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(getContext(),"Failed to get User Info",Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            client.getUserInfo(tweet.getUid(),tweet.getUser().getScreenName(),new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    User user = User.fromJSON(response);
                    populateHeader(user);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(getContext(),"Failed to get User Info",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static UserHeaderFragment newInstance(String screenName, Tweet tweet) {
        UserHeaderFragment userHeaderFragment = new UserHeaderFragment();
        Bundle args = new Bundle();
        args.putString("screen_name", screenName);
        args.putParcelable("tweet",tweet);
        userHeaderFragment.setArguments(args);
        return userHeaderFragment;
    }

    public void populateHeader(User user){

        tvScreenName.setText("@"+user.getName());
        tvDescription.setText(user.getDescription());
        tvFollowers.setText(user.getFollowersCount()+" Followers");
        tvFriends.setText(user.getFriendsCount()+" Friends");

        Glide.with(getContext()).load(user.getProfileImageUrl()).bitmapTransform(
                new RoundedCornersTransformation(getContext(),4, 4)).into(ivProfilePic);

    }


}
