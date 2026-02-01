package io.github.orasan.HANCORE.listeners;

import io.github.orasan.HANCORE.NHANCORE;
import io.github.orasan.HANCORE.managers.IgnoreManager;
import io.github.orasan.HANCORE.managers.ToggleManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final NHANCORE plugin;

    public ChatListener(NHANCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Component messageComponent = event.message();
        // Serialize component to plain text for IgnoreManager checks
        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent);
        IgnoreManager ignoreManager = plugin.getIgnoreManager();

        event.viewers().removeIf(audience -> {
            if (!(audience instanceof Player recipient)) {
                return false; // Don't filter console or other audiences
            }

            // Don't hide from self
            if (recipient.equals(sender))
                return false;

            // 1. Check Ignore System
            if (ignoreManager.isIgnored(recipient.getUniqueId(), sender.getUniqueId(), message))
                return true;

            // 2. Check Toggle System (Chat Toggle)
            if (!plugin.getToggleManager().getToggle(recipient.getUniqueId(), ToggleManager.ToggleType.GLOBAL_CHAT)) {
                return true;
            }

            return false;
        });
    }
}
