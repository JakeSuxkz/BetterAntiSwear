Don't write hundreds of synonyms or complex regexes, when you can write just words.

[Under MIT license, opensource.](https://github.com/wordnice/BetterAntiSwear)


# Default config

*Default config for Bukkit server contains English, Polish, Czech and Slovak swears. Just download & enjoy.*

BetterAntiSwear got inteligent filters to detect swears even if they do not match exactly. That means, you don't have to write hunders of synonyms or write complex regexes to avoid swears in your chat - you just tell plugin blacklisted words, few additional whitelisted (like badass) and start your server.

BetterAntiSwear was originally made as Bukkit plugin, but is fully compatible with plain Java, and removing Bukkit support is really easy - remove AntiSwearPlugin.java and unimport ChatColor and ConfigurationSection from AntiSwear.java.

For example, if you got config with blacklisted duck and noob (as d\*ck and n\*\*b), and whitelisted ducks, BetterAntiSwear will do the following:

```
duck.     -> d*ck.
!D.u.C.K! -> !d*ck!
DUUUUCK   -> d*ck
DuCKs!    -> DuCKs!
DuCkS     -> DuCkS
Duucks    -> d*cks

noob      -> n**b
nôb??     -> n**b??
n0b!      -> n**b!
Ň.ôB      -> n**b
```

but will allow:

```
badass
as
```


# Permissions

- `BetterAntiSwear.Swear` - If player has this permission, or `AllowOPSwear` from config is allowed and player is operator, his messages are not scaned
- `BetterAntiSwear.Test` - Test message with `/bas test <message>` command


# Commands

## /bas

* Permission `BetterAntiSwear.Test` or OP
* Aliases `betterantiswear`, `antiswear`, `as`
* Subcommands
	* `test` - scan entered message
	* `/bas test <message>`



# API

```java

		AntiSwear as = AntiSwear.getLast();
		
		//If antiswear instance was found, use it
		//Oterwise create new
		if(as == null) {
			as = AntiSwear.getNew();
			AntiSwear.setLast(as);
			
			//Add blacklisted words
			as.addBlacklist("idiot", "idi*t");
			as.addBlacklist("noob", "n**b");
			
			//Add blacklisted words from map
			Map<String,String> bl = new HashMap<String,String>();
			bl.put("duck", "d**k");
			bl.put("ass", "as*");
			as.addBlacklist(bl);
			
			//Add whitelisted words
			//Space will match punctation, any space and start/end of text
			//    see AntiSwear.WHITELIST_SPACE_EQUALS
			as.addWhitelist(" ducks ", " badass ");
		}
		
		
		//string to check
		String message = "My little pony, badass duck need ducks! A$shole, Nub, n00bs!";
		
		//process string
		System.out.print("Processing string: ");
		System.out.println(message);
		String nevMessage = as.processString(message);
		
		if(nevMessage == null) {
			//If processString returned null, given message does not
			//contains swears!
			System.out.println("Not swears were found!");
		} else {
			//Otherwise swears were found and removed
			System.out.println("Swears were found! New message is:");
			System.out.println(nevMessage);
			
			//If using our settings, swears will be found
			//and we will get new message:
			//My little pony, badass d**k need ducks! as*hole, n**b, n**bs!
		}

```

[Source code](https://github.com/wordnice/BetterAntiSwear/blob/master/src/eu/wordnice/antiswear/AntiSwear.java)
