package com.talkingnewt.minecraft.projectadventure;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

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
    final static int s_graveChunkInterval = 18;
    private boolean m_isGenerating = false;

    public boolean load() {
        return m_structGenerator.load("grave.struct");
    }

    static public int chunkDistance(Chunk originChunk, Location graveLocation) {
        double distance = Math.abs(Math.sqrt(Math.pow(originChunk.getX()- graveLocation.getX() / 16, 2) + Math.pow(originChunk.getZ() - graveLocation.getZ() / 16, 2)));

        return (int) Math.round(distance);
    }


    public boolean shouldGenerate(Chunk originChunk, @NotNull Set<Location> knownGraves) {
        if (m_isGenerating) {
            return false;
        }

        var nearestGraveLocationOpt = nearestGraveLocation(originChunk, knownGraves);

        if (nearestGraveLocationOpt.isPresent() && chunkDistance(originChunk, nearestGraveLocationOpt.get()) > s_graveChunkInterval) {
            Bukkit.getLogger().info("Nearest grave is " + chunkDistance(originChunk, nearestGraveLocationOpt.get()) + " chunk away.");
        }
        return (nearestGraveLocationOpt.isEmpty() || chunkDistance(originChunk, nearestGraveLocationOpt.get()) > s_graveChunkInterval);
    }

    public Optional<Location> nearestGraveLocation(Chunk originChunk, Set<Location> knownGraves) {
        DistanceToChunkComparator comparator = new DistanceToChunkComparator(originChunk);

        return knownGraves.stream().min(comparator);
    }

    public boolean isPossibleGround(Block block) {
        Material[] blacklist = {
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.CRIMSON_STEM, Material.WARPED_STEM, Material.AIR
        };

        if (Arrays.stream(blacklist).anyMatch(material -> material == block.getType())) {
            return false;
        }

        if (block.isLiquid()) {
            return true;
        }

        if (block.isEmpty() || !block.getType().isSolid() || !block.getType().isOccluding()) {
            return false;
        }

        return true;
    }

    public int findGroundY(Chunk chunk) {
        for (int y = chunk.getWorld().getMaxHeight() - 1; y > 0; --y) {
            if (isPossibleGround(chunk.getBlock(0, y, 0))) {
                return y;
            }
        }

        return 64;
    }

    public void generate(Chunk originChunk) {
        m_isGenerating = true;
        Location graveLocation = new Location(originChunk.getWorld(), originChunk.getX() * 16, findGroundY(originChunk) + 1, originChunk.getZ() * 16);
        m_structGenerator.generate(graveLocation);
        Bukkit.getLogger().info("New grave generated at " + graveLocation.toString());
        m_isGenerating = false;
    }
}
