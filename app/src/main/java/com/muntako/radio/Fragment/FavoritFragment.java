package com.muntako.radio.Fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.muntako.radio.DatabaseHelper;
import com.muntako.radio.R;
import com.muntako.radio.activity.MainActivity;
import com.muntako.radio.adapter.GridAdapter;
import com.muntako.radio.decoration.RecyclerItemClickListener;
import com.muntako.radio.model.Channel;
import com.muntako.radio.model.Constants;
import com.muntako.radio.service.ExoPlayerService;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by akhmadmuntako on 22/09/2016.
 */
public class FavoritFragment extends android.app.Fragment{
    GridAdapter gridAdapter;
    ArrayList<Channel> channels = new ArrayList<>();
    Context context;
    public static int SPEECH_REQUEST_CODE = 0;
    MainActivity mainActivity;

    DatabaseHelper helper;

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        context = (MainActivity)getActivity();
        mainActivity = (MainActivity)getActivity();
        mainActivity.getSupportActionBar().setTitle("Favorite Radios");
        helper = new DatabaseHelper(mainActivity);

        gridAdapter = mainActivity.getGridAdapter();
        if (channels.size()==0) {
            channels = (ArrayList<Channel>) helper.getFavChannel();
            gridAdapter.setChannels(channels);
        }

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        GridLayoutManager glm = new GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL,false);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(mainActivity, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Channel channel = gridAdapter.getChannels().get(position);
                playRadio(channel);
                ((MainActivity) context).setIndexChannel(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Channel channel = gridAdapter.getChannels().get(position);
                showPopupMenu(view,channel);
            }
        }));
        recyclerView.setLayoutManager(glm);
        recyclerView.setAdapter(gridAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = ((AppCompatActivity) getActivity());
            if (activity.getSupportActionBar() != null){

            }
        }
    }

    public void playRadio(Channel c){
        ((MainActivity) context).setChannel(c);
        ((MainActivity) context).showPlaybackControls(c);
        Intent intent = new Intent(context,ExoPlayerService.class);
        context.stopService(intent);
        intent.setAction(Constants.ACTION_PLAY);
        intent.putExtra("channel", (Serializable) c);
        Log.d("initialize", "start");
        mainActivity.startService(intent);
    }

    /**
     * Showing popup menu when long click
     */
    private void showPopupMenu(View view,Channel c) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_channel_favorit, popup.getMenu());
        popup.setOnMenuItemClickListener(new FavoritFragment.MyMenuItemClickListener(c));
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        Channel channelSelected ;

        public MyMenuItemClickListener(Channel c) {
            this.channelSelected = c;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    if(updateFavorite(channelSelected,false)){
                        gridAdapter.setChannels(helper.getFavChannel());
                        Toast.makeText(context, channelSelected.getName()+" deleted from favourite", Toast.LENGTH_SHORT).show();
                    }
                    return true;
            }
            return false;
        }
    }

    public boolean updateFavorite(Channel channel, boolean b){
        return helper.updateChannel(channel.getId(),b);
    }
}
