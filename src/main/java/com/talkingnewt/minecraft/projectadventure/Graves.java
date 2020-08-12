package com.talkingnewt.minecraft.projectadventure;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class Graves implements Listener {
    final private Set<Location> m_graveLocations;
    final GraveGenerator m_generator = new GraveGenerator();
    final ReentrantLock m_lock = new ReentrantLock();

    public static class DistanceToPlayerComparator implements Comparator<Location> {
        final private Player m_player;

        public DistanceToPlayerComparator(Player player) {
            m_player = player;
        }

        public int compare(Location a, Location b) {
            var distA = playerDistance(m_player, a);
            var distB = playerDistance(m_player, b);

            return Integer.compare(distA, distB);
        }

        static public int playerDistance(Player player, Location graveLocation) {
            double distance = Math.abs(Math.sqrt(Math.pow(player.getLocation().getBlockX() - graveLocation.getX(), 2) + Math.pow(player.getLocation().getBlockZ() - graveLocation.getZ(), 2)));

            return (int) Math.round(distance);
        }
    }

    public Graves(Set<Location> gravesLocations) {
        m_graveLocations = gravesLocations;

        for (Location m_grave : m_graveLocations) {
            Bukkit.getLogger().info("Load grave at " + m_grave.toString());
        }

        if (m_generator.load()) {
            Bukkit.getLogger().warning("Loaded grave generator.");
        } else {
            Bukkit.getLogger().warning("Failed to load grave generator.");
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!m_generator.isLoaded()) {
            return;
        }

        if (!m_lock.tryLock()) {
            return;
        }

        final var chunk = event.getChunk();

        if (event.isNewChunk() && m_generator.shouldGenerate(chunk, m_graveLocations)) {
            var placeHolder = new Location(chunk.getWorld(), chunk.getX() * 16, 0, chunk.getZ() * 16);
            m_graveLocations.add(placeHolder); //todo remove this shit
            m_lock.unlock();
            m_generator.generate(chunk).ifPresent(m_graveLocations::add);
            m_graveLocations.remove(placeHolder);
        } else {
            m_lock.unlock();
        }
    }

    private Stream<Location> worldGraveLocations(World world) {
        return m_graveLocations.stream().filter(location -> world == location.getWorld());
    }

    private Optional<Location> nearestGrave(Player player) {
        DistanceToPlayerComparator comparator = new DistanceToPlayerComparator(player);

        return worldGraveLocations(player.getWorld()).min(comparator);
    }

    private Location calculateRespawnPoint(Location location) {
        return new Location(location.getWorld(), location.getX(), location.getY() - 3, location.getZ() - 3);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final var player = event.getPlayer();
        final var graveLocationOpt = nearestGrave(player);

        if (graveLocationOpt.isPresent()) {
            Bukkit.getLogger().info("Teleport " + player.getName()  + " to " + graveLocationOpt.get().toString());
            event.setRespawnLocation(calculateRespawnPoint(graveLocationOpt.get()));
        } else {
            Bukkit.getLogger().info("No grave found, teleport "  + player.getName()  +  " to spawn.");
            event.setRespawnLocation(player.getWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        m_lock.lock();
        if (m_graveLocations.stream().anyMatch(location -> location.getChunk() == event.getBlock().getChunk())) {
            event.setCancelled(true);
        }
        m_lock.unlock();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        m_lock.lock();
        if (m_graveLocations.stream().anyMatch(location -> location.getChunk() == event.getBlock().getChunk())) {
            event.setCancelled(true);
        }
        m_lock.unlock();
    }

}
