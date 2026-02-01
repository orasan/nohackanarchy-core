package io.github.orasan.HANCORE.commands;

import io.github.orasan.HANCORE.NHANCORE;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CoreCommand implements CommandExecutor {

    private final NHANCORE plugin;

    public CoreCommand(NHANCORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull @SuppressWarnings("unused") Command command,
            @NotNull @SuppressWarnings("unused") String label,
            @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nhancore.admin")) {
                sender.sendMessage(Component.text("権限がありません。", NamedTextColor.RED));
                return true;
            }

            sender.sendMessage(plugin.getConfigManager().getMessage(sender, "reloading"));
            if (plugin.getConfigManager().reloadAll()) {
                plugin.reloadManagers();
                sender.sendMessage(plugin.getConfigManager().getMessage(sender, "reload_success"));
            } else {
                sender.sendMessage(plugin.getConfigManager().getMessage(sender, "reload_error"));
            }
            return true;
        }

        if (!sender.hasPermission("nhancore.command.core")) {
            sender.sendMessage(plugin.getConfigManager().getMessage(sender, "no_permission"));
            return true;
        }

        sender.sendMessage(
                Component.text("NohackAnarchy-Core v" + plugin.getPluginMeta().getVersion(), NamedTextColor.AQUA));
        return true;
    }
}
