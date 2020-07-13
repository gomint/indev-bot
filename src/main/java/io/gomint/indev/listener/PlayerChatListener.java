package io.gomint.indev.listener;

import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import io.gomint.event.EventPriority;
import io.gomint.event.player.PlayerChatEvent;
import io.gomint.indev.IndevPlugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerChatListener implements EventListener {

    private final IndevPlugin plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event) {
        this.plugin.sendMessage(event.getPlayer().getName() + ": " + event.getText());
    }

}
