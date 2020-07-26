package io.gomint.indev.listener;

import discord4j.core.object.entity.Message;
import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import io.gomint.event.EventPriority;
import io.gomint.event.player.PlayerChatEvent;
import io.gomint.indev.IndevPlugin;
import io.gomint.world.Gamemode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerChatListener implements EventListener {

    private final IndevPlugin plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event) {
        Message message = this.plugin.sendMessage(event.getPlayer().getName() + ": " + event.getText());

        // Check if the last message is a request
        if (event.getText().startsWith("!request")) {
            String[] split = event.getText().split(" ");
            switch (split[1]) {
                case "creative":
                    this.plugin.queueRequest(message, accepted -> {
                        if (accepted) {
                            if (event.getPlayer().isOnline()) {
                                this.plugin.sendMessage("Giving " + event.getPlayer().getName() + " creative");
                                event.getPlayer().setGamemode(Gamemode.CREATIVE);
                            }
                        }
                    });
            }
        }
    }

}
