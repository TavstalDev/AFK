package io.github.tavstal.afk.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class MathUtils {
    public static int Clamp(int value, int max) {
        return value > max ? max : value;
    }

    public static int Clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static double Distance(Vec3 vec, Vec3 other) {
        double x = (other.x - vec.x);
        double y = (other.y - vec.y);
        double z = (other.z - vec.z);
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static int Distance(BlockPos vec, BlockPos other) {
        int x = (other.getX() - vec.getX());
        int y = (other.getY() - vec.getY());
        int z = (other.getZ()- vec.getZ());
        return (int)Math.sqrt(x * x + y * y + z * z);
    }
}
