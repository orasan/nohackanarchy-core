package io.github.orasan.HANCORE.commands;

import io.github.orasan.HANCORE.NHANCORE;
import io.github.orasan.HANCORE.managers.IgnoreManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class IgnoreWordCommand implements CommandExecutor {

    private final NHANCORE plugin;

    public IgnoreWordCommand(NHANCORE plugin) {
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

        if (label.equalsIgnoreCase("ignoreword")) {
            if (!player.hasPermission("nhancore.command.ignoreword")) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "no_permission"));
                return true;
            }
            if (args.length < 1) {
                // Usage (should be added to lang)
                player.sendMessage(plugin.getConfigManager().getMessage(player, "ignoreword_usage"));
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                showList(player);
                return true;
            }

            String word = args[0];
            int limit = plugin.getConfig().getInt("ignore.word-limit", 15);
            if (word.length() > limit) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "ignoreword_limit", "{limit}",
                        String.valueOf(limit)));
                return true;
            }

            if (ignoreManager.addIgnoreWord(player.getUniqueId(), word)) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "ignoreword_added", "{word}", word));
            } else {
                player.sendMessage(
                        plugin.getConfigManager().getMessage(player, "ignoreword_already_exists", "{word}", word));
            }
            return true;

        } else if (label.equalsIgnoreCase("delignoreword")) {
            if (!player.hasPermission("nhancore.command.delignoreword")) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "no_permission"));
                return true;
            }
            if (args.length < 1) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "delignoreword_usage"));
                return true;
            }
            String word = args[0];
            if (ignoreManager.removeIgnoreWord(player.getUniqueId(), word)) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "ignoreword_removed", "{word}", word));
            } else {
                player.sendMessage(
                        plugin.getConfigManager().getMessage(player, "ignoreword_not_found", "{word}", word));
            }
            return true;
        } else if (label.equalsIgnoreCase("ignorewordlist")) {
            if (!player.hasPermission("nhancore.command.ignorewordlist")) {
                player.sendMessage(plugin.getConfigManager().getMessage(player, "no_permission"));
                return true;
            }
            showList(player);
            return true;
        }

        return false;
    }

    private void showList(Player player) {
        Set<String> list = plugin.getIgnoreManager().getIgnoreWordList(player.getUniqueId());
        if (list.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage(player, "ignorewordlist_empty"));
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage(player, "ignorewordlist_header"));
            for (String s : list) {
                player.sendMessage(Component.text("- " + s, NamedTextColor.WHITE));
            }
        }
    }
}
