package io.github.orasan.HANCORE.commands;

import io.github.orasan.HANCORE.NHANCORE;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SuicideCommand implements CommandExecutor {

    private final NHANCORE plugin;

    public SuicideCommand(NHANCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (sender instanceof Player) {
            if (!sender.hasPermission("nhancore.command.suicide")) {
                sender.sendMessage(plugin.getConfigManager().getMessage(sender, "no_permission"));
                return true;
            }
            Player player = (Player) sender;
            player.setHealth(0);
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage(sender, "player_only"));
        }
        return true;
    }
}
