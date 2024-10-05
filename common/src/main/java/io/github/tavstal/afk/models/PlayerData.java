package io.github.tavstal.afk.models;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.time.LocalDateTime;

public class PlayerData {
    public boolean IsAFK;
    public Vec3 LastPosition;
    public BlockPos LastBlockPosition;
    public float HeadRotation;
    public int TeleportTTL;
    public int ImpulseTTL;
    public LocalDateTime Date;
    public LocalDateTime LastCombatTime;

    public PlayerData(Vec3 lastPosition, BlockPos lastBlockPosition, float headRotation, LocalDateTime date, LocalDateTime lastCombatTime) {
        LastPosition = lastPosition;
        LastBlockPosition = lastBlockPosition;
        HeadRotation = headRotation;
        Date = date;
        LastCombatTime = lastCombatTime;
    }
}
