package com.codepath.apps.mysimpletweets.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.util.Util;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.codepath.apps.mysimpletweets.R.id.tvName;

/**
 * Created by knyamagoudar on 3/21/17.
 */

public class TweetsArrayAdapter extends RecyclerView.Adapter<TweetsArrayAdapter.ViewHolder> {


    // Define listener member variable
    private OnItemClickListener listener;
    private OnProfileClickListner profileClickListner;
    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnProfileClickListner {
        void onProfileClick(View itemView, int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnProfileClickListener(OnProfileClickListner listener) {
        this.profileClickListner = listener;
    }

    // Store a member variable for the contacts
    private List<Tweet> mTweets;
    // Store the context for easy access
    private Context mContext;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder  {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tvUserName;
        public TextView tvBody;
        public TextView tvRelativeTime;
        public ImageView ivProfilePic;
        public ImageView ivTweetImage;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tvUserName = (TextView) itemView.findViewById(tvName);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvRelativeTime = (TextView) itemView.findViewById(R.id.tvRelativeTime);
            ivProfilePic = (ImageView) itemView.findViewById(R.id.ivProfilePic);
            ivTweetImage = (ImageView) itemView.findViewById(R.id.ivTweetImage);

            ivProfilePic.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    //Toast.makeText(getContext(),"Cliked on profile image",Toast.LENGTH_SHORT).show();
                    if(profileClickListner != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            profileClickListner.onProfileClick(itemView, position);
                        }
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });

        }
    }





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
        Glide.with(getContext()).load(tweet.getUser().getProfileImageUrl()).bitmapTransform(new RoundedCornersTransformation(mContext,4,4)).into(ivProfilePic);

        ImageView ivTweetImage = holder.ivTweetImage;
        ivTweetImage.setImageResource(0);
        if(!tweet.getImageUrl().isEmpty()){
            Glide.with(getContext()).load(tweet.getImageUrl()).bitmapTransform(new RoundedCornersTransformation(mContext,20, 20)).into(ivTweetImage);
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
