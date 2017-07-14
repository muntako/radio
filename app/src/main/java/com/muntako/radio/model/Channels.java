package com.muntako.radio.model;

import com.muntako.radio.model.Channel;

import java.util.ArrayList;

/**
 * Created by akhmadmuntako on 22/09/2016.
 */
public class Channels {
    private ArrayList<Channel> radios;

    public Channels(ArrayList<Channel> channels) {
        this.radios = channels;

    }

    public ArrayList<Channel> getChannels() {
        return radios;
    }

    public void setChannels(ArrayList<Channel> channels) {
        this.radios = channels;
    }
    public Channel getChannelByName(String name){
        Channel radio = null;
        for(Channel channel : radios){
            if(channel.getName().equalsIgnoreCase(name)){
                radio = channel;
            }
        }
        return radio;
    }
}
