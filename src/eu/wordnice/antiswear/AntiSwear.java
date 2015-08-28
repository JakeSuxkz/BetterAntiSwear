/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>
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
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiSwear extends JavaPlugin implements Listener {
	
	/**
	 * Pattern to remove diacritics
	 */
	public static final Pattern DIACRITICS =
			Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
	
	/**
	 * Replace chars
	 */
	public static char[] REPLACES = new char[] {
		'Ł', 'l',
		'ł', 'l',
		'ß', 's',
		'æ', 'a',
		'ø', 'o',
		'©', 'c',
		'Ð', 'd',
		'ð', 'd',
		'Đ', 'd',
		'đ', 'd',
		'Ɖ', 'd',
		'ɖ', 'd',
		'Þ', 't',
		'þ', 't'
	};
	
	/**
	 * Blacklist - Disallowed words
	 * Format of every char[][2] elements:
	 *   { swear, replace to, ... }
	 *  
	 * For example:
	 *   { "idiot", "id**t", "noob", "n**b", "duck", "d***" }
	 */
	public static char[][] BLACKLIST = null;
	
	/**
	 * Whitelist - Allowed words
	 * Contains only char
	 */
	public static char[][] WHITELIST = null;

	/**
	 * Use {@link AntiSwear#DIACRITICS} pattern to remove diacritics
	 * 
	 * @param cs String to process
	 * 
	 * @return Processed string
	 */
	public static String stripDiacritics(CharSequence cs) {
		String str = Normalizer.normalize(cs, Normalizer.Form.NFD);
		return DIACRITICS.matcher(str).replaceAll("");
	}
	
	/**
	 * Use {@link AntiSwear#REPLACES} list to replace unicode characters
	 * with custom
	 * 
	 * @param cs char[] to process
	 * @param off Offset of `cs`
	 * @param len Length of `cs`
	 */
	public static void stripNonDiacritics(char[] cs, int off, int len) {
		int i = off;
		len += off;
		for(; i < len; i++) {
			char cur = cs[i];
			for(int i2 = 0, n2 = AntiSwear.REPLACES.length; i2 < n2; i2++) {
				if(cur == AntiSwear.REPLACES[i2++]) {
					cur = AntiSwear.REPLACES[i2];
					break;
				}
			}
			cs[i] = cur;
		}
	}
	
	/**
	 * Replace character to any other easy comparable
	 * 
	 * @param c Character to replace
	 * 
	 * @return New character easy & well comparable
	 */
	public static char getSimiliar(char c) {
		c = Character.toLowerCase(c);
		switch(c) {	
			case 'o':
			case '0':
				return 'u';
				
			case 'k':
				return 'c';
			
			case 'l':
			case '1':
			case 'e':
			case '3':
			case '8':
			case '€':
				return 'i';
				
			case '4':
				return 'a';
				
			case 's':
			case '$':
			case '2':
			case '5':
			case '6':
			case '7':
			case '9':
				return 'z';
		}
		return c;
	}
	
	/**
	 * Make string readable for plugin and remove special and unicode characters
	 * 
	 * @see {@link AntiSwear#removeTypos(char[], char[], int, int, int[], int[])}
	 */
	public static String removeTypos(String in) {
		char[] inchars = in.toCharArray();
		char[] out = new char[inchars.length];
		int nevlen = AntiSwear.removeTypos(out, inchars, 0, in.length(), null);
		return new String(out, 0, nevlen);
	}
	
	/**
	 * Make string readable for plugin, but do not remove dots and spaces
	 * 
	 * @see {@link AntiSwear#removeTyposSafe(char[], char[], int, int, int[], int[])}
	 */
	public static String removeTyposSafe(String in) {
		char[] inchars = in.toCharArray();
		char[] out = new char[inchars.length];
		int nevlen = AntiSwear.removeTyposSafe(out, inchars, 0, in.length(), null);
		return new String(out, 0, nevlen);
	}
	
	/**
	 * Make string readable for plugin and remove special and unicode characters
	 * 
	 * @param out Output buffer
	 * @param bts Input buffer
	 * @param off Offset of input buffer
	 * @param len Length of input buffer
	 * @param indexes Index of character (pair with out parameter; may be null)
	 * 
	 * @return Length of new string (of `out` parameter)
	 */
	public static int removeTypos(char[] out, char[] bts, int off, int len, int[] indexes) {
		len += off;
		int cur = off;
		int outi = 0;
		int prev = -1;
		for(; cur < len; cur++) {
			int c = AntiSwear.getSimiliar(bts[cur]);
			if(c != prev && (Character.isLetter(c) || Character.isDigit(c))) {
				prev = c;
				if(indexes != null) {
					indexes[outi] = cur;
				}
				out[outi++] = (char) c;
			}
		}
		return outi;
	}
	
	/**
	 * Make string readable for plugin, but do not remove dots and spaces
	 * 
	 * @param out Output buffer
	 * @param bts Input buffer
	 * @param off Offset of input buffer
	 * @param len Length of input buffer
	 * @param indexes Index of character (pair with out parameter)
	 * 
	 * @return Length of new string (of `out` parameter)
	 */
	public static int removeTyposSafe(char[] out, char[] bts, int off, int len, int[] indexes) {
		len += off;
		int cur = off;
		int outi = 0;
		int prev = -1;
		for(; cur < len; cur++) {
			int c = AntiSwear.getSimiliar(bts[cur]);
			if(c != prev) {
				prev = c;
				if(indexes != null) {
					indexes[outi] = cur;
				}
				out[outi++] = (char) c;
			}
		}
		return outi;
	}
	
	/**
	 * Equals method for char array
	 * 
	 * @param str1 String one
	 * @param off1 Offset of string one
	 * @param str2 String two
	 * @param off2 Offset of string two
	 * @param len Length to compare
	 * 
	 * @see {@link String#equals(Object)}
	 * 
	 * @return `true` If strings are too same
	 */
	public static boolean equals(char[] str1, int off1, char[] str2, int off2, int len) {
		len += off1;
		for(; off1 < len; off1++, off2++) {
			if(str1[off1] != str2[off2]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Index of string (char array)
	 * 
	 * @param str1 String one to explore
	 * @param off1 Offset of string one
	 * @param len1 Length of string one
	 * @param str2 String two to find
	 * @param off2 Offset of string two
	 * @param len2 Length of string two
	 * 
	 * @see {@link String#indexOf(String)}
	 * 
	 * @return Index (offset) of string one, otherwise `-1` if not found.
	 */
	public static int indexOf(char[] str1, int off1, int len1, char[] str2, int off2, int len2) {
		if(len1 < len2) {
			return -1;
		}
		len1 += off1;
		len1 -= len2;
		for(; off1 <= len1; off1++) {
			if(AntiSwear.equals(str1, off1, str2, off2, len2)) {
				return off1;
			}
		}
		return -1;
	}
	
	/**
	 * Do replace on string `in`.
	 * 
	 * @param edited Edited string.
	 * @param edited_off Offset of `edited` string
	 * @param edited_len Length of `edited` string
	 * @param in Original string, in which will be bad words replaced
	 * @param in_off Offset of `in` string
	 * @param in_len Length of `in` string
	 * @param indexes Pair with `edited`. The last `indexes` parameter from
	 *        {@link AntiSwear#removeTypos(char[], char[], int, int, int[])}
	 * 
	 * @return If any swear was matched, array from `in` parameter is modified 
	 *         and is returned size of cenzored char arrray. Otherwise return `-1`
	 */
	public static String doReplace(char[] edited, int edited_off, int edited_len, 
			char[] in, int in_off, int in_len, int[] indexes) {
		edited_len += edited_off;
		in_len += in_off;
		boolean had = false;
		StringBuilder sb = new StringBuilder();
		int oelen = edited_len - 1; //- Minimal length of blacklisted word
		int startfrom = 0;
		for(int oeoff = edited_off; oeoff < oelen; oeoff++) {
			for(int i1 = 0, n1 = AntiSwear.BLACKLIST.length; i1 < n1;) {
				char[] swear = AntiSwear.BLACKLIST[i1++];
				if(oeoff > (oelen - swear.length + 1)) {
					i1++;
					continue;
				}
				char[] replaceto = AntiSwear.BLACKLIST[i1++];
				if(AntiSwear.equals(edited, oeoff, swear, 0, swear.length)) {
					int start = indexes[oeoff];
					int end = indexes[oeoff + swear.length - 1];
					
					char end_char = AntiSwear.getSimiliar(in[end]);
					while(true) {
						if(AntiSwear.getSimiliar(in[end]) == end_char) {
							end++;
							if(end >= in_len) {
								end = in_len - 1;
								break;
							}
						} else {
							end--;
							break;
						}
					}
						
					int wh_index = -1;
					int wh_len = 0;
					for(int i2 = 0, n2 = AntiSwear.WHITELIST.length; i2 < n2; i2++) {
						char[] wh = AntiSwear.WHITELIST[i2];
						wh_len = wh.length;
						if(wh.length <= swear.length) {
							break;
						}
						int chlen = wh_len - 1;
						int choff = start - chlen;
						if(choff < in_off) {
							choff = in_off;
						}
						chlen = (chlen * 2) + wh_len;
						if((chlen + choff) > in_off) {
							chlen = in_len - in_off - choff;
						}
						wh_index = AntiSwear.indexOf(in, choff, chlen, wh, 0, wh_len);
						if(wh_index != -1) {
							break;
						}
					}
					if(wh_index != -1) {
						continue;
					}
					if(startfrom != start) {
						sb.append(in, startfrom, (start - startfrom));
					}
					sb.append(replaceto);
					startfrom = end + 1;
					oeoff += swear.length - 1;
					had = true;
				}
			}
		}
		if(!had) {
			return null;
		}
		sb.append(in, startfrom, (in_len - startfrom));
		return sb.toString();
	}
	
	/**
	 * High-level process string. Returns processed string with replaced
	 * swears. Make sure you initialized second parameter, and check it if needed.
	 * 
	 * @param in Input string
	 * @param mini If not null and length >= 1, first value is filled with minimalized string
	 * 
	 * @return If swear(s) were found, returns new modified string. Otherwise returns `null`
	 */
	public static String processString(String in, String[] mini) {
		in = AntiSwear.stripDiacritics(in);
		char[] chars = in.toCharArray();
		char[] out = new char[chars.length];
		int[] indexes = new int[chars.length];
		AntiSwear.stripNonDiacritics(chars, 0, chars.length);
		int len = AntiSwear.removeTypos(out, chars, 0, chars.length, indexes);
		if(mini != null && mini.length >= 1) {
			mini[0] = new String(out, 0, len);
		}
		return AntiSwear.doReplace(out, 0, len, chars, 0, chars.length, indexes);
	}
	
	/**
	 * Sort string array - longer strings first. Do not pass
	 * `null` strings
	 * 
	 * @param arr Array of strings to sort
	 */
	public static void sort(String[] arr) {
		Arrays.sort(arr, new Comparator<String>() {

			@Override
			public int compare(String str1, String str2) {
				return str2.length() - str1.length();
			}
			
		});
	}
	
	/**
	 * Sort array of char arrays - longer first. Do not pass
	 * `null` arrays
	 * 
	 * @param arr Array to sort
	 */
	public static void sort(char[][] arr) {
		Arrays.sort(arr, new Comparator<char[]>() {

			@Override
			public int compare(char[] str1, char[] str2) {
				return str2.length - str1.length;
			}
			
		});
	}
	
	
	/******************
	 * JAVAPLUGIN AREA
	 * LISTENER AREA
	 */
	
	/**
	 * Block message with swear
	 */
	public boolean blockSwear = false;
	
	/**
	 * Allow Operators to swear
	 */
	public boolean allowOpSwear = false;
	
	/**
	 * Message sent to player on swear (may be `null` = no message)
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
		List<String> whitelist = cfg.getStringList("Whitelist");
		if(whitelist != null && whitelist.size() > 0) {
			ListIterator<String> it = whitelist.listIterator();
			int n = whitelist.size();
			int i = 0;
			char[][] msgs = new char[n][];
			while(it.hasNext()) {
				String val = it.next();
				if(val != null) {
					msgs[i++] = val.toLowerCase().toCharArray();
				}
			}
			if(i != msgs.length) {
				msgs = Arrays.copyOf(msgs, i);
			}
			Arrays.sort(msgs);
			AntiSwear.WHITELIST = msgs;
			this.getLogger().info("Loaded " + i + " whitelisted messages!");
		} else {
			AntiSwear.WHITELIST = new char[0][];
			this.getLogger().info("Loaded no whitelisted messages!");
		}
		
		/*
		 * Load blacklist
		 */
		List<Map<?,?>> vals = cfg.getMapList("Blacklist");
		if(vals == null || vals.size() == 0) {
			this.getLogger().severe("Missing blacklisted words!");
			AntiSwear.BLACKLIST = new char[0][];
		} else {
			ListIterator<Map<?, ?>> it = vals.listIterator();
			while(it.hasNext()) {
				Iterator<? extends Entry<?, ?>> mapit = it.next().entrySet().iterator();
				if(!mapit.hasNext()) {
					it.remove();
					continue;
				}
				Entry<?,?> ent = mapit.next();
				if(ent.getKey() == null || ent.getValue() == null) {
					it.remove();
					continue;
				}
			}
			Collections.sort(vals, new Comparator<Map<?, ?>>() {

				@Override
				public int compare(Map<?, ?> map1, Map<?, ?> map2) {
					Iterator<? extends Entry<?, ?>> it1 = map1.entrySet().iterator();
					Iterator<? extends Entry<?, ?>> it2 = map2.entrySet().iterator();
					if(!it1.hasNext() || !it2.hasNext()) {
						return -1;
					}
					return AntiSwear.removeTypos(it2.next().getKey().toString()).length() 
							- AntiSwear.removeTypos(it1.next().getKey().toString()).length();
				}
				
			});
			
			it = vals.listIterator();
			int n = vals.size() * 2;
			int i = 0;
			char[][] out = new char[n][];
			while(it.hasNext() && i < n) {
				Map<?, ?> map = it.next();
				Iterator<? extends Entry<?, ?>> mapit = map.entrySet().iterator();
				if(!mapit.hasNext()) {
					continue;
				}
				Entry<?, ?> ent = mapit.next();
				out[i++] = AntiSwear.removeTypos(ent.getKey().toString()).toCharArray();
				out[i++] = ChatColor.translateAlternateColorCodes('&', ent.getValue().toString()).toCharArray();
			}
			if(i != n) {
				out = Arrays.copyOf(out, i);
			}
			AntiSwear.BLACKLIST = out;
			this.getLogger().info("Loaded " + (i / 2) + " blacklisted messages!");
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
		this.getLogger().info("BetterAntiSwear by wordnice was enabled!");
	}
	
	/**
	 * @param event Event
	 */
	@EventHandler(ignoreCancelled=true)
	public void onChat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		if((this.allowOpSwear && p.isOp()) || p.hasPermission("BetterAntiSwear.Swear")) {
			return;
		}
		String nevmsg = AntiSwear.processString(event.getMessage(), null);
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
	
}
