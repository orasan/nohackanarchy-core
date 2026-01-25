package io.github.orasan.HANCORE.managers;

import io.github.orasan.HANCORE.NHANCORE;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class IgnoreManager {

    private final NHANCORE plugin;
    private final Map<UUID, Map<UUID, String>> ignoredPlayers = new HashMap<>(); // UUID -> TargetUUID, TargetName
    private final Map<UUID, Set<String>> ignoredWords = new HashMap<>(); // Strings
    private static final String CONFIG_NAME = "ignore_data.yml";

    public IgnoreManager(NHANCORE plugin) {
        this.plugin = plugin;
        loadIgnores();
    }

    public void reload() {
        loadIgnores();
    }

    // --- Players ---
    public boolean addIgnorePlayer(UUID playerUUID, UUID targetUUID, String targetName) {
        Map<UUID, String> map = ignoredPlayers.computeIfAbsent(playerUUID, k -> new HashMap<>());
        if (map.containsKey(targetUUID))
            return false;

        map.put(targetUUID, targetName != null ? targetName : "Unknown");
        saveIgnoresAsync();
        return true;
    }

    public boolean removeIgnorePlayer(UUID playerUUID, UUID targetUUID) {
        if (ignoredPlayers.containsKey(playerUUID)) {
            Map<UUID, String> map = ignoredPlayers.get(playerUUID);
            if (!map.containsKey(targetUUID))
                return false;

            map.remove(targetUUID);
            if (map.isEmpty())
                ignoredPlayers.remove(playerUUID);
            saveIgnoresAsync();
            return true;
        }
        return false;
    }

    public Map<UUID, String> getIgnorePlayerMap(UUID playerUUID) {
        return ignoredPlayers.getOrDefault(playerUUID, Collections.emptyMap());
    }

    // For legacy/simple UUID set access if needed
    public Set<UUID> getIgnorePlayerUUIDs(UUID playerUUID) {
        return getIgnorePlayerMap(playerUUID).keySet();
    }

    // --- Words ---
    public boolean addIgnoreWord(UUID playerUUID, String word) {
        Set<String> set = ignoredWords.computeIfAbsent(playerUUID, k -> new HashSet<>());
        if (set.contains(word))
            return false;
        set.add(word);
        saveIgnoresAsync();
        return true;
    }

    public boolean removeIgnoreWord(UUID playerUUID, String word) {
        if (ignoredWords.containsKey(playerUUID)) {
            Set<String> set = ignoredWords.get(playerUUID);
            if (!set.contains(word))
                return false;
            set.remove(word);
            if (set.isEmpty())
                ignoredWords.remove(playerUUID);
            saveIgnoresAsync();
            return true;
        }
        return false;
    }

    public Set<String> getIgnoreWordList(UUID playerUUID) {
        return ignoredWords.getOrDefault(playerUUID, Collections.emptySet());
    }

    // --- Check ---
    public boolean isIgnored(UUID recipient, UUID senderUUID, String message) {
        // Check ignored players (by UUID)
        if (ignoredPlayers.containsKey(recipient)) {
            if (ignoredPlayers.get(recipient).containsKey(senderUUID))
                return true;
        }

        // Check ignored words
        if (ignoredWords.containsKey(recipient)) {
            Set<String> words = ignoredWords.get(recipient);
            for (String w : words) {
                if (message.contains(w))
                    return true;
            }
        }
        return false;
    }

    private void loadIgnores() {
        ignoredPlayers.clear();
        ignoredWords.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig(CONFIG_NAME);

        // New Structure: users.<uuid>.players (Section: uuid: name) / words (List)
        if (config.contains("users")) {
            for (String key : config.getConfigurationSection("users").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);

                    // Players
                    String playerPath = "users." + key + ".players";
                    if (config.isConfigurationSection(playerPath)) {
                        ConfigurationSection section = config.getConfigurationSection(playerPath);
                        Map<UUID, String> pMap = new HashMap<>();
                        for (String targetKey : section.getKeys(false)) {
                            try {
                                UUID targetUUID = UUID.fromString(targetKey);
                                String name = section.getString(targetKey, "Unknown");
                                pMap.put(targetUUID, name);
                            } catch (Exception ignored) {
                            }
                        }
                        if (!pMap.isEmpty())
                            ignoredPlayers.put(uuid, pMap);
                    } else if (config.isList(playerPath)) {
                        // Migration from List
                        List<String> pList = config.getStringList(playerPath);
                        Map<UUID, String> pMap = new HashMap<>();
                        for (String p : pList) {
                            try {
                                UUID targetUUID = UUID.fromString(p);
                                // Best effort lookup
                                OfflinePlayer op = Bukkit.getOfflinePlayer(targetUUID);
                                String name = op.getName();
                                pMap.put(targetUUID, name != null ? name : "Unknown");
                            } catch (Exception ignored) {
                            }
                        }
                        if (!pMap.isEmpty())
                            ignoredPlayers.put(uuid, pMap);
                    }

                    // Words
                    List<String> wList = config.getStringList("users." + key + ".words");
                    if (!wList.isEmpty())
                        ignoredWords.put(uuid, new HashSet<>(wList));

                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in ignore_data.yml: " + key);
                }
            }
        }

        // Migration: Old "players" key (legacy) being loaded as words
        if (config.contains("players")) {
            for (String key : config.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    List<String> list = config.getStringList("players." + key);
                    if (!list.isEmpty()) {
                        Set<String> set = ignoredWords.computeIfAbsent(uuid, k -> new HashSet<>());
                        set.addAll(list);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void saveIgnores() {
        FileConfiguration config = plugin.getConfigManager().getConfig(CONFIG_NAME);
        config.set("players", null); // Clear legacy
        config.set("users", null); // Clear new to rebuild

        // Merge UUID set of all keys
        Set<UUID> allKeys = new HashSet<>();
        allKeys.addAll(ignoredPlayers.keySet());
        allKeys.addAll(ignoredWords.keySet());

        for (UUID uuid : allKeys) {
            String path = "users." + uuid.toString();

            if (ignoredPlayers.containsKey(uuid)) {
                Map<UUID, String> map = ignoredPlayers.get(uuid);
                // Save as map/section
                for (Map.Entry<UUID, String> entry : map.entrySet()) {
                    config.set(path + ".players." + entry.getKey().toString(), entry.getValue());
                }
            }

            if (ignoredWords.containsKey(uuid)) {
                config.set(path + ".words", new ArrayList<>(ignoredWords.get(uuid)));
            }
        }
        plugin.getConfigManager().saveConfig(CONFIG_NAME);
    }

    // Async save to avoid blocking main thread on every command
    private void saveIgnoresAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveIgnores);
    }
}
