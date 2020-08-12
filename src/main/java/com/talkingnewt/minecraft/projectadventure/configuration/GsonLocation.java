package com.talkingnewt.minecraft.projectadventure.configuration;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GsonLocation implements Serializable {
    public int x;
    public int y;
    public int z;
    public String worldName;

    public GsonLocation(String worldName, int x, int y, int z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Set<Location> fromGson(Set<GsonLocation> gsonLocations) {
        HashSet<Location> locations = new HashSet<>();

        gsonLocations.forEach(gsonLocation -> locations.add(new Location(Bukkit.getServer().getWorld(gsonLocation.worldName), gsonLocation.x, gsonLocation.y, gsonLocation.z)));
        return locations;
    }

    public static Set<GsonLocation> toGson(Set<Location> locations) {
        HashSet<GsonLocation> gsonLocations = new HashSet<>();

        locations.forEach(location -> gsonLocations.add(new GsonLocation(Objects.requireNonNull(location.getWorld()).getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ())));
        return gsonLocations;
    }
}