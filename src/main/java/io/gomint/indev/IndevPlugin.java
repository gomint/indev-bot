package io.gomint.indev;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import io.gomint.GoMint;
import io.gomint.config.InvalidConfigurationException;
import io.gomint.entity.EntityPlayer;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@PluginName("InDev")
@Version(major = 1, minor = 0)
@Startup(StartupPriority.STARTUP)
public class IndevPlugin extends Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndevPlugin.class);

    private Config config;
    private GatewayDiscordClient discordClient;
    private GuildMessageChannel channel;

    // Requested actions
    private Map<Snowflake, Consumer<Boolean>> requests = new HashMap<>();

    @Override
    public void onStartup() {
        // Load config first
        this.config = new Config();

        try {
            this.config.init(Paths.get(this.getDataFolder().getAbsolutePath(), "config.yml").toFile());
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

        // Listen for incoming messages
        this.discordClient.on(MessageCreateEvent.class).subscribe(event -> {
            if (event.getMessage().getChannelId().equals(this.channel.getId())) {
                String message = event.getMessage().getContent();

                // Check if user clicked on it has enough permissions to ack this
                event.getMember().get().getRoles().subscribe(role -> {
                    if ("InDev Admin".equals(role.getName())) {
                        switch (message) {
                            case "stop":
                            case "restart":
                                this.getScheduler().execute(() -> GoMint.instance().shutdown());
                                break;
                            case "tps":
                                this.getScheduler().execute(() -> sendMessage("Current TPS: " + GoMint.instance().getTPS()));
                                break;
                            default:
                                if (message.startsWith("kick")) {
                                    String player = message.substring(5);
                                    this.getScheduler().execute(() -> {
                                        EntityPlayer player1 = GoMint.instance().findPlayerByName(player);
                                        if (player1 != null) {
                                            player1.disconnect("You got kicked");
                                        }
                                    });
                                }
                        }
                    }
                });
            }
        });

        // Fire actions based on reactions
        this.discordClient.on(ReactionAddEvent.class).subscribe(event -> {
            Optional<ReactionEmoji.Unicode> emoji = event.getEmoji().asUnicodeEmoji();
            if (emoji.isEmpty()) {
                LOGGER.info("No unicode emoji...");
                return;
            }

            ReactionEmoji.Unicode custom = emoji.get();
            LOGGER.info("Unicode emoji: {}", custom);
            if (!"✅".equals(custom.getRaw())) {
                return;
            }

            Consumer<Boolean> action = this.requests.get(event.getMessageId());
            if (action != null) {
                // Check if user clicked on it has enough permissions to ack this
                event.getUser().subscribe(user -> {
                    user.asMember(guild.getId()).subscribe(member -> {
                        member.getRoles().subscribe(role -> {
                            if ("InDev Admin".equals(role.getName())) {
                                this.getScheduler().execute(() -> {
                                    requests.remove(event.getMessageId());
                                    action.accept(true);
                                });
                            }
                        });
                    });
                });
            }
        });

        // Register listeners
        registerListener(new PlayerJoinListener(this));
        registerListener(new PlayerChatListener(this));
        registerListener(new PlayerQuitListener(this));

        // Tell people we are live
        this.sendMessage("InDev Server online: gomint.io");
    }

    public Message sendMessage(String line) {
        return this.channel.createMessage(line).block(Duration.ofSeconds(1));
    }

    public void queueRequest(Message message, Consumer<Boolean> action) {
        this.requests.put(message.getId(), action);

        // Add reactions
        message.addReaction(ReactionEmoji.unicode("✅"));

        // Add a timer for cleanup
        this.getScheduler().schedule(() -> {
            Consumer<Boolean> consumer = this.requests.remove(message.getId());
            if (consumer != null) {
                consumer.accept(false);
            }
        }, 1, TimeUnit.MINUTES);
    }

    @Override
    public void onUninstall() {
        this.sendMessage("Going down for a update");

        // Close connection to discord
        this.discordClient.logout().block(Duration.ofSeconds(5));
        this.discordClient.onDisconnect().block(Duration.ofSeconds(5));
    }
}
