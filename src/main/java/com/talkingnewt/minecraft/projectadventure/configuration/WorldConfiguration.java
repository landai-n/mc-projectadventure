package com.talkingnewt.minecraft.projectadventure.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

import static com.talkingnewt.minecraft.projectadventure.configuration.GsonLocation.fromGson;
import static com.talkingnewt.minecraft.projectadventure.configuration.GsonLocation.toGson;

public class WorldConfiguration {
    private final JavaPlugin m_parent;
    private final String m_dbFilePath;
    private File m_configFile;
    private FileConfiguration m_config;

    public Set<Location> graveLocations = new HashSet<>();
    public final String m_graveLocations_path = "grave-locations";

    public WorldConfiguration(JavaPlugin parent, String configName) {
        m_parent = parent;
        m_dbFilePath = configName + ".yml";
    }

    public boolean open() {
        m_configFile = new File(m_parent.getDataFolder(), m_dbFilePath);
        if (!m_configFile.exists()) {
            m_parent.getLogger().info("No configuration file " + m_dbFilePath + " found. It will now be generated.");
            try {
                if (!m_configFile.getParentFile().mkdirs() && ! m_configFile.createNewFile()) {
                    throw new IOException();
                }
            } catch (IOException e) {
                m_parent.getLogger().warning("Unable to create configuration file.");
                return false;
            }
        }

        m_config = new YamlConfiguration();
        try {
            m_config.load(m_configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean save() {
        try {
            m_parent.getLogger().info("Save configuration file " + m_dbFilePath);
            m_config.set(m_graveLocations_path, new Gson().toJson(toGson(graveLocations)));
            m_config.save(m_configFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean load() {
        try {
            HashSet<GsonLocation> gsonLocations = new Gson().fromJson(m_config.getString(m_graveLocations_path), new TypeToken<HashSet<GsonLocation>>() {
            }.getType());

            if (gsonLocations != null) {
                graveLocations = fromGson(gsonLocations);
            }

            return true;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

}
