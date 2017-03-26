package com.codepath.apps.mysimpletweets;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ComposeFragment.TweetFragmentListner} interface
 * to handle interaction events.
 * Use the {@link ComposeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ComposeFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private User user;
    private String mParam2;

    TwitterClient client;
    TextView tvName;
    TextView tvScreenName;
    ImageView ivProfilePic;
    EditText etTweetBody;
    Button btSendTweet;

    private TweetFragmentListner mListener;

    public ComposeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param user Parameter 1.
     * @return A new instance of fragment ComposeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ComposeFragment newInstance(User user) {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable("user");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvName = (TextView) view.findViewById(R.id.tvTweetUserName);
        tvScreenName = (TextView) view.findViewById(R.id.tvTweetScreenName);
        ivProfilePic = (ImageView) view.findViewById(R.id.ivTweetProfilePic);

        etTweetBody = (EditText) view.findViewById(R.id.etTweetBody);
        btSendTweet = (Button) view.findViewById(R.id.btTweet);

        btSendTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTweet(etTweetBody.getText().toString());
            }
        });

        tvName.setText(user.getName());
        tvScreenName.setText(user.getScreenName());

        Picasso.with(getContext()).load(user.getProfileImageUrl()).into(ivProfilePic);

        client = TwitterApplication.getRestClient();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Tweet uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    public void sendTweet(String tweetBody){
        if(!tweetBody.isEmpty()){
            client.postTweet(tweetBody,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Tweet tweet = Tweet.fromJSON(response);

                    mListener = (TweetFragmentListner) getActivity();
                    mListener.onFragmentInteraction(tweet);
                    dismiss();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TweetFragmentListner) {
            mListener = (TweetFragmentListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface TweetFragmentListner {
        // TODO: Update argument type and name
        void onFragmentInteraction(Tweet tweet);
    }
}
