package io.github.orasan.HANCORE;

import io.github.orasan.HANCORE.commands.CoreCommand;
import io.github.orasan.HANCORE.commands.IgnoreCommand;
import io.github.orasan.HANCORE.commands.IgnoreWordCommand;
import io.github.orasan.HANCORE.commands.SuicideCommand;
import io.github.orasan.HANCORE.commands.ToggleCommand;
import io.github.orasan.HANCORE.listeners.ChatListener;
import io.github.orasan.HANCORE.listeners.PlayerListener;
import io.github.orasan.HANCORE.managers.ConfigManager;
import io.github.orasan.HANCORE.managers.IgnoreManager;
import io.github.orasan.HANCORE.managers.ToggleManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NHANCORE extends JavaPlugin {

    private ConfigManager configManager;
    private IgnoreManager ignoreManager;
    private ToggleManager toggleManager;
    private PlayerListener playerListener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Nohackanarchy-coreが起動しました");

        // Config Manager initialization
        this.configManager = new ConfigManager(this);
        this.ignoreManager = new IgnoreManager(this);
        this.toggleManager = new ToggleManager(this);

        // Commands
        this.getCommand("suicide").setExecutor(new SuicideCommand());

        IgnoreCommand ignoreCmd = new IgnoreCommand(this);
        this.getCommand("ignore").setExecutor(ignoreCmd);
        this.getCommand("delignore").setExecutor(ignoreCmd);
        this.getCommand("ignorelist").setExecutor(ignoreCmd);

        IgnoreWordCommand ignoreWordCmd = new IgnoreWordCommand(this);
        this.getCommand("ignoreword").setExecutor(ignoreWordCmd);
        this.getCommand("delignoreword").setExecutor(ignoreWordCmd);
        this.getCommand("ignorewordlist").setExecutor(ignoreWordCmd);

        ToggleCommand toggleCmd = new ToggleCommand(this);
        this.getCommand("toggleadv").setExecutor(toggleCmd);
        this.getCommand("toggledeathmsgs").setExecutor(toggleCmd);
        this.getCommand("togglechat").setExecutor(toggleCmd);

        this.getCommand("nhancore").setExecutor(new CoreCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        this.playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Nohackanarchy-coreが停止しました");
    }

    public void reloadManagers() {
        if (this.ignoreManager != null) {
            this.ignoreManager.reload();
        } else {
            this.ignoreManager = new IgnoreManager(this);
        }

        if (this.toggleManager != null) {
            this.toggleManager.reload();
        } else {
            this.toggleManager = new ToggleManager(this);
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public IgnoreManager getIgnoreManager() {
        return ignoreManager;
    }

    public ToggleManager getToggleManager() {
        return toggleManager;
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }
}
