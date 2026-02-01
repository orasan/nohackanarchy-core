package io.github.orasan.HANCORE.commands;

import io.github.orasan.HANCORE.NHANCORE;
import io.github.orasan.HANCORE.managers.IgnoreManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class IgnoreCommand implements CommandExecutor {

    private final NHANCORE plugin;

    public IgnoreCommand(NHANCORE plugin) {
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
        IgnoreManager ignoreManager = plugin.getIgnoreManager();

        if (label.equalsIgnoreCase("ignore")) {
            if (!player.hasPermission("nhancore.command.ignore")) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "no_permission"));
                return true;
            }
            if (args.length < 1) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "ignore_usage"));
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                showList(player);
                return true;
            }

            String targetName = args[0];
            if (targetName.equalsIgnoreCase(player.getName())) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "ignore_self"));
                return true;
            }

            // Resolve UUID
            OfflinePlayer targetOp = Bukkit.getOfflinePlayer(targetName);
            UUID targetUUID = targetOp.getUniqueId();
            String resolvedName = targetOp.getName();
            if (resolvedName == null)
                resolvedName = targetName; // Fallback to input arg if OP name null

            if (ignoreManager.addIgnorePlayer(player.getUniqueId(), targetUUID, resolvedName)) {
                player.sendMessage(
                        plugin.getConfigManager().getMessage(player, "ignore_added", "{target}", resolvedName));
            } else {
                player.sendMessage(
                        plugin.getConfigManager().getMessage(player, "ignore_already_exists", "{target}",
                                resolvedName));
            }
            return true;

        } else if (label.equalsIgnoreCase("delignore")) {
            if (!player.hasPermission("nhancore.command.delignore")) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "no_permission"));
                return true;
            }
            if (args.length < 1) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "delignore_usage"));
                return true;
            }
            String targetName = args[0];
            OfflinePlayer targetOp = Bukkit.getOfflinePlayer(targetName);
            UUID targetUUID = targetOp.getUniqueId();

            if (ignoreManager.removeIgnorePlayer(player.getUniqueId(), targetUUID)) {
                player.sendMessage(
                        plugin.getConfigManager().getMessage(player, "delignore_removed", "{target}", targetName));
            } else {
                player.sendMessage(
                        plugin.getConfigManager().getMessage(player, "ignore_not_found", "{target}", targetName));
            }
            return true;

        } else if (label.equalsIgnoreCase("ignorelist")) {
            if (!player.hasPermission("nhancore.command.ignorelist")) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "no_permission"));
                return true;
            }
            showList(player);
            return true;
        }

        return false;
    }

    private void showList(Player player) {
        java.util.Map<UUID, String> map = plugin.getIgnoreManager().getIgnorePlayerMap(player.getUniqueId());
        if (map.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage(player, "ignorelist_empty"));
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage(player, "ignorelist_header"));
            for (java.util.Map.Entry<UUID, String> entry : map.entrySet()) {
                String name = entry.getValue();
                // If name is "Unknown", maybe try to re-fetch? Or just show Unknown (UUID)
                if ("Unknown".equals(name) || name == null) {
                    name = "Unknown (" + entry.getKey().toString().substring(0, 8) + "...)";
                }
                player.sendMessage(Component.text("- " + name, NamedTextColor.WHITE));
            }
        }
    }
}
