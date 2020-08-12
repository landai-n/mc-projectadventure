package com.talkingnewt.minecraft.projectadventure;

import com.talkingnewt.minecraft.projectadventure.configuration.WorldConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ProjectAdventure extends JavaPlugin {
    WorldConfiguration m_worldConfiguration = new WorldConfiguration(this, "world-config");

    @Override
    public void onEnable() {

        if (!m_worldConfiguration.open() || !m_worldConfiguration.load()) {
            getLogger().warning("Failed to enable ProjectAdventure.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new Graves(m_worldConfiguration.graveLocations), this);
    }

    @Override
    public void onDisable() {
        if (!m_worldConfiguration.save()) {
            getLogger().warning("Failed to save world configuration.");
        }
    }
}
