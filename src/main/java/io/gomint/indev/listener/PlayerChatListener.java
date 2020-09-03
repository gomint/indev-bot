package io.gomint.indev.listener;

import discord4j.core.object.entity.Message;
import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import io.gomint.event.EventPriority;
import io.gomint.event.player.PlayerChatEvent;
import io.gomint.indev.IndevPlugin;
import io.gomint.world.Gamemode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class PlayerChatListener implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerChatEvent.class);

    private final IndevPlugin plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event) {
        this.plugin.sendMessage(event.getPlayer().getName() + ": " + event.getText());

        LOGGER.info(event.getPlayer().getName() + "> " + event.getText());

        // Check if the last message is a request
        if (event.getText().startsWith("!request")) {
            LOGGER.info("Got request");
            String[] split = event.getText().split(" ");
            LOGGER.info("  {}", split[1]);
            switch (split[1]) {
                case "creative":
                    this.plugin.sendMessage("Giving " + event.getPlayer().getName() + " creative");
                    event.getPlayer().setGamemode(Gamemode.CREATIVE);
                    break;
                case "survival":
                    this.plugin.sendMessage("Giving " + event.getPlayer().getName() + " survival");
                    event.getPlayer().setGamemode(Gamemode.SURVIVAL);
                    break;
            }
        }
    }

}
