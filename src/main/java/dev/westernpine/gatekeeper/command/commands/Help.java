package dev.westernpine.gatekeeper.command.commands;

import dev.westernpine.gatekeeper.GateKeeper;
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
import proj.api.marble.lib.emoji.Emoji;

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
            sb.append("\n • `AutoRole/AR` - Lists all auto-applied roles for every account type.").append("\n");
            sb.append("\n • `AutoRole/AR Bot/Client @TaggedRoles` - Toggles the auto-applied roles for the account type.").append("\n");
            sb.append("\n • `ReactionRole/RR #TaggedChannel MessageID @TaggedRole` - Creates a reaction on  the specified message with the tagged role.").append("\n");
            sb.append("\n\n*Remember to set the bot's role, ABOVE the roles it will be offering. There will be NO ERROR MESSAGE to alert you of this.*").append("\n");
            sb.append("\n*Please also note that for effeciency and saftey purposes, "
            		+ "GateKeeper will ONLY remove a role if a user reaction is removed on a reaction role. "
            		+ "When a autorole or reactionrole is set up, all roles will be applied. "
            		+ "GateKeeper WILL NOT remove roles when a autorole or reactionrole is removed/reset. "
            		+ "Removing roles is the responsibility of the server administrator(s).*").append("\n");
            sb.append("").append("\n");
            embed.addField(sbName, sb.toString(), false);
        }
		
		embed.addField("", Emoji.Link.getValue() 
        		+ " [**Discord**](https://discord.gg/PDbnC8z)"
        		+ " • [**Code**](https://github.com/WesternPine/GateKeeper)"
                + " • [**Updates**](https://github.com/WesternPine/GateKeeper/commits/master)"
                + " • [**Invite**](https://discordapp.com/api/oauth2/authorize?client_id=697959175845707816&permissions=268520512&scope=bot)",
                false);
        
        try {
            User maker = GateKeeper.getInstance().getManager().getUserById("559027677017669661");
            embed.setFooter(
                    maker.getName() + " Powered!" + " (" + maker.getName() + "#" + maker.getDiscriminator() + ")",
                    maker.getAvatarUrl());
        } catch (Exception e) {
//            e.printStackTrace();
        	embed.setFooter("K3ttle Powered! (K3ttle#1000)",
                    "https://cdn.discordapp.com/avatars/559027677017669661/19e88d66ad8249d7b043df831b346667.png?size=128");
        }

        Messenger.clearDM(member.getUser());
        Messenger.sendEmbed(member.getUser(), embed.build());
		
	}

}
