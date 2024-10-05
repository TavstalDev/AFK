package io.github.tavstal.afk;

import io.github.tavstal.afk.platform.FabricPlatformHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FabricMain implements ModInitializer {

    private boolean _isInitialized = false;

    @Override
    public void onInitialize() {

        // CHECK IF THE MOD IS LOADED ON CLIENT
        var helper = new FabricPlatformHelper();
        if (helper.isClientSide()) {
            CommonClass.LOG.error("{} should be only loaded on the server.", CommonClass.MOD_NAME);
            return;
        }

        // SERVER START TICK EVENT
        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            if (_isInitialized)
            {
                return;
            }

            _isInitialized = true;
            CommonClass.init(server);
            AFKEvents.OnCommandRegister(server.getCommands().getDispatcher());
        });

        // Player Connected Event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, client) -> AFKEvents.OnPlayerConnected(handler.player));

        // Player Disconnected Event
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> AFKEvents.OnPlayerDisconnected(handler.player));

        // Server Tick Event
        ServerTickEvents.END_SERVER_TICK.register((server) -> AFKEvents.OnServerTick(server));

        // Sleeping Started Event
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> AFKEvents.OnEntitySleepStarts(entity));

        // Sleeping Stopped Event
        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> AFKEvents.OnEntitySleepStopped(entity));

        // Attack Block Event
        if (CommonClass.CONFIG().DisableOnAttackBlock)
        {
            AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> AFKEvents.OnAttackBlock(player));
        }

        // Attack Entity Event
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> AFKEvents.OnAttackEntity(player, entity));

        // Allow Damage Event
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(((entity, source, amount) -> AFKEvents.OnDamageEntity(entity, source)));

        // Use Block Event
        if (CommonClass.CONFIG().DisableOnUseBlock)
        {
            UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> AFKEvents.OnUseBlock(player));
        }

        // Use Entity Event
        if (CommonClass.CONFIG().DisableOnUseEntity)
        {
            UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> AFKEvents.OnUseEntity(player));
        }

        // Use Item Event
        if (CommonClass.CONFIG().DisableOnUseItem)
        {
            UseItemCallback.EVENT.register((player, world, hand) -> AFKEvents.OnUseItem(player));
        }

        // Player World Change Event
        if (CommonClass.CONFIG().DisableOnWorldChange)
        {
            ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                    AFKEvents.OnPlayerChangesWorld(player, origin));
        }

        // Player Respawned Event
        if (CommonClass.CONFIG().DisableOnRespawn)
        {
            ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> AFKEvents.OnPlayerRespawned(newPlayer));
        }

        // Player Chatted Event
        if (CommonClass.CONFIG().DisableOnChatting)
        {
            ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> AFKEvents.OnChatted(sender));
        }
    }
}
