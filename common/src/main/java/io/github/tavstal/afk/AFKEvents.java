package io.github.tavstal.afk;

import com.mojang.brigadier.CommandDispatcher;
import io.github.tavstal.afk.commands.AFKCommand;
import io.github.tavstal.afk.models.PlayerData;
import io.github.tavstal.afk.utils.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AFKEvents {
    public static InteractionResult OnPlayerConnected(Player player) {
        CommonClass.LOG.debug("PLAYER_CONNECT was called by {}", EntityUtils.GetName(player));
        CommonClass.PutPlayerData(player.getStringUUID(), new PlayerData(EntityUtils.GetPosition(player), EntityUtils.GetBlockPosition(player), player.yHeadRot, LocalDateTime.now()));
        PlayerUtils.UpdateTablistName(player);
        return InteractionResult.PASS;
    }

    public static InteractionResult OnPlayerDisconnected(Player player) {
        CommonClass.LOG.debug("PLAYER_DISCONNECT was called by {}", EntityUtils.GetName(player));
        var uuid = player.getStringUUID();
        CommonClass.ChangeAFKMode(player, false);
        CommonClass.RemovePlayerData(uuid);

        var server = player.getServer();
        if (server == null)
        {
            CommonClass.LOG.error("OnPlayerDisconnected -> Failed to get the server.");
            return InteractionResult.PASS;
        }

        var worldKey = WorldUtils.GetName(EntityUtils.GetLevel(player));
        if (player.isSleeping()) {
            ModUtils.BroadcastMessageByWorld(player, CommonClass.CONFIG().SleepStopMessage, worldKey,
                    EntityUtils.GetName(player), MathUtils.Clamp(CommonClass.GetRequiredPlayersToReset(server, worldKey), 0, server.getMaxPlayers()));
        }

        int requiredPlayersToReset = CommonClass.GetRequiredPlayersToReset(server, worldKey);
        if (requiredPlayersToReset <= 0)
        {
            ModUtils.BroadcastMessageByWorld(player, CommonClass.CONFIG().SleepResetMessage, worldKey);
            CommonClass.WakeUp(EntityUtils.GetServerLevel(player), server);
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult OnPlayerRespawned(Player player) {
        CommonClass.LOG.debug("AFTER_RESPAWN was called by {}", EntityUtils.GetName(player));
        CommonClass.ChangeAFKMode(player, false);
        PlayerUtils.UpdateTablistName(player);
        return InteractionResult.PASS;
    }

    public static InteractionResult OnServerTick(MinecraftServer server) {
        for (var player : server.getPlayerList().getPlayers()) {
            var uuid = player.getStringUUID();
            PlayerData data = CommonClass.GetPlayerData(uuid);

            // NOTES
            // HorizontalCollisions do not activate
            // isPushedByFluid is always true

            boolean isTeleported = MathUtils.Distance(player.blockPosition(), data.LastBlockPosition) > 3;
            boolean isChangedBlockPosition = data.LastBlockPosition.getX() != player.blockPosition().getX() || data.LastBlockPosition.getY() != player.blockPosition().getY() || data.LastBlockPosition.getZ() != player.blockPosition().getZ();

            boolean isInMovingVehicle = player.isPassenger();
            var playerVehicle = player.getVehicle();
            if (playerVehicle != null) {
                isInMovingVehicle = playerVehicle.getControllingPassenger() != player || playerVehicle.hasImpulse;
            }

            boolean isMovedUnwillingly = player.isInPowderSnow || player.isChangingDimension() || player.isInWater()
                    || player.isInLava() || isInMovingVehicle || player.isFallFlying() || player.isHurt()
                    || PlayerUtils.IsInCombat(player) || isTeleported;
            // Should disable AFK no matter what because player had input
            boolean shouldDisableAFK = player.isSprinting() || player.isShiftKeyDown() || player.isUsingItem() || player.yHeadRot != data.HeadRotation;

            // CHECK IF CHANGED POSITION
            if (isChangedBlockPosition)
            {
                // If the player is teleported then skips x ticks
                if (isTeleported)
                {
                    data.TeleportTTL = 5;
                }

                // If the player is pushed by smth then skips x ticks
                if (player.hasImpulse)
                {
                    data.ImpulseTTL = 10;
                }

                // CHECK IF PLAYER MOVED WILLINGLY
                if ((!isMovedUnwillingly || shouldDisableAFK) && data.TeleportTTL == 0 && data.ImpulseTTL == 0)
                {
                    if (CommonClass.CONFIG().DisableOnMove)
                        CommonClass.ChangeAFKMode(player, false);

                    data.Date = LocalDateTime.now();
                }

                if (data.TeleportTTL > 0)
                    data.TeleportTTL -= 1;

                if (data.ImpulseTTL > 0)
                    data.ImpulseTTL -= 1;

                data.LastPosition = EntityUtils.GetPosition(player);
                data.HeadRotation = player.yHeadRot;
                data.LastBlockPosition = EntityUtils.GetBlockPosition(player);
                CommonClass.PutPlayerData(uuid, data);
            }
            else
            {
                // AUTO AFK Check
                if (!(PlayerUtils.IsAFK(uuid) || PlayerUtils.IsInCombat(player)))
                {
                    if (Duration.between(data.Date, LocalDateTime.now()).toSeconds() > CommonClass.CONFIG().AutoAFKInterval && CommonClass.CONFIG().AutoAFKInterval > 0) {
                        CommonClass.ChangeAFKMode(player, true);
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult OnCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher) {
        AFKCommand.register(dispatcher);
        return InteractionResult.PASS;
    }

    public static InteractionResult OnChatted(Player player) {
        CommonClass.LOG.debug("CHAT_MESSAGE was called by {}", EntityUtils.GetName(player));
        CommonClass.ChangeAFKMode(player, false);
        return InteractionResult.PASS;
    }

    public static InteractionResult OnAttackBlock(Player player) {
        CommonClass.LOG.debug("ATTACK_BLOCK was called by {}", EntityUtils.GetName(player));
        CommonClass.ChangeAFKMode(player, false);
        return InteractionResult.PASS;
    }

    public static InteractionResult OnAttackEntity(Player player, Entity entity) {
        CommonClass.LOG.debug("ATTACK_ENTITY was called by {}", EntityUtils.GetName(player));
        if (CommonClass.CONFIG().DisableOnAttackEntity)
            CommonClass.ChangeAFKMode(player, false);

        String uuid = player.getStringUUID();
        PlayerData playerData = CommonClass.GetPlayerData(uuid);
        if (playerData != null) {

            playerData.LastCombatTime = LocalDateTime.now();
            CommonClass.PutPlayerData(uuid, playerData);
        }

        if (entity instanceof Player targetPlayer) {
            String targetUUID = targetPlayer.getStringUUID();
            PlayerData targetData = CommonClass.GetPlayerData(targetUUID);
            if (targetData != null) {
                targetData.LastCombatTime = LocalDateTime.now();
                CommonClass.PutPlayerData(targetUUID, targetData);
            }
        }


        return InteractionResult.PASS;
    }

    public static boolean OnDamageEntity(Entity entity, DamageSource source) {

        if (entity instanceof Player player) {
            String uuid = player.getStringUUID();
            PlayerData playerData = CommonClass.GetPlayerData(uuid);
            if (playerData != null) {

                playerData.LastCombatTime = LocalDateTime.now();
                CommonClass.PutPlayerData(uuid, playerData);
            }
        }

        if (source.getEntity() != null && source.getEntity() instanceof Player targetPlayer) {
            String targetUUID = targetPlayer.getStringUUID();
            PlayerData targetData = CommonClass.GetPlayerData(targetUUID);
            if (targetData != null) {
                targetData.LastCombatTime = LocalDateTime.now();
                CommonClass.PutPlayerData(targetUUID, targetData);
            }
        }

        return true;
    }

    public static InteractionResult OnUseBlock(Player player) {
        CommonClass.LOG.debug("USE_BLOCK was called by {}", EntityUtils.GetName(player));
        CommonClass.ChangeAFKMode(player, false);
        return InteractionResult.PASS;
    }

    public static InteractionResult OnUseEntity(Player player) {
        CommonClass.LOG.debug("USE_ENTITY was called by {}", EntityUtils.GetName(player));
        CommonClass.ChangeAFKMode(player, false);
        return InteractionResult.PASS;
    }

    public static InteractionResultHolder<ItemStack> OnUseItem(Player player) {
        CommonClass.LOG.debug("USE_ITEM was called by {}", EntityUtils.GetName(player));
        CommonClass.ChangeAFKMode(player, false);
        return InteractionResultHolder.pass(ItemStack.EMPTY);
    }

    public static InteractionResult OnPlayerChangesWorld(Player player, ServerLevel oldLevel) {
        CommonClass.LOG.debug("WORLD_CHANGE_1 was called by {}", EntityUtils.GetName(player));
        CommonClass.ChangeAFKMode(player, false);
        PlayerUtils.UpdateTablistName(player);
        return InteractionResult.PASS;
    }

    public static InteractionResult OnPlayerChangesWorld(Player player, String oldWorldKey) {
        CommonClass.LOG.debug("WORLD_CHANGE_2 was called by {}", EntityUtils.GetName(player));
        CommonClass.ChangeAFKMode(player, false);
        PlayerUtils.UpdateTablistName(player);
        return InteractionResult.PASS;
    }

    public static InteractionResult OnEntitySleepStarts(Entity entity) {
        if (!EntityUtils.IsPlayer(entity))
            return InteractionResult.PASS;

        CommonClass.LOG.debug("START_SLEEPING was called by {}", EntityUtils.GetName(entity));
        var server = entity.getServer();
        if (server == null)
        {
            CommonClass.LOG.error("OnEntitySleepStarts -> Failed to get the server.");
            return InteractionResult.PASS;
        }

        var worldKey = WorldUtils.GetName(EntityUtils.GetLevel(entity));

        int requiredPlayersToReset = CommonClass.GetRequiredPlayersToReset(server, worldKey);
        ModUtils.BroadcastMessageByWorld(entity, CommonClass.CONFIG().SleepStartMessage, worldKey,
                EntityUtils.GetName(entity), MathUtils.Clamp(requiredPlayersToReset, 0, server.getMaxPlayers()));

        if (requiredPlayersToReset <= 0)
        {
            ModUtils.BroadcastMessageByWorld(entity, CommonClass.CONFIG().SleepResetMessage, worldKey);
            CommonClass.WakeUp(EntityUtils.GetServerLevel(entity), server);
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult OnEntitySleepStopped(Entity entity) {
        if (!EntityUtils.IsPlayer(entity))
            return InteractionResult.PASS;

        CommonClass.LOG.debug("STOP_SLEEPING was called by {}", EntityUtils.GetName(entity));
        var server = entity.getServer();
        if (server == null)
        {
            CommonClass.LOG.error("OnEntitySleepStopped -> Failed to get the server.");
            return InteractionResult.PASS;
        }

        var worldKey = WorldUtils.GetName(EntityUtils.GetLevel(entity));
        if (!worldKey.equals(CommonClass.GetLastWorldSleepReset()))
        {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(() -> {
                ModUtils.BroadcastMessageByWorld(entity, CommonClass.CONFIG().SleepStopMessage, worldKey, EntityUtils.GetName(entity),
                        MathUtils.Clamp(CommonClass.GetRequiredPlayersToReset(server, worldKey), 0, server.getMaxPlayers()));
            }, 10, TimeUnit.MILLISECONDS);
        }
        return InteractionResult.PASS;
    }
}

