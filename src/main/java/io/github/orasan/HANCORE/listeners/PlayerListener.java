package io.github.orasan.HANCORE.listeners;

import io.github.orasan.HANCORE.NHANCORE;
import io.github.orasan.HANCORE.managers.ToggleManager;
import io.github.orasan.HANCORE.managers.ToggleManager.ToggleType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerListener implements Listener {

    private final NHANCORE plugin;

    public PlayerListener(NHANCORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Component deathMessage = event.deathMessage();
        if (deathMessage == null)
            return;

        // Hide default message
        event.deathMessage(null);

        Player victim = event.getEntity();
        ToggleManager tm = plugin.getToggleManager();
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();

        boolean hideOwn = config.getBoolean("death-messages.hide-own-on-toggle", false);
        boolean suppressBroadcast = config.getBoolean("death-messages.suppress-broadcast-on-toggle", false);
        boolean victimShow = tm.getToggle(victim.getUniqueId(), ToggleType.SHOW_DEATH_MESSAGES);

        for (Player online : Bukkit.getOnlinePlayers()) {
            boolean isVictim = online.getUniqueId().equals(victim.getUniqueId());

            if (isVictim) {
                // Victim logic: Show if they have enabled it OR if "hide-own" is false (forced
                // show)
                if (victimShow || !hideOwn) {
                    online.sendMessage(deathMessage);
                }
            } else {
                // Others logic
                // 1. Check suppression by victim
                if (suppressBroadcast && !victimShow) {
                    continue; // Victim is hiding logs, so suppress broadcast
                }

                // 2. Check recipient preference
                if (tm.getToggle(online.getUniqueId(), ToggleType.SHOW_DEATH_MESSAGES)) {
                    online.sendMessage(deathMessage);
                }
            }
        }
        // Console always sees it
        Bukkit.getConsoleSender().sendMessage(deathMessage);
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfig().getBoolean("chat.hide-tell-on-toggle", false)) {
            return;
        }

        String msg = event.getMessage().toLowerCase();
        String[] args = msg.split(" ");
        if (args.length < 2)
            return; // Not enough args for a tell command

        String command = args[0];
        if (command.equals("/tell") || command.equals("/msg") || command.equals("/w")) {
            // Find target
            String targetName = args[1];
            Player target = Bukkit.getPlayer(targetName);

            if (target != null) {
                if (!plugin.getToggleManager().getToggle(target.getUniqueId(), ToggleType.GLOBAL_CHAT)) {
                    // Target has chat disabled
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Component.text("相手がチャットを非表示にしているため、メッセージを送信できません。",
                            net.kyori.adventure.text.format.NamedTextColor.RED));
                    // Or use config message? Request didn't specify feedback message for sender.
                    // I will put a hardcoded one or add to config?
                    // "適切なメッセージ" for ignore command was requested, but valid tell feedback is good
                    // too.
                    // I'll stick to Japanese hardcoded for now or add "tell_blocked" to JSON?
                    // I'll add a simple Japanese message here for now to fit the task scope without
                    // expanding JSON task too much unless requested.
                }
            }
        }
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Component message = event.message();
        if (message == null)
            return;

        // Prevent default broadcast
        event.message(null);

        Player earner = event.getPlayer();
        ToggleManager tm = plugin.getToggleManager();
        boolean earnerBroadcasts = tm.getToggle(earner.getUniqueId(), ToggleType.BROADCAST_MY_ADVANCEMENTS);
        boolean hideOwn = plugin.getConfig().getBoolean("advancements.hide-own-on-toggle", false);

        for (Player receiver : Bukkit.getOnlinePlayers()) {
            boolean isSelf = receiver.getUniqueId().equals(earner.getUniqueId());

            if (isSelf) {
                // Show if broadcasting (toggle ON) OR if we shouldn't hide own on toggle
                // (forced show)
                if (earnerBroadcasts || !hideOwn) {
                    receiver.sendMessage(message);
                }
            } else {
                // Other players logic
                boolean receiverShows = tm.getToggle(receiver.getUniqueId(), ToggleType.SHOW_ADVANCEMENTS);
                if (earnerBroadcasts && receiverShows) {
                    receiver.sendMessage(message);
                }
            }
        }
        // Console always sees it
        Bukkit.getConsoleSender().sendMessage(message);
    }
}
