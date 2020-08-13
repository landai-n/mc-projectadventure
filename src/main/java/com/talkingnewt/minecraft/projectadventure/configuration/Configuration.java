package com.talkingnewt.minecraft.projectadventure.configuration;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
public class Configuration {
    protected final JavaPlugin m_parent;
    private final String m_dbFilePath;
    private File m_configFile;
    private FileConfiguration m_config;

    public Configuration(JavaPlugin parent, String configName) {
        m_parent = parent;
        m_dbFilePath = configName + ".yml";
    }

    protected File configFile() { return m_configFile; }
    protected FileConfiguration config() { return m_config; }

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
        return true;
    }

    public boolean load() {
        return true;
    }

}
