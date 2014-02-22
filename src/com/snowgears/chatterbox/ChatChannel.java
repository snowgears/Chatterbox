package com.snowgears.chatterbox;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatChannel{

	private String owner;
	private int channel;
	private boolean isPrivate;
	private ArrayList<String> participants = new ArrayList<String>();
	
	public ChatChannel(Player starter, int c){
		owner = starter.getName();
		channel = c;
		isPrivate = false;
	}
	
	public ChatChannel(Player starter, int c, boolean p){
		owner = starter.getName();
		channel = c;
		isPrivate = p;
		participants.add(owner);
	}
	
	public void broadcastMessage(String message){
		for(String s : participants){
			if(Bukkit.getPlayer(s) != null)
				Bukkit.getPlayer(s).sendMessage(message);
		}
	}
	
	public String getOwner(){
		return owner;
	}
	
	public void setOwner(String s){
		owner = s;
	}
	
	public int getChannel(){
		return channel;
	}
	
	public boolean isPrivate(){
		return isPrivate;
	}
	
	public void setPrivacy(boolean p){
		isPrivate = p;
	}
	
	public ArrayList<String> getParticipants(){
		return participants;
	}
	
	public void addParticipant(Player player){
		if(!participants.contains(player.getName())){
			participants.add(player.getName());
		}
	}
	
	public void removeParticipant(Player player){
		if(participants.contains(player.getName())){
			participants.remove(player.getName());
		}
	}
	
	public void clearParticipants(){
		participants.clear();
	}
}
