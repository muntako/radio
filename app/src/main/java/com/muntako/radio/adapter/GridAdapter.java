package com.muntako.radio.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.muntako.radio.R;
import com.muntako.radio.model.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 20/06/2016.
 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.MyViewHolder> implements Filterable {
    private List<Channel> channels = new ArrayList<>();
    private List<Channel> filteredList = new ArrayList<>();
    Context context;
    ChannelFilter channelFilter;

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_channel, parent, false);
        return new MyViewHolder(itemView);
    }

    public GridAdapter(Context c){
        this.context = c;
    }

    public GridAdapter(List<Channel> listData, Context c) {
        this.channels = listData;
        this.context = c;
        this.filteredList = listData;
        getFilter();
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final Channel channel = filteredList.get(position);

        Glide.with(holder.thumbnailUrl.getContext()).load(channel.getPathlogo()).into(holder.thumbnailUrl);
    }

    public List<Channel> getChannels() {
        return filteredList;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
        this.filteredList = channels;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return filteredList.size();
    }


    @Override
    public Filter getFilter() {
        if (channelFilter == null) {
            channelFilter = new ChannelFilter();
        }
        return channelFilter;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView overflow, thumbnailUrl;

        public MyViewHolder(View view) {
            super(view);
            thumbnailUrl = (ImageView) itemView.findViewById(R.id.thumbnail);
        }
    }
    private class ChannelFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                List<Channel> tempList = new ArrayList<Channel>();
                String filter = constraint.toString().toLowerCase();
                // search content in friend list
                for (Channel channel : channels) {
                    if (channel.getName().toLowerCase().contains(filter) | channel.getKota().toLowerCase().contains(filter)) {
                        tempList.add(channel);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = channels.size();
                filterResults.values = channels;
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<Channel>) results.values;
            notifyDataSetChanged();
        }
    }
}

