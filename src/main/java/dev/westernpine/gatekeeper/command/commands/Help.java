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
        embed.setAuthor(Emoji.Question.getValue() + " GateKeeper Help Guide " + Emoji.Question.getValue());
        embed.setDescription("**\nCommand Prefix:** `" + GateKeeper.getInstance().getPrefix() + "`**\n\n**");
        
        if(!member.hasPermission(Permission.MANAGE_ROLES)) {
        	String title = Emoji.GreyExclamation.getValue() + " No Permission!";
        	String desc = "Looks like you don't have permission to modify the roles of " + guild.getName() + "."
        			+ "\nDon't worry though! You can invite me to another server using the link below!";
        	embed.addField(title, desc, false);
        } else {
        	String sbName = Emoji.GreyExclamation.getValue() + " Commands";
            StringBuilder sb = new StringBuilder();
            sb.append(" • `AutoRole/AR` - Lists all auto-applied roles for every account type.").append("\n");
            sb.append("\n • `AutoRole/AR Bot/Client @TaggedRoles` - Toggles the auto-applied roles for the account type.").append("\n");
            sb.append("\n • `ReactionRole/RR #TaggedChannel MessageID @TaggedRole` - Creates a reaction on  the specified message with the tagged role.").append("\n");
            sb.append("").append("\n");
            embed.addField(sbName, sb.toString(), false);
        }
		
		embed.addField("", Emoji.Link.getValue() 
        		+ " [**Developer Discord**](https://discord.gg/PDbnC8z)"
        		+ " • [**Code**](https://github.com/WesternPine/Pulse)"
                + " • [**Updates**](https://github.com/WesternPine/Pulse/commits/master)"
                + " • [**Invite**](https://discordapp.com/api/oauth2/authorize?client_id=633025336292147222&permissions=3164160&scope=bot)",
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
