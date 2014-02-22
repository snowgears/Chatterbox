package com.snowgears.chatterbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.snowgears.chatterbox.events.PlayerChatterEvent;
import com.snowgears.chatterbox.events.PlayerCreateChannelEvent;

public class ChatListener implements Listener{

	private Chatterbox plugin = Chatterbox.getPlugin();

	private HashMap<Integer, ChatChannel> channelsByID = new HashMap<Integer, ChatChannel>(); //channel id, associated channel
	private HashMap<String, ChatChannel> channelsByPlayer = new HashMap<String, ChatChannel>(); //player, associated channel
	private HashMap<String, List<ChatChannel>> playersInvitedToChannels = new HashMap<String, List<ChatChannel>>(); //player, list of channel invites that are pending
	private HashMap<String, ChatType> lockedChatType = new HashMap<String, ChatType>(); //player, chatType

	public ChatListener(Chatterbox instance) {
		plugin = instance;
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onChatter(PlayerChatterEvent event){
		if(event.isCancelled())
			return;
		Player player = event.getPlayer();
		System.out.println("Player talking: "+player.getName());
		String message = event.getMessage();
		String prefix = event.getPrefix();
//		
//		if(event.getChatType() == ChatType.WHISPER){
//			if(Chatterbox.headWhisper){
//				displayMessageOverPlayersHead(player, message, ChatColor.LIGHT_PURPLE);
//			}
//			message = ChatColor.LIGHT_PURPLE+message;
//			if(event.getRecipients().contains(player)){
//				event.removeRecipient(player);
//				player.sendMessage(ChatColor.GRAY+"[You -> "+event.getRecipients().get(0).getName() +"] "+message);
//			}
//			replyPlayers.put(player.getName(), event.getRecipients().get(0).getName());
//			prefix = ChatColor.GRAY+"["+event.getPrefix()+ChatColor.GRAY+" -> You] ";
//		}
//		else if(event.getChatType() == ChatType.DEFAULT){
//			message = ChatColor.WHITE+message;
//			if(Chatterbox.headDefault){
//				displayMessageOverPlayersHead(player, message, ChatColor.WHITE);
//			}
//		}
//		else if(event.getChatType() == ChatType.YELL){
//			message = ChatColor.RED+message;
//			if(Chatterbox.headYell){
//				displayMessageOverPlayersHead(player, message, ChatColor.RED);
//			}
//		}
//		else if(event.getChatType() == ChatType.WORLD){
//			message = ChatColor.AQUA+message;
//			if(Chatterbox.headWorld){
//				displayMessageOverPlayersHead(player, message, ChatColor.AQUA);
//			}
//		}
//		else if(event.getChatType() == ChatType.SERVER){
//			message = ChatColor.YELLOW+message;
//			if(Chatterbox.headServer){
//				displayMessageOverPlayersHead(player, message, ChatColor.YELLOW);
//			}
//		}
//		else if(event.getChatType() == ChatType.GLOBAL){
//			message = ChatColor.GOLD+message; //TODO will need to send plugin message with bungeecord for this
//			if(Chatterbox.headGlobal){
//				displayMessageOverPlayersHead(player, message, ChatColor.GOLD);
//			}
//		}
//		else if(event.getChatType() == ChatType.CHANNEL){
//			if(Chatterbox.headChannel){
//				displayMessageOverPlayersHead(player, message, ChatColor.GRAY);
//			}
//		}
		
		if(event.getRecipients().contains(player))
			event.removeRecipient(player);
			
		String fullMessage = prefix+" "+message;
		for(Player p : event.getRecipients()){
			p.sendMessage(fullMessage);
		}
	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event){
		removePlayerFromChannel(event.getPlayer(), false);
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent event){
		removePlayerFromChannel(event.getPlayer(), true);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDefaultChat(AsyncPlayerChatEvent event){
	
		event.setCancelled(true);
		
		Player player = event.getPlayer();		
		String message = event.getMessage();
		ChatType type = getLockedChatType(player);
		
		ArrayList<Player> recipients = new ArrayList<Player>();
		recipients.add(player);
		
		if(type == ChatType.DEFAULT){
			for(Entity e : player.getNearbyEntities(plugin.getDefaultChatRadius(), plugin.getDefaultChatRadius(), plugin.getDefaultChatRadius())){
				if( e instanceof Player)
					recipients.add((Player)e);
			}
			PlayerChatterEvent e = new PlayerChatterEvent(player, ChatType.DEFAULT, message, recipients);
			Bukkit.getServer().getPluginManager().callEvent(e);
			System.out.println("ChatterboxEvent called default.");
		}
		else if(type == ChatType.YELL){
			for(Entity e : player.getNearbyEntities(plugin.getYellingChatRadius(), plugin.getYellingChatRadius(), plugin.getYellingChatRadius())){
				if( e instanceof Player)
					recipients.add((Player)e);
			}
			PlayerChatterEvent e = new PlayerChatterEvent(player, ChatType.YELL, message, recipients);
			Bukkit.getServer().getPluginManager().callEvent(e);
			System.out.println("ChatterboxEvent called yell.");
		}
//		else if(type == ChatType.WHISPER){
//			boolean success = reply(player, message);
//			if(success == false)
//				player.sendMessage(ChatColor.GRAY+"You are still locked in whisper chat. To unlock, type '/chat unlock'.");
//		}
		//TODO do all other chat types
		return;
	}
	
	public ChatChannel createChannel(Player p, boolean isPrivate){
		Random rnd = new Random();
		int n = rnd.nextInt(1000-100) + 100; //This gives you a random number in between 100 (inclusive) and 1000 (exclusive)
		
		//make sure no two channels have the same id number
		while(channelsByID.containsKey(n)){
			n = rnd.nextInt(1000-100) + 100;
		}
		
		ChatChannel channel = new ChatChannel(p, n, isPrivate);
		
		PlayerCreateChannelEvent e = new PlayerCreateChannelEvent(p, channel);
		Bukkit.getServer().getPluginManager().callEvent(e);
		return channel;
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onChannelCreation(PlayerCreateChannelEvent event){
		if(event.isCancelled()){
			return;
		}
		ChatChannel cc = event.getChannel();
		channelsByID.put(cc.getChannel(), cc);
		//channelsByPlayer.put(event.getPlayer().getName(), cc);
		addPlayerToChannel(event.getPlayer(), cc.getChannel());
	}

	public void removePlayerFromChannel(Player player, boolean wasKicked){
		ChatChannel cc = null;
		if(channelsByPlayer.containsKey(player.getName()))
			cc = channelsByPlayer.remove(player.getName());
		if(cc == null)
			return;
		
		cc.removeParticipant(player);
		if(wasKicked){
			cc.broadcastMessage(ChatColor.GRAY+player.getName()+" was kicked from the channel.");
			player.sendMessage(ChatColor.GRAY+"You have been kicked from the chat channel.");
		}
		else{
			cc.broadcastMessage(ChatColor.GRAY+player.getName()+" has left the channel.");
			player.sendMessage(ChatColor.GRAY+"You have left the chat channel.");
		}
		
		//owner was the one that left the channel
		if(cc.getOwner().equalsIgnoreCase(player.getName())){
			//nobody is left in the channel after owner left
			if(cc.getParticipants().size() == 0){
				if(channelsByID.containsKey(cc.getChannel()))
					cc = channelsByID.remove(cc.getChannel());
			}
			//there are still players in the channel
			else{
				String newOwner = cc.getParticipants().get(0);
				cc.setOwner(newOwner);
				cc.broadcastMessage(ChatColor.GRAY+newOwner+" is now the owner of this channel.");
			}
		}	
	}
	
	//do not use this method for accepting a channel invite
	public void addPlayerToChannel(Player player, int channel){
		removePlayerFromChannel(player, false);
		ChatChannel cc = channelsByID.get(channel);
		if(cc == null)
			return;
		
		if(cc.isPrivate()){
			if(playersInvitedToChannels.containsKey(player.getName())){
				List<ChatChannel> channelsInvitedTo = playersInvitedToChannels.get(player.getName());
				if(!channelsInvitedTo.contains(cc))
					return;
				else
					playersInvitedToChannels.remove(player.getName());
				
				cc.addParticipant(player);
				cc.broadcastMessage(ChatColor.GRAY+player.getName()+" has joined the channel.");
			}
			else{
				if(Bukkit.getPlayer(cc.getOwner()) != null){
					player.sendMessage(ChatColor.RED+"You have tried joining a private channel without an invite. The owner have been notified.");
					Player owner = Bukkit.getPlayer(cc.getOwner());
					if(owner != null){
						owner.sendMessage(ChatColor.YELLOW+player.getName()+" tried to join your channel but was rejected because the channel is private.");
						owner.sendMessage(ChatColor.GRAY+"If you wish to invite them to your channel, type '/channel invite "+player.getName()+"'.");
					}
					return;
				}
			}
		}
		else{
			cc.addParticipant(player);
			cc.broadcastMessage(ChatColor.GRAY+player.getName()+" has joined the channel.");
			return;
		}
	}
	
	public void invitePlayerToChannel(final Player player, final ChatChannel cc){
		if(playersInvitedToChannels.containsKey(player.getName())){
			List<ChatChannel> channelsInvitedTo = playersInvitedToChannels.get(player.getName());
			//is not already on the invite list of that channel
			if(!channelsInvitedTo.contains(cc)){
				channelsInvitedTo.add(cc);
				playersInvitedToChannels.put(player.getName(), channelsInvitedTo);
			}
		}
		else{
			List<ChatChannel> channelsInvitedTo = new ArrayList<ChatChannel>();
			channelsInvitedTo.add(cc);
			playersInvitedToChannels.put(player.getName(), channelsInvitedTo);
		}

		player.sendMessage(ChatColor.GREEN+cc.getOwner()+" has invited you to join their channel!");
		player.sendMessage(ChatColor.GRAY+"To accept their invitation, type '/channel accept'.");
		player.sendMessage(ChatColor.GRAY+"To decline this, and all other pending invitations, type '/channel decline'.");
		
		if(plugin.getInviteExpireTime() != 0){
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				  public void run() {
				      //channel has not yet been deleted
					  if(cc != null){
						  if(playersInvitedToChannels.containsKey(player.getName())){ 
								List<ChatChannel> channelsInvitedTo = playersInvitedToChannels.get(player.getName());
								if(channelsInvitedTo.contains(cc)){
									channelsInvitedTo.remove(cc);
									playersInvitedToChannels.put(player.getName(), channelsInvitedTo);
									player.sendMessage(ChatColor.RED+"Your invitation to "+cc.getOwner()+"'s channel has expired.");
									if(!cc.isPrivate())
						    			  player.sendMessage(ChatColor.GRAY+"You can still join the channel by typing '/channel join "+cc.getChannel()+"'.");
								}
						  }
				      }
				  }
			}, plugin.getInviteExpireTime());
		}
	}
	
	public boolean acceptLastChannelInvite(Player player){
		if(playersInvitedToChannels.containsKey(player.getName())){
			List<ChatChannel> channelsInvitedTo = playersInvitedToChannels.get(player.getName());
			ChatChannel cc = channelsInvitedTo.get(channelsInvitedTo.size()-1);
			addPlayerToChannel(player, cc.getChannel());
			playersInvitedToChannels.remove(player.getName());
			return true;
		}
		return false;
	}
	
	public void declineAllChannelInvites(Player player){	
		if(playersInvitedToChannels.containsKey(player.getName())){
			List<ChatChannel> channelsInvitedTo = playersInvitedToChannels.get(player.getName());
			
			for(ChatChannel cc : channelsInvitedTo){
				Player owner = Bukkit.getPlayer(cc.getOwner());
				if(owner != null)
					owner.sendMessage(ChatColor.GRAY+player.getName()+" has declined his invitation to your channel.");
			}
			playersInvitedToChannels.remove(player.getName());
		}
	}
	
	public ChatType getLockedChatType(Player p){
		if(lockedChatType.containsKey(p.getName()))
			return lockedChatType.get(p.getName());
		return ChatType.DEFAULT;
	}
}