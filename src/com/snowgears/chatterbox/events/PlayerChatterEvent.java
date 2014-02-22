package com.snowgears.chatterbox.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.snowgears.chatterbox.ChatType;

public class PlayerChatterEvent extends Event implements Cancellable{

	 private static final HandlerList handlers = new HandlerList();
	    private Player player;
	    private List<Player> recipients = new ArrayList<Player>();
	    private ChatType type;
	    private String message;
	    private String prefix;
	    private boolean cancelled = false;
	    
		public PlayerChatterEvent(Player p, ChatType c, String m, ArrayList<Player> others) {
			player = p;
			type = c;
			message = m;
			prefix = ChatColor.GRAY+"["+player.getName()+"]";
			recipients = others;
			recipients.add(player);
	    }
		
		public Player getPlayer(){
			return player;
		}
		
		public List<Player> getRecipients(){
			return recipients;
		}
		
		public void setRecipients(ArrayList<Player> players){
			recipients = players;
		}
		
		public void addRecipient(Player p){
			if(!recipients.contains(p))
				recipients.add(p);
		}
		
		public void removeRecipient(Player p){
			if(recipients.contains(p))
				recipients.remove(p);
		}
	 
	    public ChatType getChatType() {
	        return type;
	    }
	    
	    public void setChatType(ChatType c) {
	        type = c;
	    }

	    public String getMessage(){
	    	return message;
	    }
	    
	    public void setMessage(String s){
	    	message = s;
	    }
	    
	    public String getPrefix(){
	    	return prefix;
	    }
	    
	    public void setPrefix(String s){
	    	prefix = s;
	    }

	    public HandlerList getHandlers() {
	        return handlers;
	    }
	 
	    public static HandlerList getHandlerList() {
	        return handlers;
	    }

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public void setCancelled(boolean set) {
			cancelled = set;
		}
}
