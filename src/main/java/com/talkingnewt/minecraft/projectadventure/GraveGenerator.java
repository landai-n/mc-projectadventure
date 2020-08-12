package com.talkingnewt.minecraft.projectadventure;

import com.talkingnewt.minecraft.projectadventure.helpers.Coordinates;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GraveGenerator {
    public static class DistanceToChunkComparator implements Comparator<Location> {
        final private Chunk m_chunk;

        public DistanceToChunkComparator(Chunk chunk) {
            m_chunk = chunk;
        }

        public int compare(Location a, Location b) {
            var distA = chunkDistance(m_chunk, a);
            var distB = chunkDistance(m_chunk, b);

            return Integer.compare(distA, distB);
        }
    }

    final StructGenerator m_structGenerator = new StructGenerator();
    final static int s_normalGraveChunkInterval = 32;
    final static int s_netherGraveChunkInterval = 20;
    private boolean m_isLoaded = false;

    public boolean isLoaded() {
        return m_isLoaded;
    }

    public boolean load() {
        m_isLoaded = m_structGenerator.load(World.Environment.NORMAL.name(), "grave-overworld.struct");
        m_isLoaded &= m_structGenerator.load(World.Environment.NETHER.name(), "grave-nether.struct");
        return isLoaded();
    }

    static public int chunkDistance(Chunk originChunk, Location graveLocation) {
        double distance = Math.abs(Math.sqrt(Math.pow(originChunk.getX()- graveLocation.getX() / 16, 2) + Math.pow(originChunk.getZ() - graveLocation.getZ() / 16, 2)));

        return (int) Math.round(distance);
    }

    public boolean shouldGenerate(Chunk originChunk, @NotNull Set<Location> knownGraves) {
        var nearestGraveLocationOpt = nearestGraveLocation(originChunk, knownGraves);

        if (originChunk.getWorld().getEnvironment() == World.Environment.NORMAL) {
            return (nearestGraveLocationOpt.isEmpty() || chunkDistance(originChunk, nearestGraveLocationOpt.get()) > s_normalGraveChunkInterval);
        } else if (originChunk.getWorld().getEnvironment() == World.Environment.NETHER) {
            return (nearestGraveLocationOpt.isEmpty() || chunkDistance(originChunk, nearestGraveLocationOpt.get()) > s_netherGraveChunkInterval);
        }

        return false;
    }

    private Optional<Location> nearestGraveLocation(Chunk originChunk, Set<Location> knownGraves) {
        DistanceToChunkComparator comparator = new DistanceToChunkComparator(originChunk);

        return knownGraves.stream().min(comparator);
    }

    private boolean isPossibleGround(Block block) {
        Material[] blacklist = {
                Material.OAK_LOG,
                Material.SPRUCE_LOG,
                Material.BIRCH_LOG,
                Material.JUNGLE_LOG,
                Material.ACACIA_LOG,
                Material.DARK_OAK_LOG,
                Material.CRIMSON_STEM,
                Material.WARPED_STEM,
                Material.AIR,
                Material.BEDROCK
        };

        if (Arrays.stream(blacklist).anyMatch(material -> material == block.getType())) {
            return false;
        }

        if (block.isEmpty() || !block.getType().isSolid() || !block.getType().isOccluding()) {
            return false;
        }

        return true;
    }

    private boolean isPossibleGround(StructGenerator.Dimensions dimensions, Location location) {
        for (int x = 0; x < dimensions.width; x++) {
            for (int z = 0; z < dimensions.length; z++) {
                var block = Objects.requireNonNull(location.getWorld()).getBlockAt(location.getBlockX() + x, location.getBlockY(), location.getBlockZ() + z);

                if (!isPossibleGround(block)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Optional<Location> findPossibleLocation(Chunk chunk, String structId) {
        var dimensions = m_structGenerator.structDimensions(structId);

        for (int chunkX = 0; chunkX < Coordinates.chunkSize; chunkX++) {
            for (int chunkZ = 0; chunkZ < Coordinates.chunkSize; chunkZ++) {
                int worldX = Coordinates.ChunkRelative.x_toWorld(chunk, chunkX);
                int worldZ = Coordinates.ChunkRelative.z_toWorld(chunk, chunkZ);

                int highestBlockY = 128;//chunk.getWorld().getHighestBlockYAt(new Location(chunk.getWorld(), worldX, 256, worldZ));
                for (int y = highestBlockY; y > 1; --y) {
                    var block = chunk.getBlock(chunkX, y, chunkZ);
                    var groundBlock = chunk.getBlock(chunkX, y - 1, chunkZ);
                    if (isPossibleGround(dimensions, groundBlock.getLocation()) && hasEnoughSpace(dimensions, block.getLocation())) {
                        return Optional.of(new Location(chunk.getWorld(), worldX, y, worldZ));
                    }
                }
            }
        }

        return Optional.empty();
    }

    private boolean hasEnoughSpace(StructGenerator.Dimensions dimensions, Location location) {
        for (int x = 0; x < dimensions.width; x++) {
            for (int z = 0; z < dimensions.length; z++) {
                for (int y = 0; y < dimensions.height; y++) {

                    var block = Objects.requireNonNull(location.getWorld()).getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);

                    if (block.getType().isSolid() || block.isLiquid()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }


    public Optional<Location> generate(Chunk originChunk) {
        String structId = Objects.requireNonNull(originChunk.getWorld()).getEnvironment().name();
        var graveLocationOpt = findPossibleLocation(originChunk, structId);

        if (graveLocationOpt.isEmpty() || !m_structGenerator.generate(structId, graveLocationOpt.get())) {
            return Optional.empty();
        }

        Bukkit.getLogger().info("New grave generated at " + graveLocationOpt.get().toString());
        return graveLocationOpt;
    }
}
