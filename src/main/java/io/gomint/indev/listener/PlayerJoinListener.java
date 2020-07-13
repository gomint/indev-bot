package io.gomint.indev.listener;

import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import io.gomint.event.EventPriority;
import io.gomint.event.player.PlayerJoinEvent;
import io.gomint.indev.IndevPlugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerJoinListener implements EventListener {

    private final IndevPlugin plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.plugin.sendMessage(event.getPlayer().getName() + " joined the server");
    }

}
