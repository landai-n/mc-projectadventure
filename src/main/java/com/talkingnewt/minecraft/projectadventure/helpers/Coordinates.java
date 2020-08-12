package com.talkingnewt.minecraft.projectadventure.helpers;

import org.bukkit.Chunk;

public class Coordinates {
    static public final int chunkSize = 16;

    public static class ChunkRelative {
        static public int x_toWorld(Chunk chunk, int relativeX) {
            return chunk.getX() * chunkSize + relativeX;
        }
        static public int z_toWorld(Chunk chunk, int relativeZ) {
            return chunk.getZ() * chunkSize + relativeZ;
        }
    }
}
