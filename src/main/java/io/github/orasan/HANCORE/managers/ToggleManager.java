package io.github.orasan.HANCORE.managers;

import io.github.orasan.HANCORE.NHANCORE;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ToggleManager {

    private final NHANCORE plugin;
    private final Map<UUID, Map<String, Boolean>> toggleCache = new HashMap<>();
    private static final String CONFIG_NAME = "userdata.yml";

    public enum ToggleType {
        SHOW_DEATH_MESSAGES("death_msgs", true), // true = show, false = hide

        SHOW_ADVANCEMENTS("show_advancements", true), // Receive others' advancements
        BROADCAST_MY_ADVANCEMENTS("broadcast_my_advancements", true), // Broadcast my advancements

        GLOBAL_CHAT("global_chat", true); // Show chat from others

        private final String key;
        private final boolean defaultValue;

        ToggleType(String key, boolean defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getKey() {
            return key;
        }

        public boolean getDefaultValue() {
            return defaultValue;
        }
    }

    public ToggleManager(NHANCORE plugin) {
        this.plugin = plugin;
        loadToggles();
    }

    public void reload() {
        loadToggles();
    }

    public boolean getToggle(UUID uuid, ToggleType type) {
        return toggleCache.computeIfAbsent(uuid, k -> new HashMap<>())
                .getOrDefault(type.getKey(), type.getDefaultValue());
    }

    public void setToggle(UUID uuid, ToggleType type, boolean value) {
        toggleCache.computeIfAbsent(uuid, k -> new HashMap<>()).put(type.getKey(), value);
        saveTogglesAsync();
    }

    public boolean toggle(UUID uuid, ToggleType type) {
        boolean current = getToggle(uuid, type);
        boolean newState = !current;
        setToggle(uuid, type, newState);
        return newState;
    }

    private void loadToggles() {
        FileConfiguration config = plugin.getConfigManager().getConfig(CONFIG_NAME);
        if (config.contains("users")) {
            for (String key : config.getConfigurationSection("users").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    for (ToggleType type : ToggleType.values()) {
                        if (config.contains("users." + key + "." + type.getKey())) {
                            boolean val = config.getBoolean("users." + key + "." + type.getKey());
                            toggleCache.computeIfAbsent(uuid, k -> new HashMap<>()).put(type.getKey(), val);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Ignore invalid UUIDs
                }
            }
        }
    }

    public void saveToggles() {
        FileConfiguration config = plugin.getConfigManager().getConfig(CONFIG_NAME);
        config.set("users", null); // Clear to key it fresh avoid zombies? Or just update.
        // Clearing is safer for removing old keys if we wanted, but for user perks we
        // just overwrite.
        // Actually, let's just overwrite relevant keys.

        for (Map.Entry<UUID, Map<String, Boolean>> entry : toggleCache.entrySet()) {
            String path = "users." + entry.getKey().toString();
            for (Map.Entry<String, Boolean> val : entry.getValue().entrySet()) {
                config.set(path + "." + val.getKey(), val.getValue());
            }
        }
        plugin.getConfigManager().saveConfig(CONFIG_NAME);
    }

    private void saveTogglesAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveToggles);
    }
}
