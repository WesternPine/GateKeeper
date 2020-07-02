package dev.westernpine.gatekeeper;

import java.awt.Color;
import java.io.File;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import dev.westernpine.gatekeeper.backend.Backend;
import dev.westernpine.gatekeeper.configuration.ConfigValue;
import dev.westernpine.gatekeeper.configuration.GateKeeperConfig;
import dev.westernpine.gatekeeper.listener.CommandListener;
import dev.westernpine.gatekeeper.listener.RoleDeletionListener;
import dev.westernpine.gatekeeper.listener.autorole.UserJoinListener;
import dev.westernpine.gatekeeper.listener.reaction.ChannelDeletionListener;
import dev.westernpine.gatekeeper.listener.reaction.GuildListener;
import dev.westernpine.gatekeeper.listener.reaction.MessageDeletionListener;
import dev.westernpine.gatekeeper.listener.reaction.ReactionAppliedListener;
import dev.westernpine.gatekeeper.listener.reaction.ReactionDeletionListener;
import dev.westernpine.gatekeeper.management.GuildManager;
import lombok.Getter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class GateKeeper {

	@Getter
	private static GateKeeper instance;

	@Getter
	private GateKeeperConfig config;

	@Getter
	private ShardManager manager;

	public static void main(String[] args) {
		new GateKeeper().init(args);
	}

	public static Color defColor(Guild guild) {
		return guild.getSelfMember().getColor();
	}

	private void init(String[] args) {
		instance = this;

		System.out.println("Starting discord api library (JDA)...");

		try {
			String filePath;
			try {
				filePath = new File(GateKeeper.class.getProtectionDomain().getCodeSource().getLocation().toURI()
						.getPath().toString()).getParent();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error getting file path, shutting down.");
				return;
			}

			config = new GateKeeperConfig(args, filePath, "GateKeeperConfig.yml");

			Backend.initialize(true);

			if (!Backend.canConnect())
				throw new Exception("Unable to connect to MySQL Database, shutting down.");

			Set<GatewayIntent> intents = new HashSet<>(EnumSet.of(GatewayIntent.DIRECT_MESSAGES,
					GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES,
					GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_EMOJIS));
			Set<CacheFlag> flags = new HashSet<>(
					EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.MEMBER_OVERRIDES, CacheFlag.EMOTE));

			DefaultShardManagerBuilder builder = DefaultShardManagerBuilder
					.createLight(config.getValue(ConfigValue.DISCORD_TOKEN));
			builder.enableIntents(intents);
			builder.enableCache(flags);
			builder.setChunkingFilter(ChunkingFilter.ALL);
			builder.setMemberCachePolicy(MemberCachePolicy.ALL);

			// common listeners
			builder.addEventListeners(new CommandListener());
			builder.addEventListeners(new RoleDeletionListener());

			// auto role listeners
			builder.addEventListeners(new UserJoinListener());

			// reaction role listeners
			builder.addEventListeners(new ChannelDeletionListener());
			builder.addEventListeners(new GuildListener());
			builder.addEventListeners(new MessageDeletionListener());
			builder.addEventListeners(new ReactionAppliedListener());
			builder.addEventListeners(new ReactionDeletionListener());

			// startup initializer
			builder.addEventListeners(new ListenerAdapter() {
				@Override
				public void onReady(ReadyEvent event) {
					System.out.println("Ready event called, initializing...");
					Backend.initializeGuilds(manager.getGuilds());
					GuildManager.initialize();
					System.out.println("Initialization completed!");
					System.out.println("Bot startup completed!");
				}
			});

			manager = builder.build();

			manager.setPresence(OnlineStatus.ONLINE, Activity.watching("for \"" + getPrefix() + "help\""));

			System.out.println("Discord library started!");
			System.out.println("Waiting for initialization...");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("There was a problem starting the bot. Please try again.");
			return;
		}
	}

	public String getPrefix() {
		return config.getValue(ConfigValue.COMMAND_PREFIX);
	}

}