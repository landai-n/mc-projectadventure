package com.talkingnewt.minecraft.projectadventure;

import com.talkingnewt.minecraft.projectadventure.configuration.PlayerConfiguration;
import com.talkingnewt.minecraft.projectadventure.configuration.WorldConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ProjectAdventure extends JavaPlugin {
    WorldConfiguration m_worldConfiguration = new WorldConfiguration(this, "world-config");
    PlayerConfiguration m_playerConfiguration = new PlayerConfiguration(this, "player-config");

    @Override
    public void onEnable() {
        if (!m_worldConfiguration.open() || !m_worldConfiguration.load() || !m_playerConfiguration.open() || !m_playerConfiguration.load()) {
            getLogger().warning("Failed to enable ProjectAdventure.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        GraveManager graveManager = new GraveManager(m_worldConfiguration.graveLocations);
        getServer().getPluginManager().registerEvents(graveManager, this);
        getServer().getPluginManager().registerEvents(new GhostManager(this, m_playerConfiguration.ghosts, graveManager), this);
    }

    @Override
    public void onDisable() {
        if (!m_worldConfiguration.save()) {
            getLogger().warning("Failed to save world configuration.");
        }

        if (!m_playerConfiguration.save()) {
            getLogger().warning("Failed to save players configuration.");
        }
    }
}
