Don't write hundreds of synonyms or complex regexes, when you can write just words.

[Under MIT license, opensource.](https://github.com/wordnice/BetterAntiSwear)


# Default config

*Default config contains English, Polish, Czech and Slovak swears. Just download & enjoy.*

BetterAntiSwear got inteligent filters to detect swears even if they do not match exactly. That means, you don't have to write hunders of synonyms or write complex regexes to avoid swears in your chat, you just tell plugin blacklisted words, few additional whitelisted (like badass) and start your server.

For example, if you got config with blacklisted ass and idiot, and whitelisted badass and as, BetterAntiSwear will disallow:

```
ass       --> a**
áss       --> a**
A$S       --> a**
A.S.S     --> a**
4.$.$     --> a**
AssSSs    --> a**

noob      --> n**b
n00b      --> n**b
nob       --> n**b
nôb       --> n**b
n0b       --> n**b
nub       --> n**b
ňúb       --> n**b
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

See [commented sources](https://github.com/wordnice/BetterAntiSwear/blob/master/src/eu/wordnice/antiswear/AntiSwear.java). You may use:

```java
	/**
	 * @param in Message to check and process
	 * @param mini If not null and length >= 1, mini[0] is filled with minimalized string
	 * 
	 * @return If swear(s) were found, returns new modified string. Otherwise returns `null`
	 */
	public static String processString(String in, String[] mini) {...}
```

As:

```java
	String message = "hello nubs!";
	String nevmessage = AntiSwears.processString(message, null);
	if(nevmessage == null) {
		nevmessage = message;
		//No swears were found
	} else {
		//Swears were found
	}
	//Done, "nevmessage" variable contains censored words, use it as you want
```
