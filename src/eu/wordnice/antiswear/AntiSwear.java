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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class AntiSwear {
	
	/**
	 * Last known AntiSwear instance loaded 
	 * by Bukkit/Spigot server
	 */
	protected static AntiSwear LAST = null;
	
	/**
	 * @return Last known AntiSwear instance loaded 
	 *         by Bukkit/Spigot server or {@code null}
	 */
	public static AntiSwear getLast() {
		return LAST;
	}
	
	/**
	 * @param last To which instance will be set last known instance. Null ignored
	 */
	public static void setLast(AntiSwear last) {
		if(last != null) {
			AntiSwear.LAST = last;
		}
	}
	
	/**
	 * @return New instance with no blacklist nor whitelist
	 */
	public static AntiSwear getNew() {
		return new AntiSwear();
	}
	
	/**
	 * Pattern to remove diacritics
	 */
	public static final Pattern DIACRITICS =
			Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
	
	public static String WHITELIST_SPACE_EQUALS = ".,:;?! \t\n\r\f";
	
	/**
	 * Empty array
	 */
	protected static char[][] EMPTY = new char[0][];
	
	/**
	 * Blacklist - Disallowed words
	 * Format of every char[][2] elements:
	 *   { swear, replace to, ... }
	 *  
	 * For example:
	 *   { "idiot", "id**t", "noob", "n**b", "duck", "d***" }
	 */
	public char[][] BLACKLIST = null;
	
	/**
	 * Whitelist - Allowed words
	 * Contains only char
	 */
	public char[][] WHITELIST = null;
	
	
	/**
	 * Create instance with no blacklist nor whitelist
	 */
	public AntiSwear() {
		this.BLACKLIST = EMPTY;
		this.WHITELIST = EMPTY;
	}
	
	public void addWhitelist(ConfigurationSection sec) {
		this.WHITELIST = AntiSwear.contactSort(this.WHITELIST, AntiSwear.loadWhitelist(sec));
	}
	
	public void addWhitelist(Iterable<String> wh) {
		this.WHITELIST = AntiSwear.contactSort(this.WHITELIST, AntiSwear.loadWhitelist(wh));
	}
	
	public void addWhitelist(Iterator<String> wh) {
		this.WHITELIST = AntiSwear.contactSort(this.WHITELIST, AntiSwear.loadWhitelist(wh));
	}
	
	public void addWhitelist(String... strs) {
		this.WHITELIST = AntiSwear.contactSort(this.WHITELIST, AntiSwear.loadWhitelist(strs));
	}
	
	public void addWhitelistRaw(char[]... sorted) {
		this.WHITELIST = AntiSwear.contactSort(this.WHITELIST, sorted);
	}
	
	
	public void addBlacklist(String search, String replace) {
		this.BLACKLIST = AntiSwear.contactSortTwo(this.BLACKLIST, AntiSwear.loadBlacklist(search, replace));
	}
	
	public void addBlacklist(ConfigurationSection sec) {
		this.BLACKLIST = AntiSwear.contactSortTwo(this.BLACKLIST, AntiSwear.loadBlacklist(sec));
	}
	
	public void addBlacklist(Iterable<Map<?,?>> it) {
		this.BLACKLIST = AntiSwear.contactSortTwo(this.BLACKLIST, AntiSwear.loadBlacklist(it));
	}
	
	public void addBlacklist(Iterator<Map<?,?>> it) {
		this.BLACKLIST = AntiSwear.contactSortTwo(this.BLACKLIST, AntiSwear.loadBlacklist(it));
	}
	
	public void addBlacklist(Map<String,String> map) {
		this.BLACKLIST = AntiSwear.contactSortTwo(this.BLACKLIST, AntiSwear.loadBlacklist(map));
	}
	
	public void addBlacklistSorted(Map<char[],char[]> map) {
		this.BLACKLIST = AntiSwear.contactSortTwo(this.BLACKLIST, AntiSwear.loadBlacklistSorted(map));
	}
	
	public void addBlacklistRaw(char[]... sorted) {
		this.BLACKLIST = AntiSwear.contactSortTwo(this.BLACKLIST, sorted);
	}
	

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
	 * Replace character to any other easy comparable
	 * 
	 * @param c Character to replace
	 * 
	 * @return New character easy & well comparable
	 */
	public static char getSimiliar(char c) {
		c = Character.toLowerCase(c);
		switch(c) {
			case 'ø':
			case 'o':
			case '0':
			case 'v':
			case 'w':
			case 'u':
				return 'u';
				
			case 'k':
			case 'c':
			case '©':
				return 'c';
			
			case 'l':
			case '1':
			case 'Ł':
			case 'ł':
			case 'e':
			case '3':
			case '8':
			case '€':
			case 'y':
			case '4':
			case 'æ':
			case 'a':
			case 'i':
				return 'i';
				
			case 'ß':
			case 'b':
			case 'Þ':
			case 'þ':
				return 'b';
				
			case 'Ð':
			case 'ð':
			case 'Đ':
			case 'đ':
			case 'Ɖ':
			case 'ɖ':
			case 'd':
				return 'd';
				
			case 'z':
			case '$':
			case '2':
			case '5':
			case '6':
			case '7':
			case '9':
			case 's':
				return 's';
		}
		return c;
	}
	
	/**
	 * Make string readable for plugin and remove special and unicode characters
	 * - remove punctuation, hiccups and debt
	 * - remove similar characters
	 * 
	 * @see {@link AntiSwear#removeTyposSlang(char[], char[], int, int, int[], int[])}
	 */
	public static String removeTyposAll(String in) {
		char[] chars = AntiSwear.stripDiacritics(in).toCharArray();
		int nevlen = AntiSwear.removeTyposSlang(chars, chars, 0, in.length(), null, 0);
		return String.copyValueOf(chars, 0, nevlen);
	}
	
	/**
	 * Make string readable for plugin
	 * - remove similar characters excluding unicode
	 * 
	 * @see {@link AntiSwear#removeTyposSlang(char[], char[], int, int, int[], int[])}
	 */
	public static String removeTyposSlang(String in) {
		char[] chars = AntiSwear.stripDiacritics(in).toCharArray();
		int nevlen = AntiSwear.removeTyposSlang(chars, chars, 0, in.length(), null, 0);
		return String.copyValueOf(chars, 0, nevlen);
	}
	
	/**
	 * Make string readable for plugin and remove special and unicode characters
	 * 
	 * @param out Output buffer
	 * @param bts Input buffer
	 * @param off Offset of input buffer
	 * @param len Length of input buffer
	 * @param indexes Index of character (pair with out parameter; may be null)
	 * @param ind_off Offset of indexes parameter
	 * 
	 * @return Length of new string (of `out` parameter)
	 */
	public static int removeTyposSlang(char[] out, char[] bts, int off, int len, int[] indexes, int ind_off) {
		len += off;
		int cur = off;
		int outi = 0;
		int prev = -1;
		for(; cur < len; cur++) {
			int c = AntiSwear.getSimiliar(bts[cur]);
			if(Character.isLetter(c) || Character.isDigit(c)) {
				if(c == prev) {
					if(indexes != null) {
						indexes[ind_off + outi - 1] = cur;
					}
				} else {
					prev = c;
					if(indexes != null) {
						indexes[ind_off + outi] = cur;
					}
					out[outi++] = (char) c;
				}
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
	 * Case-insensitive equals method for char array
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
	public static boolean equalsIgnoreCaseWhitelist(char[] str1, int off1, char[] str2, int off2, int len) {
		len += off1;
		for(; off1 < len; off1++, off2++) {
			char c1 = str1[off1];
			char c2 = str2[off2];
			if(Character.isSpaceChar(c2)) {
				if(WHITELIST_SPACE_EQUALS.indexOf(c1) == -1) {
					return false;
				}
			} else if(Character.toLowerCase(str1[off1]) != Character.toLowerCase(str2[off2])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Case-insensitive index of string (char array)
	 * If space is found in str2 argument, str1 may
	 * contain space, comma or full stop
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
	public static int indexOfIgnoreCaseWhitelist(char[] str1, int off1, int len1, char[] str2, int off2, int len2) {
		if(len1 < len2) {
			return -1;
		}
		len1 += off1;
		len1 -= len2;
		for(; off1 <= len1; off1++) {
			if(AntiSwear.equalsIgnoreCaseWhitelist(str1, off1, str2, off2, len2)) {
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
	 *        {@link AntiSwear#removeTyposSlang(char[], char[], int, int, int[])}
	 * @param ind_off Offset of indexes parameter
	 * 
	 * @return If any swear was matched, array from `in` parameter is modified 
	 *         and is returned size of cenzored char arrray. Otherwise return `-1`
	 */
	public String doReplace(char[] edited, int edited_off, int edited_len, 
			char[] in, int in_off, int in_len, int[] indexes, int ind_off) {
		edited_len += edited_off;
		in_len += in_off;
		boolean had = false;
		StringBuilder sb = new StringBuilder();
		int oelen = edited_len - 1; //- Minimal length of blacklisted word
		int startfrom = 0;
		
		char[][] bl = this.BLACKLIST;
		char[][] wl = this.WHITELIST;
		
		for(int oeoff = edited_off; oeoff < oelen; oeoff++) {
			for(int i1 = 0, n1 = bl.length; i1 < n1;) {
				char[] swear = bl[i1++];
				if(oeoff > (oelen - swear.length + 1)) {
					i1++;
					continue;
				}
				char[] replaceto = bl[i1++];
				if(AntiSwear.equals(edited, oeoff, swear, 0, swear.length)) {
					int start = indexes[ind_off + oeoff];
					int end = indexes[ind_off + oeoff + swear.length - 1];
						
					int wh_index = -1;
					for(int i2 = 0, n2 = wl.length; i2 < n2; i2++) {
						char[] wh = wl[i2];
						
						int chlen = wh.length - 1;
						int choff = start - chlen;
						if(choff < in_off) {
							choff = in_off;
						}
						chlen = (chlen * 2) + swear.length;
						if((chlen + choff) > in_len) {
							chlen = in_len - in_off - choff;
						}
						wh_index = AntiSwear.indexOfIgnoreCaseWhitelist(in, choff, chlen, wh, 0, wh.length);
						if(wh_index != -1) {
							break;
						}
					}
					if(wh_index != -1) {
						continue;
					}
					if(start > startfrom) {
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
	 * @param in Message to check and process
	 * 
	 * @return If swear(s) were found, returns new modified string. Otherwise returns `null`
	 */
	public String processString(String in) {
		return this.processString(in, null);
	}
	
	/**
	 * @param in Message to check and process
	 * @param mini If not null and length >= 1, mini[0] is filled with minimalized string
	 * 
	 * @return If swear(s) were found, returns new modified string. Otherwise returns `null`
	 */
	public String processString(String in, String[] mini) {
		char[] chars_orig = new char[in.length() + 2];
		in.getChars(0, in.length(), chars_orig, 1);
		chars_orig[0] = ' ';
		chars_orig[chars_orig.length - 1] = ' ';
		
		in = AntiSwear.stripDiacritics(in);
		
		char[] chars = new char[in.length() + 2];
		in.getChars(0, in.length(), chars, 1);
		chars[0] = ' ';
		chars[chars.length - 1] = ' ';
		
		char[] out = new char[chars.length + 2];
		int[] indexes = new int[chars.length + 2];
		int len = AntiSwear.removeTyposSlang(out, chars, 0, chars.length, indexes, 0);
		if(mini != null && mini.length >= 1) {
			mini[0] = String.copyValueOf(out, 0, len);
		}
		String ret = this.doReplace(out, 0, len, chars_orig, 0, chars_orig.length, indexes, 0);
		if(ret != null) {
			return ret.substring(1, ret.length() - 1);
		}
		return null;
	}
	
	/**
	 * Sort string array - longer strings first. Do not pass
	 * `null` strings
	 * 
	 * @param arr Array of strings to sort
	 */
	public static String[] sort(String[] arr) {
		Arrays.sort(arr, new Comparator<String>() {

			@Override
			public int compare(String str1, String str2) {
				return str2.length() - str1.length();
			}
			
		});
		return arr;
	}
	
	/**
	 * Sort array of char arrays - longer first. Do not pass
	 * `null` arrays
	 * 
	 * @param arr Array to sort
	 */
	public static char[][] sort(char[][] arr) {
		Arrays.sort(arr, new Comparator<char[]>() {

			@Override
			public int compare(char[] str1, char[] str2) {
				return str2.length - str1.length;
			}
			
		});
		return arr;
	}
	
	/**
	 * Sort array of char arrays - longer first. Do not pass
	 * `null` arrays. Sort by key - every second value (0,2,4...)
	 * 
	 * @param arr Array to sort
	 */
	public static char[][] sortTwo(char[][] arr) {
		SortedMap<char[],char[]> ret = AntiSwear.getSortedBlacklistMap();
		for(int i = 0, n = arr.length; i < n;) {
			char[] key = arr[i++];
			char[] val = arr[i++];
			ret.put(key, val);
		}
		return AntiSwear.loadBlacklistSorted(ret);
	}
	
	public static SortedMap<char[], char[]> getSortedBlacklistMap() {
		return new TreeMap<char[],char[]>(new Comparator<char[]>(){
			@Override
			public int compare(char[] arg0, char[] arg1) {
				return ((arg1.length - arg0.length) < 0) ? -1 : 1;
			}
		});
	}
	
	public static SortedMap<char[], char[]> removeTypos(Iterator<Map<?,?>> it) {
		if(it == null || !it.hasNext()) {
			return null;
		}
		SortedMap<char[],char[]> nev = getSortedBlacklistMap();
		while(it.hasNext()) {
			Iterator<? extends Entry<?,?>> cit = it.next().entrySet().iterator();
			while(cit.hasNext()) {
				Entry<?,?> ent = cit.next();
				Object key = ent.getKey();
				Object val = ent.getValue();
				if(key == null || val == null) {
					continue;
				}
				nev.put(AntiSwear.removeTyposAll(key.toString()).toCharArray(), 
						ChatColor.translateAlternateColorCodes('&', val.toString()).toCharArray());
			}
		}
		return nev;
	}
	
	public static SortedMap<char[], char[]> removeTypos(Map<String,String> map) {
		if(map == null || map.size() == 0) {
			return null;
		}
		SortedMap<char[],char[]> nev = getSortedBlacklistMap();
		Iterator<Entry<String,String>> cit = map.entrySet().iterator();
		while(cit.hasNext()) {
			Entry<String,String> ent = cit.next();
			String key = ent.getKey();
			String val = ent.getValue();
			if(key == null || val == null) {
				continue;
			}
			nev.put(AntiSwear.removeTyposAll(key).toCharArray(), 
					ChatColor.translateAlternateColorCodes('&', val).toCharArray());
		}
		return nev;
	}
	
	public static SortedMap<char[], char[]> sort(Map<char[],char[]> map) {
		SortedMap<char[],char[]> nev = getSortedBlacklistMap();
		nev.putAll(map);
		return nev;
	}
	
	public static char[][] loadBlacklist(String search, String replace) {
		if(search == null || replace == null) {
			return null;
		}
		return new char[][] {
			AntiSwear.removeTyposAll(search).toCharArray(),
			ChatColor.translateAlternateColorCodes('&', replace).toCharArray()
		};
	}
	
	public static char[][] loadBlacklist(ConfigurationSection sec) {
		if(sec == null) {
			return null;
		}
		return AntiSwear.loadBlacklist(sec.getMapList("Blacklist"));
	}
	
	public static char[][] loadBlacklist(Iterable<Map<?,?>> vals) {
		if(vals == null) {
			return null;
		}
		return AntiSwear.loadBlacklist(vals.iterator());
	}
	
	public static char[][] loadBlacklist(Iterator<Map<?,?>> vals) {
		if(vals == null || !vals.hasNext()) {
			return null;
		}
		return AntiSwear.loadBlacklistSorted(AntiSwear.removeTypos(vals));
	}
	
	public static char[][] loadBlacklist(Map<String,String> vals) {
		if(vals == null || vals.size() == 0) {
			return null;
		}
		return AntiSwear.loadBlacklistSorted(AntiSwear.removeTypos(vals));
	}
	
	public static char[][] loadBlacklistSorted(Map<char[],char[]> map) {
		if(map == null || map.size() == 0) {
			return null;
		}
		Iterator<Entry<char[],char[]>> it = map.entrySet().iterator();
		int n = map.size() * 2;
		int i = 0;
		char[][] out = new char[n][];
		while(it.hasNext() && i < n) {
			Entry<char[],char[]> ent = it.next();
			out[i++] = ent.getKey();
			out[i++] = ent.getValue();
		}
		if(i != n) {
			out = Arrays.copyOf(out, i);
		}
		return out;
	}
	
	public static char[][] loadWhitelist(ConfigurationSection sec) {
		if(sec == null) {
			return null;
		}
		return AntiSwear.loadWhitelist(sec.getStringList("Whitelist"));
	}
	
	public static char[][] loadWhitelist(String[] strs) {
		if(strs == null || strs.length == 0) {
			return null;
		}
		int n = strs.length;
		char[][] ret = new char[n][];
		for(int i = 0; i < n; i++) {
			ret[i] = strs[i].toLowerCase().toCharArray();
		}
		return ret;
	}
	
	public static char[][] loadWhitelist(Iterable<String> whitelist) {
		if(whitelist == null) {
			return null;
		}
		return AntiSwear.loadWhitelist(whitelist.iterator());
	}
	
	public static char[][] loadWhitelist(Iterator<String> it) {
		if(it != null && it.hasNext()) {
			List<char[]> list = new ArrayList<char[]>();
			while(it.hasNext()) {
				String val = it.next();
				if(val != null) {
					list.add(val.toLowerCase().toCharArray());
				}
			}
			char[][] arr = list.toArray(new char[0][]);
			AntiSwear.sort(arr);
			return arr;
		}
		return null;
	}
	
	public static char[][] contact(char[][] one, char[][] two) {
		if(one == null || one.length == 0) {
			return two;
		}
		if(two == null || two.length == 0) {
			return one;
		}
		char[][] arr = new char[one.length + two.length][];
		System.arraycopy(one, 0, arr, 0, one.length);
		System.arraycopy(two, 0, arr, one.length, two.length);
		return arr;
	}
	
	public static char[][] contactSort(char[][] one, char[][] two) {
		if(one == null || one.length == 0) {
			return two;
		}
		if(two == null || two.length == 0) {
			return one;
		}
		char[][] arr = new char[one.length + two.length][];
		System.arraycopy(one, 0, arr, 0, one.length);
		System.arraycopy(two, 0, arr, one.length, two.length);
		AntiSwear.sort(arr);
		return arr;
	}
	
	public static char[][] contactSortTwo(char[][] one, char[][] two) {
		if(one == null || one.length == 0) {
			return two;
		}
		if(two == null || two.length == 0) {
			return one;
		}
		char[][] arr = new char[one.length + two.length][];
		System.arraycopy(one, 0, arr, 0, one.length);
		System.arraycopy(two, 0, arr, one.length, two.length);
		AntiSwear.sortTwo(arr);
		return arr;
	}
	
}
