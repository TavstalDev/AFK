package io.github.tavstal.afk;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        CommonClass.init(((CraftServer)this.getServer()).getServer(), true);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ServerPlayer mcPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        if (mcPlayer == null)
            return;
        AFKEvents.OnPlayerConnected(mcPlayer);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ServerPlayer mcPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        if (mcPlayer == null)
            return;
        AFKEvents.OnPlayerDisconnected(mcPlayer);
    }

    @EventHandler
    public void onServerTick(ServerTickEndEvent event) {
        AFKEvents.OnServerTick(((CraftServer)this.getServer()).getServer());
    }

    @EventHandler
    public void onAttackBlock(BlockBreakEvent event) {
        ServerPlayer mcPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        if (mcPlayer == null)
            return;
        AFKEvents.OnAttackBlock(mcPlayer, mcPlayer.getUsedItemHand());
    }

    @EventHandler
    public void onAttackEntity(EntityDamageByEntityEvent event) {
        var victim = event.getEntity();
        var attacker = event.getDamager();
        if (victim instanceof Player player) {
            ServerPlayer mcPlayer = ((CraftPlayer)player.getPlayer()).getHandle();
            AFKEvents.OnAttackEntity(mcPlayer, ((CraftEntity)attacker).getHandle(), getHand(player.getHandRaised()));
        }
    }

    @EventHandler
    public  void  onChatted(PlayerCommandPreprocessEvent event) {
        ServerPlayer mcPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        if (mcPlayer == null)
            return;
        AFKEvents.OnChatted(mcPlayer);
    }

    @EventHandler
    public void onDamageEntity(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            ServerPlayer mcPlayer = ((CraftPlayer)player.getPlayer()).getHandle();
            DamageSource source = ((CraftDamageSource)event.getDamageSource()).getHandle();
            AFKEvents.OnDamageEntity(mcPlayer, source);
        }
    }

    @EventHandler
    public void onSleepStart(PlayerBedEnterEvent event) {
        ServerPlayer mcPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        if (mcPlayer == null)
            return;
        AFKEvents.OnEntitySleepStarts(mcPlayer);
    }

    @EventHandler
    public void onSleepStop(PlayerBedLeaveEvent event) {
        ServerPlayer mcPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        if (mcPlayer == null)
            return;
        AFKEvents.OnEntitySleepStopped(mcPlayer);
    }

    @EventHandler
    public void onPlayerChangesWorld(PlayerChangedWorldEvent event) {
        ServerPlayer mcPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        if (mcPlayer == null)
            return;
        AFKEvents.OnPlayerChangesWorld(mcPlayer, event.getFrom().getName());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        ServerPlayer mcPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        if (mcPlayer == null)
            return;
        AFKEvents.OnPlayerRespawned(mcPlayer);
    }

    @EventHandler
    public void onUseBlock(EntityPlaceEvent event) {
        var player = event.getPlayer();
        if (player == null)
            return;

        ServerPlayer mcPlayer = ((CraftPlayer)player).getHandle();
        if (mcPlayer == null)
            return;
        AFKEvents.OnUseBlock(mcPlayer, getHand(event.getHand()));
    }

    @EventHandler
    public void onUseEntity(PlayerInteractEntityEvent event) {
        ServerPlayer mcPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        if (mcPlayer == null)
            return;
        AFKEvents.OnUseEntity(mcPlayer, getHand(event.getHand()));
    }

    @EventHandler
    public void onUseItem(PlayerInteractEvent event) {
        ServerPlayer mcPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        if (mcPlayer == null)
            return;
        if (event.getItem() != null) {
            AFKEvents.OnUseItem(mcPlayer, getHand(event.getHand()));
        }
    }


    private InteractionHand getHand(EquipmentSlot slot) {
        if (slot == EquipmentSlot.HAND) {
            return InteractionHand.MAIN_HAND;
        } else if (slot == EquipmentSlot.OFF_HAND) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
}
