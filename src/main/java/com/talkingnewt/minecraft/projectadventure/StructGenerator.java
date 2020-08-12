package com.talkingnewt.minecraft.projectadventure;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class StructGenerator {
    public static class Struct {
        final public Material[][][] materials;
        final public Dimensions dimensions;

        public Struct(Material[][][] materials, Dimensions dimensions) {
            this.materials = materials;
            this.dimensions = dimensions;
        }
    }

    private final Map<String, Struct> m_structArrayMap = new HashMap<>();

    public static class Dimensions {
        final public int width;  // x
        final public int length;  // z
        final public int height;  // y

        public Dimensions(int width, int height, int length) {
            this.width = width;
            this.height = height;
            this.length = length;
        }
    }

    public boolean load(@NotNull String id, @NotNull String structFilePath) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(structFilePath);

        if (stream != null) {
            InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            try {
                return parse(id, reader);
            } catch (IOException e) {
                Bukkit.getLogger().warning(e.getMessage());
                return false;
            }
        } else {
            Bukkit.getLogger().warning("Structure file not found.");
            return false;
        }
    }

    private Optional<Dimensions> parseDimensions(String line) {
        final String[] values = line.split(",");

        if (values.length != 3) {
            return Optional.empty();
        }

        try {
            return Optional.of(new Dimensions(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2])));
        } catch(NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Material parseMaterial(String materialName) {
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e){
            return Material.AIR;
        }
    }

    private boolean parse(String id, BufferedReader fileReader) throws IOException {
        final var dimensionsOpt = parseDimensions(fileReader.readLine());
        if (dimensionsOpt.isEmpty()) {
            Bukkit.getLogger().warning("Failed to parse structure dimensions.");
            return false;
        }

        Dimensions dimensions = dimensionsOpt.get();

        var structArray = new Material[dimensions.width][dimensions.height][dimensions.length];
        int y = 0;
        int z = 0;

        String line;
        while ((line = fileReader.readLine()) != null) {
            if (line.isEmpty()) continue;

            String[] values = line.split(",");

            if (values.length != dimensions.width) {  // Struct dimensions description doesn't match provided blocks width
                Bukkit.getLogger().warning("Struct dimensions description doesn't match provided blocks width.");
                return false;
            }

            for (int x = 0; x < dimensions.width; ++x) {
                structArray[x][y][z] = parseMaterial(values[x]);
            }

            z++;
            if (z >= dimensions.length) {
                z = 0;
                y++;
            }
        }

        if (y != dimensions.height) {  // Struct dimensions description doesn't match provided blocks height or length
            Bukkit.getLogger().warning("Struct dimensions description doesn't match provided blocks height or length.");
            return false;
        }

        m_structArrayMap.put(id, new Struct(structArray, dimensions));
        return true;
    }

    public Dimensions structDimensions(String id) {
        Struct struct = m_structArrayMap.get(id);

        if (struct == null) {
            return new Dimensions(0, 0, 0);
        }

        return struct.dimensions;
    }

    public boolean generate(String id, Location location) {
        var struct = m_structArrayMap.get(id);
        if (struct == null) {
            return false;
        }

        final int minX = location.getBlockX();
        final int minZ = location.getBlockZ();
        final int maxX = minX + struct.dimensions.width;
        final int minY = location.getBlockY();
        final int maxY = minY + struct.dimensions.height;
        final int maxZ = minZ + struct.dimensions.length;

        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    Objects.requireNonNull(location.getWorld()).getBlockAt(x, y, z).setType(struct.materials[x - minX][y - minY][z - minZ]);
                }
            }
        }
        return true;
    }

}
