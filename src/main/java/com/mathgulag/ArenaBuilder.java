package com.mathgulag;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import java.util.*;

/** Interior de un corazon: dos camaras (ventriculos) unidas por un tabique con arcos. Sin netherrack. */
public class ArenaBuilder {
    public static BlockPos center; public static ServerWorld world;
    static final double RX = 34, RY = 30, RZ = 30; static final int OFF = 22;
    static double f(double dx, double dy, double dz) { return (dx / RX) * (dx / RX) + (dy / RY) * (dy / RY) + (dz / RZ) * (dz / RZ); }
    static boolean in(double x, double y, double z, double k) { return f(x + OFF, y, z) <= k || f(x - OFF, y, z) <= k; }

    public static void build(ServerWorld w, BlockPos c) {
        center = c; world = w; Random r = new Random(c.asLong());
        BlockState air = Blocks.AIR.getDefaultState(), fl = ModBlocks.FLESH.getDefaultState(), vein = ModBlocks.FLESH_VEIN.getDefaultState(), bone = ModBlocks.BONE.getDefaultState(), wl = ModBlocks.FLESH_WALL.getDefaultState();
        int minX = -(int) (OFF + RX) - 4, maxX = (int) (OFF + RX) + 4, maxY = (int) RY + 4, zz = (int) RZ + 4;
        BlockPos.Mutable p = new BlockPos.Mutable();
        for (int x = minX; x <= maxX; x++) for (int z = -zz; z <= zz; z++) {
            boolean foot = in(x, 0, z, 1.18);
            if (foot) for (int y = -3; y <= 0; y++) set(w, p.set(c.getX() + x, c.getY() + y, c.getZ() + z), r.nextInt(100) < 9 ? vein : fl);
            for (int y = 1; y <= maxY; y++) {
                boolean inside = in(x, y, z, 1.0), outer = in(x, y, z, 1.18);
                BlockState s = inside ? air : outer ? (r.nextInt(100) < 5 ? vein : wl) : null;
                // tabique central con dos arcos
                if (inside && Math.abs(x) <= 1 && y <= 16 && !(Math.abs(z) <= 7 && y <= 9) && Math.abs(z) < 24) s = r.nextInt(100) < 6 ? vein : wl;
                if (s != null) set(w, p.set(c.getX() + x, c.getY() + y, c.getZ() + z), s);
            }
        }
        // losas de carne en el suelo (relieve) y pilares para esconderse
        for (int x = minX; x <= maxX; x++) for (int z = -zz; z <= zz; z++) if (in(x, 1, z, 0.9) && Math.abs(x) > 2 && ((Math.floorDiv(x, 4) + Math.floorDiv(z, 4)) & 1) == 0) set(w, p.set(c.getX() + x, c.getY() + 1, c.getZ() + z), ModBlocks.FLESH_SLAB.getDefaultState());   // losas tipo baldosa
        for (int i = 0; i < 22; i++) {
            int x = rx(r), z = r.nextInt(2 * (int) RZ - 20) - (int) RZ + 10; if (!in(x, 1, z, 0.7) || Math.abs(x) < 5) continue;
            int h = 5 + r.nextInt(8);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) for (int y = 1; y <= h; y++) if (Math.abs(dx) + Math.abs(dz) < 2 || y < h - 2) set(w, p.set(c.getX() + x + dx, c.getY() + y, c.getZ() + z + dz), y == h ? vein : wl);
        }
        // huesos: costillas pegadas a las paredes, estalactitas y columnas
        for (int side = -1; side <= 1; side += 2) for (int zk = -24; zk <= 24; zk += 8) {
            double k = 1 - (zk / RZ) * (zk / RZ); if (k <= 0.05) continue;
            double rx = RX * Math.sqrt(k) * 0.97, ry = RY * Math.sqrt(k) * 0.97;
            for (double t = 0.05; t < Math.PI - 0.05; t += 0.02) {
                int x = (int) (side * OFF + Math.cos(t) * rx), y = 1 + (int) (Math.sin(t) * ry);
                if (Math.abs(x) < 3) continue; set(w, p.set(c.getX() + x, c.getY() + y, c.getZ() + zk), bone); set(w, p.set(c.getX() + x, c.getY() + y, c.getZ() + zk + 1), bone);
            }
        }
        for (int i = 0; i < 110; i++) {
            int x = rx(r), z = r.nextInt(2 * (int) RZ - 8) - (int) RZ + 4, top = -1;
            for (int y = (int) RY + 2; y >= 1; y--) if (in(x, y, z, 0.97)) { top = y; break; }
            if (top < 8 || Math.abs(x) < 3) continue;
            int len = 2 + r.nextInt(9);
            for (int j = 0; j < len; j++) set(w, p.set(c.getX() + x, c.getY() + top - j, c.getZ() + z), j == len - 1 ? ModBlocks.BONE_SPIKE.getDefaultState().with(BoneSpikeBlock.HANGING, true) : bone);
        }
        for (int i = 0; i < 45; i++) {
            int x = rx(r), z = r.nextInt(2 * (int) RZ - 8) - (int) RZ + 4; if (!in(x, 1, z, 0.8) || Math.abs(x) < 3) continue;
            int h = 2 + r.nextInt(6);
            for (int j = 1; j <= h; j++) set(w, p.set(c.getX() + x, c.getY() + j, c.getZ() + z), j == h ? ModBlocks.BONE_SPIKE.getDefaultState() : bone);
        }
        // luces de carne venosa y corazones latientes incrustados en el techo
        for (int i = 0; i < 160; i++) {
            int x = rx(r), z = r.nextInt(2 * (int) RZ - 8) - (int) RZ + 4, top = -1;
            for (int y = (int) RY + 2; y >= 1; y--) if (in(x, y, z, 0.97)) { top = y; break; }
            if (top < 6) continue;
            set(w, p.set(c.getX() + x, c.getY() + top + 1, c.getZ() + z), i % 9 == 0 ? ModBlocks.HEART_BLOCK.getDefaultState() : vein);
        }
        // corazon gigante colgando del centro
        MathGulag.cmdAt(w, "summon minecraft:item_display " + c.getX() + " " + (c.getY() + 20) + " " + c.getZ() + " " + MathGulag.HEART_NBT);
    }
    static int rx(Random r) { return r.nextInt((int) (2 * (OFF + RX) - 10)) - (int) (OFF + RX) + 5; }
    static void set(ServerWorld w, BlockPos p, BlockState s) { if (w.getBlockState(p) != s) w.setBlockState(p, s, Block.NOTIFY_LISTENERS); }

    public static BlockPos randomSpawn(Random r) {
        for (int i = 0; i < 200; i++) {
            int x = rx(r), z = r.nextInt(50) - 25;
            if (in(x, 3, z, 0.6) && Math.abs(x) > 4) return center.add(x, 1, z);
        }
        return center.add(-OFF, 1, 0);
    }
}
