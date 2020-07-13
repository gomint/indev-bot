package io.gomint.indev;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import io.gomint.config.InvalidConfigurationException;
import io.gomint.indev.config.Config;
import io.gomint.indev.listener.PlayerChatListener;
import io.gomint.indev.listener.PlayerJoinListener;
import io.gomint.indev.listener.PlayerQuitListener;
import io.gomint.plugin.Plugin;
import io.gomint.plugin.PluginName;
import io.gomint.plugin.Startup;
import io.gomint.plugin.StartupPriority;
import io.gomint.plugin.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.time.Duration;

@PluginName("InDev")
@Version(major = 1, minor = 0)
@Startup(StartupPriority.STARTUP)
public class IndevPlugin extends Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndevPlugin.class);

    private Config config;
    private GatewayDiscordClient discordClient;
    private GuildMessageChannel channel;

    @Override
    public void onStartup() {
        // Load config first
        this.config = new Config();

        try {
            this.config.init( Paths.get(this.getDataFolder().getAbsolutePath(), "config.yml").toFile());
        } catch (InvalidConfigurationException e) {
            LOGGER.error("Could not load config", e);
            this.uninstall();
        }

        // Now connect to discord
        final DiscordClient client = DiscordClient.create(this.config.getDiscordToken());
        this.discordClient = client.login().block();

        if (this.discordClient == null) {
            LOGGER.error("Could not connect to discord");
            this.uninstall();
            return;
        }

        // Get discord channel
        Guild guild = this.discordClient.getGuildById(Snowflake.of(this.config.getDiscordGuild())).block(Duration.ofSeconds(5));
        if (guild == null) {
            LOGGER.error("Could not connect to discord guild");
            this.uninstall();
            return;
        }

        this.channel = (GuildMessageChannel) guild.getChannelById(Snowflake.of(this.config.getDiscordChannel())).block(Duration.ofSeconds(5));

        if (this.channel == null) {
            LOGGER.error("Could not connect to discord channel");
            this.uninstall();
            return;
        }

        // Register listeners
        registerListener(new PlayerJoinListener(this));
        registerListener(new PlayerChatListener(this));
        registerListener(new PlayerQuitListener(this));

        // Tell people we are live
        this.sendMessage("InDev Server online: gomint.io");
    }

    public void sendMessage(String line) {
        this.channel.createMessage(line).block(Duration.ofSeconds(1));
    }

    @Override
    public void onUninstall() {
        this.sendMessage("Going down for a update");

        // Close connection to discord
        this.discordClient.logout().block(Duration.ofSeconds(5));
        this.discordClient.onDisconnect().block(Duration.ofSeconds(5));
    }
}
