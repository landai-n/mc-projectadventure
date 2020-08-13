package com.talkingnewt.minecraft.projectadventure.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.talkingnewt.minecraft.projectadventure.configuration.entities.GsonLocation;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

import static com.talkingnewt.minecraft.projectadventure.configuration.entities.GsonLocation.fromGson;
import static com.talkingnewt.minecraft.projectadventure.configuration.entities.GsonLocation.toGson;

public class WorldConfiguration extends Configuration {
    public Set<Location> graveLocations = new HashSet<>();
    private final String m_graveLocations_path = "grave-locations";

    public WorldConfiguration(JavaPlugin parent, String configName) {
        super(parent, configName);
    }

    @Override
    public boolean save() {
        try {
            config().set(m_graveLocations_path, new Gson().toJson(toGson(graveLocations)));
            config().save(configFile());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean load() {
        try {
            HashSet<GsonLocation> gsonLocations = new Gson().fromJson(config().getString(m_graveLocations_path), new TypeToken<HashSet<GsonLocation>>() {
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
