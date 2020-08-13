package com.talkingnewt.minecraft.projectadventure.helpers;

import org.bukkit.Location;

import java.util.Comparator;

public class DistanceComparator2D implements Comparator<Location> {
    final private Location m_location;

    public DistanceComparator2D(Location location) {
        m_location = location;
    }

    public int compare(Location a, Location b) {
        var distA = distance(m_location, a);
        var distB = distance(m_location, b);

        return Integer.compare(distA, distB);
    }

    static public int distance(Location originLocation, Location destLocation) {
        double distance = Math.abs(Math.sqrt(Math.pow(originLocation.getBlockX() - destLocation.getX(), 2) + Math.pow(originLocation.getBlockZ() - destLocation.getZ(), 2)));

        return (int) Math.round(distance);
    }
}