package io.github.tavstal.afk;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.concurrent.TimeUnit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class EventListener {
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {

        CommonClass.init(event.getServer());
        AFKEvents.OnCommandRegister(event.getServer().getCommands().getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        AFKEvents.OnServerTick(event.getServer());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        //AFKEvents.OnCommandRegister(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        AFKEvents.OnPlayerConnected(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        AFKEvents.OnPlayerDisconnected(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerChatted(ServerChatEvent event) {
        if (CommonClass.CONFIG().DisableOnChatting)
            AFKEvents.OnChatted(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerSleepStarted(CanPlayerSleepEvent event) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            if (!event.getEntity().isSleeping()) {
                AFKEvents.OnEntitySleepStarts(event.getEntity());
            }
        }, 5, TimeUnit.MILLISECONDS);
        executorService.shutdown();

    }

    @SubscribeEvent
    public void onPlayerSleepEnded(PlayerWakeUpEvent event) {
        AFKEvents.OnEntitySleepStopped(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerChangeWorld(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (CommonClass.CONFIG().DisableOnWorldChange)
            AFKEvents.OnPlayerChangesWorld(event.getEntity(), event.getFrom().toString());
    }

    @SubscribeEvent
    public void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        if (CommonClass.CONFIG().DisableOnRespawn)
            AFKEvents.OnPlayerRespawned(event.getEntity());
    }

    // Left Click Block
    @SubscribeEvent
    public void onPlayerAttackedBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (CommonClass.CONFIG().DisableOnAttackBlock)
            AFKEvents.OnAttackBlock(event.getEntity());
    }

    // Right Click Block
    @SubscribeEvent
    public void onPlayerUsedBlock(PlayerInteractEvent.RightClickBlock event) {
        if (CommonClass.CONFIG().DisableOnUseBlock)
            AFKEvents.OnUseBlock(event.getEntity());
    }

    // Left Click Entity
    @SubscribeEvent
    public void onPlayerAttackedEntity(AttackEntityEvent event) {
        if (CommonClass.CONFIG().DisableOnAttackEntity)
            AFKEvents.OnAttackEntity(event.getEntity(), event.getTarget());
    }

    // Damage
    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        AFKEvents.OnDamageEntity(event.getEntity(), event.getEntity().getLastDamageSource());
    }

    // Right Click Entity
    @SubscribeEvent
    public void onPlayerUsedEntity(PlayerInteractEvent.EntityInteract event) {
        if (CommonClass.CONFIG().DisableOnUseEntity)
            AFKEvents.OnUseEntity(event.getEntity());
    }

    // Left Click Empty
    @SubscribeEvent
    public void onPlayerUsedItem(PlayerInteractEvent.LeftClickEmpty event) {
        if (CommonClass.CONFIG().DisableOnUseItem)
            AFKEvents.OnUseItem(event.getEntity());
    }

    // Right Click Item
    @SubscribeEvent
    public void onPlayerUsedItem(PlayerInteractEvent.RightClickItem event) {
        if (CommonClass.CONFIG().DisableOnUseItem)
            AFKEvents.OnUseItem(event.getEntity());
    }
}
