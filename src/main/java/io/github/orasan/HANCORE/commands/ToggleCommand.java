package io.github.orasan.HANCORE.commands;

import io.github.orasan.HANCORE.NHANCORE;
import io.github.orasan.HANCORE.managers.ToggleManager;
import io.github.orasan.HANCORE.managers.ToggleManager.ToggleType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ToggleCommand implements CommandExecutor {

    private final NHANCORE plugin;

    public ToggleCommand(NHANCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage(sender, "player_only"));
            return true;
        }

        Player player = (Player) sender;
        ToggleManager toggleManager = plugin.getToggleManager();

        if (label.equalsIgnoreCase("toggledeathmsgs")) {
            boolean newState = toggleManager.toggle(player.getUniqueId(), ToggleType.SHOW_DEATH_MESSAGES);
            if (newState) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "toggledeathmsgs_on"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "toggledeathmsgs_off"));
            }
            return true;
        }

        if (label.equalsIgnoreCase("togglechat")) {
            boolean newState = toggleManager.toggle(player.getUniqueId(), ToggleType.GLOBAL_CHAT);
            if (newState) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "togglechat_on"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "togglechat_off"));
            }
            return true;
        }

        if (label.equalsIgnoreCase("toggleadv")) {
            boolean newShow = toggleManager.toggle(player.getUniqueId(), ToggleType.SHOW_ADVANCEMENTS);

            // Sync BROADCAST to match SHOW
            toggleManager.setToggle(player.getUniqueId(), ToggleType.BROADCAST_MY_ADVANCEMENTS, newShow);

            if (newShow) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "toggle_adv_on"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "toggle_adv_off"));
            }
            return true;
        }

        return false;
    }
}
