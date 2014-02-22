package com.snowgears.chatterbox;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.snowgears.chatterbox.events.PlayerChatterEvent;
import com.snowgears.chatterbox.utils.Metrics;


public class Chatterbox extends JavaPlugin{
	
	private final ChatListener chatListener = new ChatListener(this);
	private static Chatterbox plugin;
	protected FileConfiguration config;  

	private static boolean usePerms = false;

	private static int radiusDefault = 0;
	private static int radiusYell = 0;
	
	private static int inviteExpireTime = 0;
	
	public void onEnable(){
		plugin = this;
		getServer().getPluginManager().registerEvents(chatListener, this);
		
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats
		}
		
		File configFile = new File(this.getDataFolder() + "/config.yml");

		if(!configFile.exists())
		{
		  this.saveDefaultConfig();
		}
		
		usePerms = getConfig().getBoolean("usePermissions");

		radiusDefault = getConfig().getConfigurationSection("ChatRadius").getInt("default");
		radiusYell = getConfig().getConfigurationSection("ChatRadius").getInt("yell");
		
		inviteExpireTime = getConfig().getInt("inviteExpireTime")*1200; //*1200 for minutes

	}
	
	public void onDisable(){
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if(args.length > 0){
			if (cmd.getName().equalsIgnoreCase("yell") || cmd.getName().equalsIgnoreCase("y")) {
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.use.yell")){
						String message = cutFirstWords(0, args);
						ArrayList<Player> recipients = new ArrayList<Player>();
						recipients.add(player);
						for(Entity e : player.getNearbyEntities(radiusYell, radiusYell, radiusYell)){
							if( e instanceof Player)
								recipients.add((Player)e);
						}
						PlayerChatterEvent e = new PlayerChatterEvent(player, ChatType.YELL, message, recipients);
						Bukkit.getServer().getPluginManager().callEvent(e);
						System.out.println("ChatterboxEvent called yell command.");
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else
					sender.sendMessage(ChatColor.DARK_RED+"That command can only be used in-game.");
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("reply") || cmd.getName().equalsIgnoreCase("r")) {
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.use.whisper")){
						String message = cutFirstWords(0, args);
						chatListener.reply(player, message);
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else
					sender.sendMessage(ChatColor.DARK_RED+"That command can only be used in-game.");
				return true;
			}
		}
		if(args.length > 1){
			if (cmd.getName().equalsIgnoreCase("whisper") || cmd.getName().equalsIgnoreCase("w")) { 
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.use.whisper")){
						if(Bukkit.getPlayer(args[0]) == null){ //check that 1st argument is a player that is online
							player.sendMessage(ChatColor.RED+"There is no player with that name online.");
							player.sendMessage(ChatColor.GRAY+"/whisper <player> <message>.");
							return true;
						}
						Player recipient = Bukkit.getPlayer(args[0]);
						String message = cutFirstWords(1, args);
						ArrayList<Player> recipients = new ArrayList<Player>();
						recipients.add(recipient);
						recipients.add(player);
						PlayerChatterEvent e = new PlayerChatterEvent(player, ChatType.WHISPER, message, recipients);
						Bukkit.getServer().getPluginManager().callEvent(e);
						System.out.println("ChatterboxEvent called whisper command.");
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else{
					//TODO cannot create new chatterbox event because it requires player
					//instead just whisper to player and make replying impossible
				}
				return true;
			}
		}
		if(args.length == 1){
			if (cmd.getName().equalsIgnoreCase("channel") && args[0].equalsIgnoreCase("list")) { 
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.use.channel")){
						if(chatListener.allChannels.isEmpty()){
							player.sendMessage(ChatColor.GRAY+"Currently there are no active channels.");
							if(player.hasPermission("chatterbox.create.channel"))
								player.sendMessage(ChatColor.GRAY+"To create a channel type '/channel create'.");
						}
						else{ //there are active channels open
							player.sendMessage(ChatColor.BOLD+"Current Active Channels:");
							player.sendMessage(ChatColor.GRAY+"To join a channel type '/channel join <#>'");
							player.sendMessage(ChatColor.WHITE+"--------------------------------------------------");
							for(ChatChannel cc : chatListener.allChannels){
								if(cc.getPrivacy())
									player.sendMessage(ChatColor.GREEN+"Channel: "+ChatColor.GRAY+cc.getChannel()+", "+ChatColor.GOLD+"Owner: "+ChatColor.GRAY+cc.getOwner()+", "+ChatColor.LIGHT_PURPLE+"Players: "+ChatColor.GRAY+cc.getParticipants().size()+", "+ChatColor.YELLOW+"Status: "+ChatColor.RED+"private");
								else
									player.sendMessage(ChatColor.GREEN+"Channel: "+ChatColor.GRAY+cc.getChannel()+", "+ChatColor.GOLD+"Owner: "+ChatColor.GRAY+cc.getOwner()+", "+ChatColor.LIGHT_PURPLE+"Players: "+ChatColor.GRAY+cc.getParticipants().size()+", "+ChatColor.YELLOW+"Status: "+ChatColor.GREEN+"public");
							}
						}
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else{
					sender.sendMessage(ChatColor.GREEN+"Current Active Channels:");
					for(ChatChannel cc : chatListener.allChannels){
						if(cc.getPrivacy())
							sender.sendMessage(ChatColor.RED+""+cc.getChannel()+ChatColor.GOLD+": Owner: "+cc.getOwner()+ChatColor.YELLOW+", Players: "+cc.getParticipants().size());
						else
							sender.sendMessage(ChatColor.YELLOW+""+cc.getChannel()+ChatColor.GOLD+": Owner: "+cc.getOwner()+ChatColor.YELLOW+", Players: "+cc.getParticipants().size());
					}
				}
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("channel") && args[0].equalsIgnoreCase("create")) { 
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.create.channel")){
						ChatChannel cc = chatListener.createChannel(player, false);
						player.sendMessage(ChatColor.YELLOW+"You have created a public channel on channel "+cc.getChannel()+".");
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else{
					sender.sendMessage(ChatColor.RED+"This command can only be used in-game.");
				}
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("channel") && args[0].equalsIgnoreCase("leave")) { 
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.use.channel")){
						chatListener.removePlayerFromAllChannels(player, false);
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else{
					sender.sendMessage(ChatColor.RED+"This command can only be used in-game.");
				}
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("channel") && args[0].equalsIgnoreCase("accept")) { 
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.use.channel")){
						boolean success = chatListener.acceptChannelInvite(player);
						if(success == false)
							player.sendMessage(ChatColor.RED+"You have no pending channel invitations.");
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else{
					sender.sendMessage(ChatColor.RED+"This command can only be used in-game.");
				}
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("channel") && args[0].equalsIgnoreCase("decline")) { 
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.use.channel")){
						chatListener.declineChannelInvite(player);
						player.sendMessage(ChatColor.GRAY+"You have declined all pending channel invitations.");
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else{
					sender.sendMessage(ChatColor.RED+"This command can only be used in-game.");
				}
				return true;
			}
		}
		else if(args.length == 2){
			if (cmd.getName().equalsIgnoreCase("channel") && args[0].equalsIgnoreCase("invite") && args[1].length() > 0) { 
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.create.channel")){
						ChatChannel cc = chatListener.getChannelOfPlayer(player);
						if(cc == null){
							player.sendMessage(ChatColor.RED+"You are not in a channel.");
							if(player.hasPermission("chatterbox.create.channel"))
								player.sendMessage(ChatColor.GRAY+"To create a channel type '/channel create'.");
						}
						else{
							if(cc.getOwner().equals(player.getName())){
								if(Bukkit.getPlayer(args[1]) == null){ //check that 1st argument is a player that is online
									player.sendMessage(ChatColor.RED+"There is no player with that name online.");
									player.sendMessage(ChatColor.GRAY+"/channel invite <player>.");
									return true;
								}
								chatListener.invitePlayerToChannel(Bukkit.getPlayer(args[1]), cc);
							}
							else{
								if(cc.getPrivacy())
									player.sendMessage(ChatColor.RED+"Only the owner of the channel can invite people to the channel.");
								else{
									if(Bukkit.getPlayer(args[1]) == null){ //check that 1st argument is a player that is online
										player.sendMessage(ChatColor.RED+"There is no player with that name online.");
										player.sendMessage(ChatColor.GRAY+"/channel invite <player>.");
										return true;
									}
									chatListener.invitePlayerToChannel(Bukkit.getPlayer(args[1]), cc);
								}
							}
						}
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else{
					sender.sendMessage(ChatColor.RED+"This command can only be used in-game.");
				}
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("channel") && args[0].equalsIgnoreCase("kick") && args[1].length() > 0) { 
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.create.channel")){
						ChatChannel cc = chatListener.getChannelOfPlayer(player);
						if(cc == null){
							player.sendMessage(ChatColor.RED+"You are not in a channel.");
							if(player.hasPermission("chatterbox.create.channel"))
								player.sendMessage(ChatColor.GRAY+"To create a channel type '/channel create'.");
						}
						else{
							if(cc.getOwner().equals(player.getName())){
								if(Bukkit.getPlayer(args[1]) == null){ //check that 1st argument is a player that is online
									player.sendMessage(ChatColor.RED+"There is no player with that name online.");
									player.sendMessage(ChatColor.GRAY+"/channel kick <player>.");
									return true;
								}
								chatListener.removePlayerFromChannel(player, cc, true);
							}
							else{
								player.sendMessage(ChatColor.RED+"Only the owner of the channel can kick people from the channel.");
							}
						}
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else{
					sender.sendMessage(ChatColor.RED+"This command can only be used in-game.");
				}
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("channel") && args[0].equalsIgnoreCase("join") && args[1].length() > 0) { 
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.use.channel")){
						if(Bukkit.getPlayer(args[1]) == null){ //check that 1st argument is a player that is online
							if(isInteger(args[1])){
								chatListener.addPlayerToChannel(player, Integer.parseInt(args[1]));
							}
							else{
								player.sendMessage(ChatColor.RED+"There is no player with that name online.");
								player.sendMessage(ChatColor.GRAY+"/channel join <player>.");
								player.sendMessage(ChatColor.GRAY+"To see a list of channels, type '/channel list'.");
							}
							return true;
						}
						else{ //trying to join channel of online player
							Player friend = Bukkit.getPlayer(args[1]);
							ChatChannel cc = chatListener.getChannelOfPlayer(friend);
							if(cc == null){
								player.sendMessage(ChatColor.RED+"That player is not currently in a channel.");
								player.sendMessage(ChatColor.GRAY+"To see a list of channels, type '/channel list'.");
							}
							else
								chatListener.addPlayerToChannel(player, cc);
						}
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else{
					sender.sendMessage(ChatColor.RED+"This command can only be used in-game.");
				}
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("channel") && args[0].equalsIgnoreCase("privacy") && args[1].equalsIgnoreCase("toggle")) { 
				if(sender instanceof Player){
					Player player = (Player)sender;
					if(usePerms == false || player.hasPermission("chatterbox.create.channel")){
						ChatChannel cc = chatListener.getChannelOfPlayer(player);
						if(cc == null){
							player.sendMessage(ChatColor.RED+"You are not in a channel.");
							if(player.hasPermission("chatterbox.create.channel"))
								player.sendMessage(ChatColor.GRAY+"To create a channel type '/channel create'.");
						}
						else{
							if(cc.getOwner().equals(player.getName())){
								Player p = Bukkit.getPlayer(cc.getOwner());
								boolean priv = cc.getPrivacy();
								if(priv){
									priv = false;
									p.sendMessage(ChatColor.YELLOW+"The privacy for the channel has been toggled "+ChatColor.RED+"OFF"+ChatColor.YELLOW+".");
								}
								else{
									priv = true;
									p.sendMessage(ChatColor.YELLOW+"The privacy for the channel has been toggled "+ChatColor.GREEN+"ON"+ChatColor.YELLOW+".");
								}
								cc.setPrivacy(priv);
							}
							else{
								player.sendMessage(ChatColor.RED+"Only the owner of the channel can kick people from the channel.");
							}
						}
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
				else{
					sender.sendMessage(ChatColor.RED+"This command can only be used in-game.");
				}
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("chat") && args[0].equalsIgnoreCase("head") && args[1].length() > 0) { 
				for(Player p : getServer().getOnlinePlayers()){
					chatListener.displayMessageOverPlayersHead(p, args[1], ChatColor.GOLD);
				}
			}
		}
		return false;
    }
	
    private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
    
    private String cutFirstWords(int c, String[] onSpaces){
		String message = "";
		if(onSpaces.length > c){
			for(int i=c; i<onSpaces.length; i++){
				message = message + onSpaces[i] + " ";
			}
		}
		else
			message = onSpaces[c];
		return message;
    }

	public ChatListener getChatListener(){
		return chatListener;
	}
	
	public static Chatterbox getPlugin(){
		return plugin;
	}
	
	public boolean usePerms(){
		return usePerms;
	}
	
	public int getDefaultChatRadius(){
		return radiusDefault;
	}
	
	public int getYellingChatRadius(){
		return radiusYell;
	}
	
	public int getInviteExpireTime(){
		return inviteExpireTime;
	}
}