package com.snowgears.chatterbox.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.snowgears.chatterbox.ChatChannel;

public class PlayerCreateChannelEvent extends Event implements Cancellable{

	private static final HandlerList handlers = new HandlerList();
	
	private Player player;
	private ChatChannel channel;
	private boolean isCancelled;
	
	public PlayerCreateChannelEvent(Player p, ChatChannel cc){
		player = p;
		channel = cc;
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public ChatChannel getChannel(){
		return channel;
	}
	
	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		isCancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
