package dev.westernpine.gatekeeper.command.commands;

import dev.westernpine.common.emoji.Emoji;
import dev.westernpine.gatekeeper.command.Command;
import dev.westernpine.gatekeeper.object.Messages;
import dev.westernpine.gatekeeper.util.Messenger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class Help implements Command {

	@Override
	public boolean permissible() {
		return false;
	}

	@Override
	public boolean useRole() {
		return false;
	}

	@Override
	public String getRole() {
		return null;
	}

	@Override
	public Permission getPermission() {
		return null;
	}

	@Override
	public void execute(Guild guild, User user, MessageChannel ch, Message msg, String command, String[] args) {
		Member member = guild.getMember(user);
		EmbedBuilder embed = Messages.getEmbedFrame();
        embed.setAuthor(Emoji.Gear.getValue() + " GateKeeper Help Guide " + Emoji.Gear.getValue());
        
        if(!member.hasPermission(Permission.MANAGE_ROLES)) {
        	String title = Emoji.Exclamation.getValue() + " No Permission!";
        	String desc = "Looks like you don't have permission to modify the roles of " + guild.getName() + "."
        			+ "\nDon't worry though! You can invite me to another server using the link below!";
        	embed.addField(title, desc, false);
        } else {
        	String sbName = Emoji.Exclamation.getValue() + " Commands";
            StringBuilder sb = new StringBuilder();
            sb.append("\n • **AutoRole/AR** *Lists all auto-applied roles for every account type.*");
            sb.append("\n\n • **AutoRole/AR Bot/Client @MentionedRoles** *Toggles the auto-applied roles for the account type.*");
            sb.append("\n\n • **ReactionRole/RR Add/Remove #ChannelMention MessageID @MentionedRoles** *Creates a reaction on  the specified message with the tagged role.*");
            
            embed.addField(sbName, sb.toString(), false);
            embed.addField("Important Notes:", "**Remember to set the bot's role, ABOVE the roles it will be offering. There will be NO ERROR MESSAGE to alert you of this.*", false);
            embed.addField("", "**Role removal outside the bot's intended purpose, is the responsibility of the server administrator.*", false);
            embed.addField("", "**To reset a reaction role, delete the reaction from the message.*", false);
            embed.addField("", "**Deleting only the bot's reaction, and later setting a reaction role with existing user reactions, will apply the reaction role properties to all other reactors.*", false);
            
        }
		
        embed.addField("", Emoji.Link.getValue() 
				+ " [**Discord**](https://discord.gg/PDbnC8z)"
        		+ " • [**Code**](https://github.com/WesternPine/GateKeeper)"
                + " • [**Updates**](https://github.com/WesternPine/GateKeeper/commits/master)"
                + " • [**Invite**](https://discordapp.com/api/oauth2/authorize?client_id=697959175845707816&permissions=268520512&scope=bot)",
                false);
        
		embed.setFooter("Join the Discord above!", "https://cdn.discordapp.com/icons/651185146543996938/ad1d1ed7f46f8a8ab9f77e1798dad233.webp?size=128");
		
        Messenger.clearDM(member.getUser());
        Messenger.sendEmbed(member.getUser(), embed.build());
		
	}

}
