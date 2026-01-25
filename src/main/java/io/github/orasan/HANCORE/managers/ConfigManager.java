package io.github.orasan.HANCORE.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.orasan.HANCORE.NHANCORE;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private final NHANCORE plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();

    private final Map<String, Map<String, String>> langCache = new HashMap<>();
    private String defaultLang;

    public ConfigManager(NHANCORE plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        reloadAll();
    }

    public Component getMessage(CommandSender sender, String key) {
        String locale = defaultLang;
        if (sender instanceof Player) {
            locale = ((Player) sender).locale().toString().toLowerCase().replace("-", "_");
        }

        // Try precise match (e.g. en_us)
        String msg = getRawMessage(locale, key);
        if (msg == null) {
            // Try default lang
            msg = getRawMessage(defaultLang, key);
        }
        if (msg == null) {
            // Try base default (ja_jp)? Or just return key
            msg = getRawMessage("ja_jp", key);
        }

        if (msg == null)
            return Component.text(key);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    public Component getMessage(CommandSender sender, String key, String placeholder, String value) {
        String locale = defaultLang;
        if (sender instanceof Player) {
            locale = ((Player) sender).locale().toString().toLowerCase().replace("-", "_");
        }

        String msg = getRawMessage(locale, key);
        if (msg == null)
            msg = getRawMessage(defaultLang, key);
        if (msg == null)
            msg = getRawMessage("ja_jp", key);

        if (msg == null)
            return Component.text(key);
        msg = msg.replace(placeholder, value);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    // Helper to get raw string from cache
    private String getRawMessage(String locale, String key) {
        if (langCache.containsKey(locale)) {
            Map<String, String> map = langCache.get(locale);
            if (map != null)
                return map.get(key);
        }
        return null;
    }

    public boolean reloadAll() {
        try {
            plugin.reloadConfig();
            for (String name : configs.keySet()) {
                reloadCustomConfig(name);
            }
            // Reload Languages
            loadLanguages();
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload configuration", e);
            return false;
        }
    }

    private void loadLanguages() {
        langCache.clear();
        defaultLang = plugin.getConfig().getString("default-lang", "ja_jp");

        // Load built-in langs
        loadLangResource("ja_jp");
        loadLangResource("en_us");
    }

    private void loadLangResource(String lang) {
        String filename = "lang/" + lang + ".json";
        InputStream is = plugin.getResource(filename);
        if (is == null) {
            plugin.getLogger().warning("Language resource not found: " + filename);
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            Map<String, String> map = gson.fromJson(reader, new TypeToken<Map<String, String>>() {
            }.getType());
            langCache.put(lang, map);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load language: " + lang);
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig(String name) {
        if (!configs.containsKey(name)) {
            loadCustomConfig(name);
        }
        return configs.get(name);
    }

    public void saveConfig(String name) {
        if (configs.containsKey(name) && configFiles.containsKey(name)) {
            try {
                configs.get(name).save(configFiles.get(name));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + name, e);
            }
        }
    }

    public void saveAll() {
        for (String name : configs.keySet()) {
            saveConfig(name);
        }
    }

    private void loadCustomConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            try {
                if (plugin.getResource(name) != null) {
                    plugin.saveResource(name, false);
                } else {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create config file " + name, e);
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(name, config);
        configFiles.put(name, file);
    }

    private void reloadCustomConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            configs.put(name, config);
        }
    }
}
