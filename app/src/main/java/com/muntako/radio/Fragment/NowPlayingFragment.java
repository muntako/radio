package com.muntako.radio.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.muntako.radio.R;
import com.muntako.radio.model.Channel;
import com.muntako.radio.model.Constants;
import com.muntako.radio.service.ExoPlayerService;

/**
 * Fragment that displays the currently playing show title and communicates with the media player
 * service to play and pause the media player.
 */
public class NowPlayingFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = NowPlayingFragment.class.getSimpleName();

    ToggleButton mPlayPauseButton;
    TextView mMediaPlayerTitle;
    TextView mMediaPlayerTime;
    TextView mBuffering;
    ImageView logo;
    RelativeLayout mPlayerData;
    ProgressBar progressBar;

    private GetMainService sGetMainService;
    private Channel mNowPlaying;

    public interface GetMainService {
        ExoPlayerService getMainService();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            sGetMainService = (GetMainService) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement GetMainService Interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_media_controls, container, false);
        mPlayPauseButton = (ToggleButton) v.findViewById(R.id.play_pause_button);
        mMediaPlayerTitle = (TextView) v.findViewById(R.id.media_player_title);
        mMediaPlayerTime = (TextView) v.findViewById(R.id.media_player_time);
        mBuffering = (TextView) v.findViewById(R.id.buffering_title);
        progressBar = (ProgressBar)v.findViewById(R.id.progress);
        logo = (ImageView)v.findViewById(R.id.image_row_vertical);
        mPlayPauseButton.setOnClickListener(this);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToggle();
        if (sGetMainService.getMainService()!=null){

        }
        try{
            mNowPlaying = sGetMainService.getMainService().getChannel();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setmNowPlaying(Channel channel) {
        this.mNowPlaying = channel;
        addDataToView();
    }


    private void addDataToView() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mMediaPlayerTitle.setText(mNowPlaying.getName());
                        mMediaPlayerTime.setText(mNowPlaying.getKota());
//                        Glide.with(getActivity()).load(mNowPlaying.getPathlogo()).into(logo);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * This method is called by the main activity when it receives a broadcast from the media player
     * service. This tells the media player bar to check if the media player is playing and set the
     * play pause button accordingly.
     */
    public void updateToggle() {
        ExoPlayerService mainService = sGetMainService.getMainService();

        //The media player might have been buffering so set the buffering view to gone.
        showBuffering(false);

        if (mainService != null) {
            mPlayPauseButton.setChecked(sGetMainService.getMainService().isPlaying());
        } else {
            mPlayPauseButton.setChecked(false);
        }
    }

    /**
     * If the media player is buffering on setting itself up this method is called to make the ui
     * changes indicating that the media player is currently buffering.
     *
     * @param isBuffering
     */
    public void showBuffering(boolean isBuffering) {
        if (isBuffering) {
            mBuffering.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            mMediaPlayerTime.setVisibility(View.GONE);
            mPlayPauseButton.setVisibility(View.GONE);
        } else {
            mBuffering.setVisibility(View.GONE);
            mMediaPlayerTime.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            mPlayPauseButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play_pause_button) {
            Intent mediaPlayerIntent = new Intent(getActivity(), ExoPlayerService.class);
            if (mPlayPauseButton.isChecked()) {//media player is playing or the play intent has been passed to the media player
                //if its playing then user clicked the pause button so sent pause intent.
                mediaPlayerIntent.setAction(Constants.ACTION_PLAY);
            } else {
                mediaPlayerIntent.setAction(Constants.ACTION_PAUSE);
            }
            getActivity().startService(mediaPlayerIntent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (sGetMainService.getMainService()!=null)
            mNowPlaying = sGetMainService.getMainService().getChannel();
    }
}
