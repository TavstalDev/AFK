package io.github.tavstal.afk;

import java.util.concurrent.TimeUnit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        AFKEvents.OnPlayerConnected(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        AFKEvents.OnPlayerDisconnected(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerChatted(ServerChatEvent event) {
        if (CommonClass.CONFIG().DisableOnChatting)
            AFKEvents.OnChatted(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerSleepStarted(PlayerSleepInBedEvent event) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            if (!event.isCanceled() && event.getEntity().isSleeping()) {
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
    public void onPlayerChangeWorld(PlayerChangedDimensionEvent event) {
        if (CommonClass.CONFIG().DisableOnWorldChange)
            AFKEvents.OnPlayerChangesWorld(event.getEntity(), event.getFrom().toString());
    }

    @SubscribeEvent
    public void onPlayerRespawned(PlayerRespawnEvent event) {
        if (CommonClass.CONFIG().DisableOnRespawn)
            AFKEvents.OnPlayerRespawned(event.getEntity());
    }

    // Left Click Block
    @SubscribeEvent
    public void onPlayerAttackedBlock(LeftClickBlock event) {
        if (CommonClass.CONFIG().DisableOnAttackBlock)
            AFKEvents.OnAttackBlock(event.getEntity());
    }

    // Right Click Block
    @SubscribeEvent
    public void onPlayerUsedBlock(RightClickBlock event) {
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
    public void onPlayerUsedEntity(EntityInteract event) {
        if (CommonClass.CONFIG().DisableOnUseEntity)
            AFKEvents.OnUseEntity(event.getEntity());
    }

    // Left Click Empty
    @SubscribeEvent
    public void onPlayerUsedItem(LeftClickEmpty event) {
        if (CommonClass.CONFIG().DisableOnUseItem)
            AFKEvents.OnUseItem(event.getEntity());
    }

    // Right Click Item
    @SubscribeEvent
    public void onPlayerUsedItem(RightClickItem event) {
        if (CommonClass.CONFIG().DisableOnUseItem)
            AFKEvents.OnUseItem(event.getEntity());
    }
}
