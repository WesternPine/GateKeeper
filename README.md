# GateKeeper

[![Pulse](https://github.com/WesternPine/GateKeeper/blob/master/Gatekeeper.jpg?raw=true)](https://discordapp.com/api/oauth2/authorize?client_id=697959175845707816&permissions=268520512&scope=bot)

^ [Click To Add Me!](https://discordapp.com/api/oauth2/authorize?client_id=697959175845707816&permissions=268520512&scope=bot)

## Introduction

Steamy is a simple discord bot that allows server owners to manage automatic roles, as well as clear all the chat from a text channel. It's purpose was to be minimalistic, yet simple. If you plan on hosting your own bot, MySQL is a requirement as the Steamy was made to run without using any config files if possible. If you have the bot added to your server already, take a look at [Usage](https://github.com/WesternPine/Steamy#usage). Otherwise, let's get started!

## Downloading The Jar (Requires A Java IDE, Java 8 JDK, and Git):

  - Clone the GateKeeper Repository.
  - Run the project as a `Maven Build` with goals of `clean install`
  - Production Jar-> `Project-Folder/target/GateKeeper-X.jar`
  
## Pre-Set-Up Information:

To set up and run the jar, there are a few different options depending on how you run it. All options use the same configuration keys to identify values in any of the 3 launch options. If any keys were not set-up correctly, the program will default to the next lowest tier until using a configuration file variable that would be automatically generated. In English: We got your back. <3

### Configuration Keys

The following are configuration keys to be used when setting up the bot.

| Key | Description |
|-----|-------------|
| BOT_TOKEN | Token for account to be used by the bot. |
| COMMAND_PREFIX | The command prefix to be listening for. |
| SQL_IP | The Ip of the MySQL database. |
| SQL_PORT | The port to use for the MySQL database. |
| SQL_DATABASE | The SQL Database to use. |
| SQL_USERNAME | The Username of the account to be used. |
| SQL_PASSWORD | The password for the user account to be used. |

### MySQL

GateKeeper requires a SQL database to create tables (one for each server), to delete values from those tables, and to insert values into them as well. As long as the bot has those permissions, everything else is automated from there.

## Setup (almost there...)

  - Failover/Fallback Configuration Hierarchy: 
  
```
Startup Arguments > System Environmental Variables > Configuration File (Automatically Generated, Last Resort)
```

  - Starup Arguments:
  
```
java -jar GateKeeper-X.jar -[Configuration Key] [Value] -[Configuration Key 2] [Value] (etc...)
```

  - Environmental Variables:

    - This is mostly used for services such as [Heroku](https://heroku.com) where you can set the variables manually. 

  - Configuration File:
  
```
[Configuration Key]: [Value]
(etc.)
```

## Starting The Bot (finally!)

Start your bot in any of the 3 ways listed above, with the proper configuration information set up. (Please have MySQL set up before starting the bot... We programmers don't code magic! :P) Add the bot account used, to your server if you havn't already, and type the help command to get started!


## Usage

Whether you made your own bot, or want to use the [pre-existing one (Click Here)](https://discordapp.com/api/oauth2/authorize?client_id=697959175845707816&permissions=268520512&scope=bot), Using the bot is the same. Simply stick with the default permissions the bot needs (You can find these using the invite link), and run the help command (Found in the bot's status).

And that's everything to know about Gatekeeper! If you have any other questions, comments, or concerns, feel free to contact me here on github or use my website in my profile. Thank you!

License
----

[MIT](https://choosealicense.com/)


