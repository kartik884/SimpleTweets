package com.codepath.apps.mysimpletweets.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

    TextView tvName;
    TextView tvScreenName;
    ImageView ivProfilePic;
    EditText etTweetBody;
    Button btSendTweet;
    ImageButton ibtCancel;
    AlertDialog dialog;
    SharedPreferences pref;
    TwitterClient client;
    private TweetFragmentListner mListener;

    public ComposeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment ComposeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ComposeFragment newInstance() {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable("user");
        }

        client = TwitterApplication.getRestClient();
        getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_compose, container, false);

        tvName = (TextView) view.findViewById(R.id.tvTweetUserName);
        tvScreenName = (TextView) view.findViewById(R.id.tvTweetScreenName);
        ivProfilePic = (ImageView) view.findViewById(R.id.ivTweetProfilePic);

        etTweetBody = (EditText) view.findViewById(R.id.etTweetBody);
        btSendTweet = (Button) view.findViewById(R.id.btTweet);
        ibtCancel = (ImageButton) view.findViewById(R.id.ibtTweetComposeCancel);

        ibtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        btSendTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTweet(etTweetBody.getText().toString());
            }
        });

        return view;

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Do you want to save the draft?");

        builder.setPositiveButton("Save Draft", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SharedPreferences.Editor edit = pref.edit();
                String tweetBodyText = etTweetBody.getText().toString();
                if(!tweetBodyText.isEmpty()){
                    edit.putString("tweetBody",etTweetBody.getText().toString());
                    edit.commit();
                    Toast.makeText(getContext(),"Draft Saved",Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });

        builder.setNegativeButton("Delete Draft", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("tweetBody","");
                edit.commit();
                dismiss();
            }
        });

        dialog = builder.create();
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());

        etTweetBody.setText(pref.getString("tweetBody",""));
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

    public void getCurrentUser(){
        client.verifyCredentials(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //setCurrentUser(User.fromJSON(response));
                user = User.fromJSON(response);
                tvName.setText(user.getName());
                tvScreenName.setText(user.getScreenName());

                Glide.with(getContext()).load(user.getProfileImageUrl()).bitmapTransform(
                        new RoundedCornersTransformation(getContext(),4, 4)).into(ivProfilePic);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getContext(),"Failed to get the user details",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
