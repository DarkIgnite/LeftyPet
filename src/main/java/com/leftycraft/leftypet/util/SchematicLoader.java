package com.leftycraft.leftypet.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.StructureRotation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class SchematicLoader {

    public record SchematicBlock(int x, int y, int z, String blockDataString) {}

    public static class Schematic {
        private final int width;
        private final int height;
        private final int length;
        private final int anchorX;
        private final int anchorY;
        private final int anchorZ;
        private final List<SchematicBlock> blocks;

        public Schematic(int width, int height, int length, int anchorX, int anchorY, int anchorZ, List<SchematicBlock> blocks) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.anchorZ = anchorZ;
            this.blocks = blocks;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getLength() { return length; }
        public int getAnchorX() { return anchorX; }
        public int getAnchorY() { return anchorY; }
        public int getAnchorZ() { return anchorZ; }
        public List<SchematicBlock> getBlocks() { return blocks; }

        /**
         * Transforms continuous schematic coordinates to world coordinates based on anchor block center and rotation.
         */
        public Location transformLocation(Location origin, double sx, double sy, double sz, StructureRotation rotation) {
            double rx = sx - (anchorX + 0.5);
            double ry = sy - anchorY;
            double rz = sz - (anchorZ + 0.5);

            double dx;
            double dz;

            switch (rotation) {
                case CLOCKWISE_90 -> { // Facing NORTH
                    dx = -rz;
                    dz = rx;
                }
                case CLOCKWISE_180 -> { // Facing EAST
                    dx = -rx;
                    dz = -rz;
                }
                case COUNTERCLOCKWISE_90 -> { // Facing SOUTH
                    dx = rz;
                    dz = -rx;
                }
                default -> { // NONE (Facing WEST - base schematic)
                    dx = rx;
                    dz = rz;
                }
            }

            return new Location(origin.getWorld(), origin.getBlockX() + 0.5 + dx, origin.getBlockY() + ry, origin.getBlockZ() + 0.5 + dz);
        }

        /**
         * Transforms integer block coordinates to world block coordinates based on anchor and rotation.
         */
        public Location transformBlockLocation(Location origin, int x, int y, int z, StructureRotation rotation) {
            int rx = x - anchorX;
            int ry = y - anchorY;
            int rz = z - anchorZ;

            int dx;
            int dz;

            switch (rotation) {
                case CLOCKWISE_90 -> { // Facing NORTH
                    dx = -rz;
                    dz = rx;
                }
                case CLOCKWISE_180 -> { // Facing EAST
                    dx = -rx;
                    dz = -rz;
                }
                case COUNTERCLOCKWISE_90 -> { // Facing SOUTH
                    dx = rz;
                    dz = -rx;
                }
                default -> { // NONE (Facing WEST - base schematic)
                    dx = rx;
                    dz = rz;
                }
            }

            return new Location(origin.getWorld(), origin.getBlockX() + dx, origin.getBlockY() + ry, origin.getBlockZ() + dz);
        }
    }

    public static Schematic loadFromStream(InputStream input, int anchorX, int anchorY, int anchorZ) throws IOException {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(input)))) {
            byte rootType = dis.readByte();
            if (rootType != 10) { // TAG_Compound
                throw new IOException("Invalid schematic NBT root tag: " + rootType);
            }
            readString(dis); // root name
            Map<String, Object> root = readCompound(dis);

            @SuppressWarnings("unchecked")
            Map<String, Object> schematic = (root.containsKey("Schematic")) ?
                    (Map<String, Object>) root.get("Schematic") : root;

            int width = ((Number) schematic.getOrDefault("Width", (short) 14)).intValue();
            int height = ((Number) schematic.getOrDefault("Height", (short) 7)).intValue();
            int length = ((Number) schematic.getOrDefault("Length", (short) 13)).intValue();

            @SuppressWarnings("unchecked")
            Map<String, Object> blocksCompound = (schematic.containsKey("Blocks")) ?
                    (Map<String, Object>) schematic.get("Blocks") : schematic;

            @SuppressWarnings("unchecked")
            Map<String, Object> paletteMap = (Map<String, Object>) blocksCompound.get("Palette");
            if (paletteMap == null && schematic.containsKey("Palette")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> p = (Map<String, Object>) schematic.get("Palette");
                paletteMap = p;
            }

            if (paletteMap == null) {
                throw new IOException("Schematic palette not found!");
            }

            Map<Integer, String> revPalette = new HashMap<>();
            for (Map.Entry<String, Object> entry : paletteMap.entrySet()) {
                int id = ((Number) entry.getValue()).intValue();
                revPalette.put(id, entry.getKey());
            }

            byte[] blockDataBytes = (byte[]) blocksCompound.getOrDefault("Data", blocksCompound.get("BlockData"));
            if (blockDataBytes == null && schematic.containsKey("BlockData")) {
                blockDataBytes = (byte[]) schematic.get("BlockData");
            }
            if (blockDataBytes == null) {
                throw new IOException("Schematic block data bytes not found!");
            }

            int[] indices = readVarInts(blockDataBytes, width * height * length);
            List<SchematicBlock> blockList = new ArrayList<>();

            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int idx = (y * length + z) * width + x;
                        if (idx < indices.length) {
                            int pid = indices[idx];
                            String state = revPalette.getOrDefault(pid, "minecraft:air");
                            if (!state.equals("minecraft:air")) {
                                blockList.add(new SchematicBlock(x, y, z, state));
                            }
                        }
                    }
                }
            }

            return new Schematic(width, height, length, anchorX, anchorY, anchorZ, blockList);
        }
    }

    private static int[] readVarInts(byte[] bytes, int expectedCount) {
        int[] result = new int[expectedCount];
        int count = 0;
        int i = 0;
        int len = bytes.length;

        while (i < len && count < expectedCount) {
            int val = 0;
            int shift = 0;
            while (true) {
                if (i >= len) break;
                byte b = bytes[i++];
                val |= (b & 0x7F) << shift;
                shift += 7;
                if ((b & 0x80) == 0) break;
            }
            result[count++] = val;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readCompound(DataInputStream dis) throws IOException {
        Map<String, Object> map = new HashMap<>();
        while (true) {
            byte type = dis.readByte();
            if (type == 0) break; // TAG_End
            String name = readString(dis);
            map.put(name, readTag(dis, type));
        }
        return map;
    }

    private static Object readTag(DataInputStream dis, byte type) throws IOException {
        return switch (type) {
            case 1 -> dis.readByte();
            case 2 -> dis.readShort();
            case 3 -> dis.readInt();
            case 4 -> dis.readLong();
            case 5 -> dis.readFloat();
            case 6 -> dis.readDouble();
            case 7 -> { // byte array
                int len = dis.readInt();
                byte[] arr = new byte[len];
                dis.readFully(arr);
                yield arr;
            }
            case 8 -> readString(dis);
            case 9 -> { // list
                byte elemType = dis.readByte();
                int len = dis.readInt();
                List<Object> list = new ArrayList<>(len);
                for (int i = 0; i < len; i++) {
                    list.add(readTag(dis, elemType));
                }
                yield list;
            }
            case 10 -> readCompound(dis);
            case 11 -> { // int array
                int len = dis.readInt();
                int[] arr = new int[len];
                for (int i = 0; i < len; i++) arr[i] = dis.readInt();
                yield arr;
            }
            case 12 -> { // long array
                int len = dis.readInt();
                long[] arr = new long[len];
                for (int i = 0; i < len; i++) arr[i] = dis.readLong();
                yield arr;
            }
            default -> throw new IOException("Unsupported NBT tag type: " + type);
        };
    }

    private static String readString(DataInputStream dis) throws IOException {
        short len = dis.readShort();
        byte[] bytes = new byte[len];
        dis.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
