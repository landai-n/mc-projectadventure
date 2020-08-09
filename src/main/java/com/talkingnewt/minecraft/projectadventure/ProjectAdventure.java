package com.talkingnewt.minecraft.projectadventure;

import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class ProjectAdventure extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Graves(), this);
    }

    @Override
    public void onDisable() {
    }
}
