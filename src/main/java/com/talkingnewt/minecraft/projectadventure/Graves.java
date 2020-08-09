package com.talkingnewt.minecraft.projectadventure;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Graves implements Listener {
    final private Map<String, Set<Location>> m_graves = new HashMap<>();
    GraveGenerator m_generator = new GraveGenerator();

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

    private Set<Location> worldGraves(World world) {
        return m_graves.computeIfAbsent(world.getName(), s -> new HashSet<>());
    }

    private void addGrave(World world, Location location) {
        Set<Location> graves = worldGraves(world);

        graves.add(location);
        m_graves.put(world.getName(), graves);
    }

    public Graves() {
        if (m_generator.load()) {
            Bukkit.getLogger().warning("Loaded grave generator.");
        } else {
            Bukkit.getLogger().warning("Failed to load grave generator.");
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        final var chunk = event.getChunk();

        if (event.isNewChunk() && m_generator.shouldGenerate(chunk, worldGraves(chunk.getWorld()))) {
            m_generator.generate(chunk);
        }

        graveLocation(chunk).ifPresent(location -> addGrave(chunk.getWorld(), location));
    }

    @EventHandler
    public void onChunkUnload(@NotNull ChunkUnloadEvent event) {
        final var chunk = event.getChunk();

        final var graves = worldGraves(chunk.getWorld());
        if (graves.isEmpty()) {
            return;
        }

        graveLocation(chunk).ifPresent(graves::remove);
    }

    public Optional<Location> graveLocation(@NotNull Chunk chunk) {
        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < chunk.getWorld().getMaxHeight(); ++y) {
                for (int z = 0; z < 16; ++z) {
                    if (chunk.getBlock(x, y, z).getType() == Material.DRAGON_HEAD) {
                        return Optional.of(chunk.getBlock(x, y, z).getLocation());
                    }
                }
            }
        }

        return Optional.empty();
    }

    public Optional<Location> nearestGrave(Player player, Set<Location> knownGraves) {
        DistanceToPlayerComparator comparator = new DistanceToPlayerComparator(player);

        for (Location knownGrave : knownGraves) {
            Bukkit.getLogger().info("Player " + player.getLocation().toString() + " grave " + knownGrave.toString());
        }
        return knownGraves.stream().min(comparator);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final var player = event.getPlayer();
        final var graveLocationOpt = nearestGrave(player, worldGraves(player.getWorld()));

        if (graveLocationOpt.isPresent()) {
            Bukkit.getLogger().info("Teleport player to nearest grave at: " + graveLocationOpt.get().toString());
            event.setRespawnLocation(graveLocationOpt.get());
        } else {
            Bukkit.getLogger().info("No grave found, teleport player to spawn");
            event.setRespawnLocation(player.getWorld().getSpawnLocation());
        }
    }

}
