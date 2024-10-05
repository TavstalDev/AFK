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
    public static void OnPlayerConnected(Player player) {
        try
        {
            CommonClass.LOG.debug("PLAYER_CONNECT was called by {}", EntityUtils.GetName(player));
            CommonClass.PutPlayerData(player.getStringUUID(), new PlayerData(EntityUtils.GetPosition(player), EntityUtils.GetBlockPosition(player), player.yHeadRot, LocalDateTime.now(), null));
            PlayerUtils.UpdateTablistName(player);
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnPlayerConnected':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }

    public static void OnPlayerDisconnected(Player player) {
        try {
            CommonClass.LOG.debug("PLAYER_DISCONNECT was called by {}", EntityUtils.GetName(player));
            var uuid = player.getStringUUID();
            CommonClass.ChangeAFKMode(player, false);
            CommonClass.RemovePlayerData(uuid);

            var server = player.getServer();
            if (server == null) {
                CommonClass.LOG.error("OnPlayerDisconnected -> Failed to get the server.");
                return;
            }

            var worldKey = WorldUtils.GetName(EntityUtils.GetLevel(player));
            if (player.isSleeping()) {
                ModUtils.BroadcastMessageByWorld(player, CommonClass.CONFIG().SleepStopMessage, worldKey,
                        EntityUtils.GetName(player), MathUtils.Clamp(CommonClass.GetRequiredPlayersToReset(server, worldKey), 0, server.getMaxPlayers()));
            }

            int requiredPlayersToReset = CommonClass.GetRequiredPlayersToReset(server, worldKey);
            if (requiredPlayersToReset <= 0) {
                ModUtils.BroadcastMessageByWorld(player, CommonClass.CONFIG().SleepResetMessage, worldKey);
                CommonClass.WakeUp(EntityUtils.GetServerLevel(player), server);
            }
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnPlayerDisconnected':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }

    public static void OnPlayerRespawned(Player player) {
        try {
            CommonClass.LOG.debug("AFTER_RESPAWN was called by {}", EntityUtils.GetName(player));
            CommonClass.ChangeAFKMode(player, false);
            PlayerUtils.UpdateTablistName(player);
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnPlayerRespawned':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }

    public static void OnServerTick(MinecraftServer server) {
        try {
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
                if (isChangedBlockPosition) {
                    // If the player is teleported then skips x ticks
                    if (isTeleported) {
                        data.TeleportTTL = 5;
                    }

                    // If the player is pushed by smth then skips x ticks
                    if (player.hasImpulse) {
                        data.ImpulseTTL = 10;
                    }

                    // CHECK IF PLAYER MOVED WILLINGLY
                    if ((!isMovedUnwillingly || shouldDisableAFK) && data.TeleportTTL == 0 && data.ImpulseTTL == 0) {
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
                } else {
                    // AUTO AFK Check
                    if (!(PlayerUtils.IsAFK(uuid) || PlayerUtils.IsInCombat(player))) {
                        if (Duration.between(data.Date, LocalDateTime.now()).toSeconds() > CommonClass.CONFIG().AutoAFKInterval && CommonClass.CONFIG().AutoAFKInterval > 0) {
                            CommonClass.ChangeAFKMode(player, true);
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnServerTick':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }

    public static void OnCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher) {
        try
        {
            AFKCommand.register(dispatcher);
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnCommandRegister':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }

    public static void OnChatted(Player player) {
        try {
            CommonClass.LOG.debug("CHAT_MESSAGE was called by {}", EntityUtils.GetName(player));
            CommonClass.ChangeAFKMode(player, false);
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnChatted':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }

    public static InteractionResult OnAttackBlock(Player player) {
        try {
            CommonClass.LOG.debug("ATTACK_BLOCK was called by {}", EntityUtils.GetName(player));
            CommonClass.ChangeAFKMode(player, false);
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnAttackBlock':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult OnAttackEntity(Player player, Entity entity) {
        try {
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
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnAttackEntity':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }

        return InteractionResult.PASS;
    }

    public static boolean OnDamageEntity(Entity entity, DamageSource source) {
        try {
            if (entity instanceof Player player) {
                String uuid = player.getStringUUID();
                PlayerData playerData = CommonClass.GetPlayerData(uuid);
                if (playerData != null) {

                    playerData.LastCombatTime = LocalDateTime.now();
                    CommonClass.PutPlayerData(uuid, playerData);
                }
            }

            if (source == null)
                return true;

            if (source.getEntity() != null && source.getEntity() instanceof Player targetPlayer) {
                String targetUUID = targetPlayer.getStringUUID();
                PlayerData targetData = CommonClass.GetPlayerData(targetUUID);
                if (targetData != null) {
                    targetData.LastCombatTime = LocalDateTime.now();
                    CommonClass.PutPlayerData(targetUUID, targetData);
                }
            }
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnDamageEntity':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
        return true;
    }

    public static InteractionResult OnUseBlock(Player player) {
        try {
            CommonClass.LOG.debug("USE_BLOCK was called by {}", EntityUtils.GetName(player));
            CommonClass.ChangeAFKMode(player, false);
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnUseBlock':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult OnUseEntity(Player player) {
        try {
            CommonClass.LOG.debug("USE_ENTITY was called by {}", EntityUtils.GetName(player));
            CommonClass.ChangeAFKMode(player, false);
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnUseEntity':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
        return InteractionResult.PASS;
    }

    public static InteractionResultHolder<ItemStack> OnUseItem(Player player) {
        try {
            CommonClass.LOG.debug("USE_ITEM was called by {}", EntityUtils.GetName(player));
            CommonClass.ChangeAFKMode(player, false);
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnUseItem':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
        return InteractionResultHolder.pass(ItemStack.EMPTY);
    }

    public static void OnPlayerChangesWorld(Player player, ServerLevel oldLevel) {
        try {
            CommonClass.LOG.debug("WORLD_CHANGE_1 was called by {}", EntityUtils.GetName(player));
            CommonClass.ChangeAFKMode(player, false);
            PlayerUtils.UpdateTablistName(player);
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnPlayerChangesWorld(Level)':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }

    public static void OnPlayerChangesWorld(Player player, String oldWorldKey) {
        try {
            CommonClass.LOG.debug("WORLD_CHANGE_2 was called by {}", EntityUtils.GetName(player));
            CommonClass.ChangeAFKMode(player, false);
            PlayerUtils.UpdateTablistName(player);
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnPlayerChangesWorld(Key)':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }

    public static void OnEntitySleepStarts(Entity entity) {
        try {
            if (!EntityUtils.IsPlayer(entity))
                return;

            CommonClass.LOG.debug("START_SLEEPING was called by {}", EntityUtils.GetName(entity));
            var server = entity.getServer();
            if (server == null) {
                CommonClass.LOG.error("OnEntitySleepStarts -> Failed to get the server.");
                return;
            }

            var worldKey = WorldUtils.GetName(EntityUtils.GetLevel(entity));

            int requiredPlayersToReset = CommonClass.GetRequiredPlayersToReset(server, worldKey);
            ModUtils.BroadcastMessageByWorld(entity, CommonClass.CONFIG().SleepStartMessage, worldKey,
                    EntityUtils.GetName(entity), MathUtils.Clamp(requiredPlayersToReset, 0, server.getMaxPlayers()));

            if (requiredPlayersToReset <= 0) {
                ModUtils.BroadcastMessageByWorld(entity, CommonClass.CONFIG().SleepResetMessage, worldKey);
                CommonClass.WakeUp(EntityUtils.GetServerLevel(entity), server);
            }
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnEntitySleepStarts':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }

    public static void OnEntitySleepStopped(Entity entity) {
        try {
            if (!EntityUtils.IsPlayer(entity))
                return;

            CommonClass.LOG.debug("STOP_SLEEPING was called by {}", EntityUtils.GetName(entity));
            var server = entity.getServer();
            if (server == null) {
                CommonClass.LOG.error("OnEntitySleepStopped -> Failed to get the server.");
                return;
            }

            var worldKey = WorldUtils.GetName(EntityUtils.GetLevel(entity));
            if (!worldKey.equals(CommonClass.GetLastWorldSleepReset())) {
                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.schedule(() -> {
                    ModUtils.BroadcastMessageByWorld(entity, CommonClass.CONFIG().SleepStopMessage, worldKey, EntityUtils.GetName(entity),
                            MathUtils.Clamp(CommonClass.GetRequiredPlayersToReset(server, worldKey), 0, server.getMaxPlayers()));
                }, 10, TimeUnit.MILLISECONDS);
            }
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing event 'OnEntitySleepStoppe':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }
}
