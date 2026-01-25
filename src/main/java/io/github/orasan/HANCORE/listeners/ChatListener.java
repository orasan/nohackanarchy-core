package io.github.orasan.HANCORE.listeners;

import io.github.orasan.HANCORE.NHANCORE;
import io.github.orasan.HANCORE.managers.IgnoreManager;
import io.github.orasan.HANCORE.managers.IgnoreManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class ChatListener implements Listener {

    private final NHANCORE plugin;

    public ChatListener(NHANCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage(); // Legacy string method is easier for keyword checking than Component
        IgnoreManager ignoreManager = plugin.getIgnoreManager();

        // Loop through recipients and remove those who ignore the sender or content
        // We use a safe way to remove from the recipients set
        Set<Player> recipients = event.getRecipients();

        // RemoveIf is safe on the Set provided by event.getRecipients() in most server
        // implementations,
        // but to be absolutely safe and follow logic:
        recipients.removeIf(recipient -> {
            // Don't hide from self unless we really want to (usually we don't)
            if (recipient.equals(sender))
                return false;

            // 1. Check Ignore System
            if (ignoreManager.isIgnored(recipient.getUniqueId(), sender.getUniqueId(), message))
                return true;

            // 2. Check Toggle System (Chat Toggle)
            if (!plugin.getToggleManager().getToggle(recipient.getUniqueId(),
                    io.github.orasan.HANCORE.managers.ToggleManager.ToggleType.GLOBAL_CHAT)) {
                return true;
            }

            return false;
        });
    }
}
