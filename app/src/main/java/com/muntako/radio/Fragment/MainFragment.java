package com.muntako.radio.Fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
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

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by akhmadmuntako on 21/09/2016.
 */
public class MainFragment extends android.app.Fragment {
    GridAdapter gridAdapter;
    ArrayList<Channel> channels = new ArrayList<>();
    Context context;
    public static int SPEECH_REQUEST_CODE = 0;
    MainActivity mainActivity;
    DatabaseHelper helper;
    SharedPreferences prefs = null;



//    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        context = (MainActivity) getActivity();
        mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle("All Radios");

        prefs = mainActivity.getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);

        helper = new DatabaseHelper(mainActivity);
        Target target = new ViewTarget(R.id.caption_text, mainActivity);
        if (prefs.getBoolean("firstrun", true)) {
            new ShowcaseView.Builder(mainActivity, true)
                    .setTarget(target)
                    .setStyle(R.style.ShowcaseView)
                    .setContentTitle("Command Text")
                    .setContentText("You should do command that showed in this field")
                    .build();
        }

        gridAdapter = ((MainActivity) getActivity()).getGridAdapter();
        if (channels.size()==0) {
            channels = (ArrayList<Channel>) helper.getAllChannel();
            gridAdapter.setChannels(channels);
        }

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        GridLayoutManager glm = new GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(mainActivity, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Channel channel = gridAdapter.getChannels().get(position);
                playRadio(channel);
                ((MainActivity) context).setIndexChannel(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                showPopupMenu(view,gridAdapter.getChannels().get(position));
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
            if (activity.getSupportActionBar() != null) {

            }
        }
    }

    public void playRadio(Channel c) {
        mainActivity.setChannel(c);
        mainActivity.showPlaybackControls(c);
        Intent intent = new Intent(context, ExoPlayerService.class);
        context.stopService(intent);
        intent.setAction(Constants.ACTION_PLAY);
        intent.putExtra("channel", (Serializable) c);
        Log.d("initialize", "start");
        mainActivity.startService(intent);
    }

    /**
     * Showing popup menu when long clicked
     */
    private void showPopupMenu(View view,Channel c) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_channel, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(c));
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        Channel selectedChannel;
        public MyMenuItemClickListener(Channel c) {
            this.selectedChannel = c;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    if(updateFavorite(selectedChannel,true)){
                        Toast.makeText(context, selectedChannel.getName() +" added to favourite", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case R.id.delete_channel:
                    showDialog(selectedChannel);
                    return true;
            }
            return false;
        }
    }

    public boolean updateFavorite(Channel channel, boolean b){
        return helper.updateChannel(channel.getId(),b);
    }

    public void showDialog(final Channel c){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder.setTitle("Warning");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure want to delete "+c.getName() + " from list?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity

                        if (deleteChannel(c)){
                            gridAdapter.setChannels(helper.getAllChannel());
                            Toast.makeText(getActivity(),helper.getAllChannel().size()+"",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public boolean deleteChannel(Channel c){
        return helper.deleteChannel(c.getId());
    }

}
