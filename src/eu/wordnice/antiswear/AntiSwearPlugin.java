/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015, Dalibor DrgoĹ� <emptychannelmc@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.wordnice.antiswear;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiSwearPlugin extends JavaPlugin implements Listener {
	
	/**
	 * Prefix of plugin
	 */
	public static String PREFIX = (ChatColor.GREEN + "[Better4nti5we*r] " + ChatColor.AQUA);
	
	/**
	 * Last known instance of AntiSwearPlugin 
	 * loaded by Bukkit/Spigot server
	 */
	protected static AntiSwearPlugin LAST;
	
	/**
	 * @return Last known instance of AntiSwearPlugin loaded 
	 *        by Bukkit/Spigot server or {@code null}
	 */
	public static AntiSwearPlugin getLast() {
		return LAST;
	}
	
	/**
	 * AntiSwear instance for this plugin
	 */
	public AntiSwear aswear = null;
	
	/**
	 * @return AntiSwear instance for this plugin
	 */
	public AntiSwear getAntiSwear() {
		return this.aswear;
	}
	
	/**
	 * Block message with swear
	 */
	public boolean blockSwear = false;
	
	/**
	 * Allow Operators to swear
	 */
	public boolean allowOpSwear = false;
	
	/**
	 * Message sent to player on swear (may beÂ `null` = no message)
	 */
	public String swearMessage = null;
	
	/**
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		if(!new File(this.getConfig().getCurrentPath()).exists()) {
			this.saveDefaultConfig();
		}
		ConfigurationSection cfg = this.getConfig();
		
		this.aswear = new AntiSwear();
		AntiSwear.LAST = this.aswear;
		
		this.blockSwear = cfg.getBoolean("BlockSwear", false);
		this.allowOpSwear = cfg.getBoolean("AllowOPSwear", false);
		this.swearMessage = cfg.getString("SwearMessage", null);
		if(this.swearMessage == null || this.swearMessage.length() == 0 || this.swearMessage.equalsIgnoreCase("null")) {
			this.swearMessage = null;
		} else {
			this.swearMessage = ChatColor.translateAlternateColorCodes('&', this.swearMessage);
		}
		
		/*
		 * Load Whitelist
		 */
		this.aswear.WHITELIST = AntiSwear.loadWhitelist(cfg);
		if(this.aswear.WHITELIST == null) {
			this.aswear.WHITELIST = new char[0][];
			this.getLogger().info("Loaded no whitelisted messages!"
					+ "(section 'Whitelist' in '" + cfg.getCurrentPath() + "')!");
		} else {
			this.getLogger().info("Loaded " + this.aswear.WHITELIST.length + " whitelisted messages!");
		}
		
		/*
		 * Load blacklist
		 */
		this.aswear.BLACKLIST = AntiSwear.loadBlacklist(cfg);
		if(this.aswear.BLACKLIST == null) {
			this.getLogger().severe("Missing blacklisted words "
					+ "(section 'Blacklist' in '" + cfg.getCurrentPath() + "')!");
			this.aswear.BLACKLIST = new char[0][];
		} else {
			this.getLogger().info("Loaded " 
				+ (this.aswear.BLACKLIST.length / 2) + " blacklisted messages!");
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
		this.getCommand("antiswear").setExecutor(this);
		this.getLogger().info("BetterAntiSwear by wordnice was enabled!");
	}
	
	/**
	 * @param event Event
	 */
	@EventHandler(ignoreCancelled=true)
	public void onChat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		if(p == null || (this.allowOpSwear && p.isOp()) || p.hasPermission("BetterAntiSwear.Swear")) {
			return;
		}
		String nevmsg = this.aswear.processString(event.getMessage(), null);
		if(nevmsg != null) {
			if(this.swearMessage != null) {
				p.sendMessage(this.swearMessage);
			}
			if(this.blockSwear) {
				event.setCancelled(true);
			} else {
				event.setMessage(nevmsg);
			}
		}
	}
	
	/**
	 * Called on command
	 * See {@link JavaPlugin#onCommand(CommandSender, Command, String, String[])}
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String typed, String[] args) {
		if(args.length >= 1 && args[0].equalsIgnoreCase("test") 
				&& (sender.isOp() || sender.hasPermission("BetterAntiSwear.Test"))) {
			String msg = "";
			for(int i = 1, n = args.length; i < n; i++) {
				if(i != 1) {
					msg += " ";
				}
				msg += args[i];
			}
			long start = System.nanoTime();
			String[] mini = new String[1];
			String process = this.aswear.processString(msg, mini);
			sender.sendMessage(new String[] {
				(PREFIX + msg),
				(ChatColor.YELLOW + "" + ChatColor.ITALIC + "" + (System.nanoTime() - start) 
						 + " ns, processed: " + ChatColor.YELLOW + mini[0]),
				((process == null) ? (ChatColor.GREEN + msg) : (ChatColor.RED + process)),
			});
			return true;
		} else if(sender.isOp() || sender.hasPermission("BetterAntiSwear.Test")) {
			sender.sendMessage(PREFIX + "/" + typed + " test <message>");
			return true;
		}
		return false;
	}
	
}
