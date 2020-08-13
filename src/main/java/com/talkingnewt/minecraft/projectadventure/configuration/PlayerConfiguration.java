package com.talkingnewt.minecraft.projectadventure.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.talkingnewt.minecraft.projectadventure.configuration.entities.Ghost;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerConfiguration extends Configuration {
    public Map<UUID, Ghost> ghosts = new HashMap<>();
    public final String m_ghosts_path = "ghosts";

    public PlayerConfiguration(JavaPlugin parent, String configName) {
        super(parent, configName);
    }

    @Override
    public boolean save() {
        try {
            config().set(m_ghosts_path, new Gson().toJson(ghosts));
            config().save(configFile());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean load() {
        try {
            Map<UUID, Ghost> loadedGhosts = new Gson().fromJson(config().getString(m_ghosts_path), new TypeToken<HashMap<UUID, Ghost>>() {
            }.getType());

            if (loadedGhosts != null) {
                ghosts = loadedGhosts;
            }

            return true;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }
}
