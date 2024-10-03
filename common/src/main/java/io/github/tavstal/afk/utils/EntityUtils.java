package io.github.tavstal.afk.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityUtils {
    public static String GetName(Entity entity) {
        return entity.getName().getString();
    }

    public static boolean IsPlayer(Entity entity)
    {
        return entity instanceof Player;
    }

    public static boolean IsLiving(Entity entity)
    {
        return entity instanceof LivingEntity;
    }

    public static Level GetLevel(Entity entity) {
        return entity.level();
    }

    public static ServerLevel GetServerLevel(Entity entity) {
        var server = entity.getServer();
        if (server == null)
            return null;

        return server.getLevel(GetLevel(entity).dimension());
    }

    public static Vec3 GetPosition(Entity entity) {
        return entity.position();
    }

    public static BlockPos GetBlockPosition(Entity entity) {
        return entity.blockPosition();
    }

    public static double GetWalkSpeed(Entity entity, Vec3 oldPos) {
        var deltaX = entity.getX() - oldPos.x;
        var deltaZ = entity.getZ() - oldPos.z;
        return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaZ, 2));
    }

    public static double DistanceToSqr(Entity entity, Vec3 vec3) {
        double d = entity.getX() - vec3.x;
        double e = entity.getY() - vec3.y;
        return d * d + e * e;
    }
}
