package com.codepath.apps.mysimpletweets.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.util.Util;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by knyamagoudar on 3/21/17.
 */

public class TweetsArrayAdapter extends RecyclerView.Adapter<TweetsArrayAdapter.ViewHolder> {




    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tvUserName;
        public TextView tvBody;
        public TextView tvRelativeTime;
        public ImageView ivProfilePic;
        public ImageView ivTweetImage;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tvUserName = (TextView) itemView.findViewById(R.id.tvName);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvRelativeTime = (TextView) itemView.findViewById(R.id.tvRelativeTime);
            ivProfilePic = (ImageView) itemView.findViewById(R.id.ivProfilePic);
            ivTweetImage = (ImageView) itemView.findViewById(R.id.ivTweetImage);
        }
    }


    // Store a member variable for the contacts
    private List<Tweet> mTweets;
    // Store the context for easy access
    private Context mContext;


    // Pass in the contact array into the constructor
    public TweetsArrayAdapter(Context context, List<Tweet> tweets) {
        mTweets = tweets;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    @Override
    public TweetsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_tweet, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tweet tweet = mTweets.get(position);

        TextView tvName = holder.tvUserName;
        tvName.setText(tweet.getUser().getName());

        TextView tvBody = holder.tvBody;
        tvBody.setText(tweet.getBody());

        TextView tvRelativeTime = holder.tvRelativeTime;
        tvRelativeTime.setText(Util.getRelativeTimeAgo(tweet.getCreatedAt()));

        ImageView ivProfilePic = holder.ivProfilePic;
        ivProfilePic.setImageResource(0);
        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).into(ivProfilePic);

        ImageView ivTweetImage = holder.ivTweetImage;
        ivTweetImage.setImageResource(0);
        if(!tweet.getImageUrl().isEmpty()){
            Picasso.with(getContext()).load(tweet.getImageUrl()).into(ivTweetImage);
        }
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    //    public TweetsArrayAdapter(Context context,ArrayList<Tweet> tweets) {
//        super(context,0,tweets);
//    }


//    @NonNull
//    @Override
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//
//        Tweet tweet = getItem(position);
//
//        if(convertView == null){
//            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet,parent,false);
//        }
//
//        ImageView ivProfilePic = (ImageView) convertView.findViewById(R.id.ivProfilePic);
//        TextView tvUserName = (TextView) convertView.findViewById(R.id.tvName);
//        TextView tvBody = (TextView) convertView.findViewById(R.id.tvBody);
//        TextView tvRelativeTime = (TextView) convertView.findViewById(R.id.tvRelativeTime);
//
//        tvUserName.setText(tweet.getUser().getScreenName());
//        tvBody.setText(tweet.getBody());
//        tvRelativeTime.setText(Util.getRelativeTimeAgo(tweet.getCreatedAt()));
//
//        ivProfilePic.setImageResource(0);
//        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).into(ivProfilePic);
//
//        return convertView;
//    }



}
