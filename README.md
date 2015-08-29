Don't write hundreds of synonyms or complex regexes, when you can write just words.

[Under MIT license, opensource.](https://github.com/wordnice/BetterAntiSwear)


# Default config with some swears

```yaml
# Block message with swear
BlockSwear: false

# Allow Operators to swear
AllowOPSwear: false

# Message sent to player when swear
# If you don't want send any message, just leave it empty
SwearMessage: "&4Don't swear!"





# ATENTION
# CONTENT BELOW DOES NOT LOOK NICE

# Swears, disallowed words
# Use as
#    - swear: cen**red
# (you can use colors &4)
# Otherwise swear will be replaced by ***
# 
# NOTE: Put censored words between apostrophes
Blacklist:
- noob: 'n**b'
- idiot: 'id**t'
- ass: 'a**'
- penis: 'pen*s'
- vagina: 'vag**a'
- fuck: 'f**k'
- dick: 'd**k'
- pussy: 'p***y'
- stupid: 'st*pid'
- nerd: 'n**d'
- stfu: 's***'
- wtf: 'w**'
- anus: 'a***'
- arse: 'a***'
- arsehole: 'a***'
- asshole: 'a***'
- butt: 'b**t'
- bitch: 'b**ch'
- crap: 'cr*p'
- bastard: 'b*stard'
- boobs: 'b**bs'
- shit: 'sh*t'
- piss: 'p*ss'

# Words containing blacklisted words, but are enabled
Whitelist:
- ' badass '
- ' as '

```

This config will block:

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


# Permissions:

- `BetterAntiSwear.Swear` - If player has this permission, or `AllowOPSwear` from config is allowed and player is operator, his messages are not scaned
- `BetterAntiSwear.Test` - Test message with `/bas test <message>` command


# Commands

## /bas

* Permission `BetterAntiSwear.Test` or OP
* Aliases `betterantiswear`, `antiswear`, `as`
* Subcommands
	* `test` - scan entered message

![/bas test lel m8s i gotta rekt u stoopid idiots nubs](/screenshots/bas_test.png)



# API:

See [commented sources](https://github.com/wordnice/BetterAntiSwear/blob/master/src/eu/wordnice/antiswear/AntiSwear.java). You may use:

```java
	/**
	 * @param in Message to check and process
	 * @param mini If not null and length >= 1, mini[0] is filled with minimalized string
	 * 
	 * @return If swear(s) were found, returns new modified string. Otherwise returns `null`
	 */
	public static String processString(String in, String[] mini)
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
