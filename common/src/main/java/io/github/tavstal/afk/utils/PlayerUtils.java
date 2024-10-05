package io.github.tavstal.afk.utils;

import io.github.tavstal.afk.CommonClass;
import io.github.tavstal.afk.models.PlayerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.time.Duration;
import java.time.LocalDateTime;

public class PlayerUtils {
    public static boolean IsAFK(String uuid) {
        PlayerData data = CommonClass.GetPlayerData(uuid);
        if (data == null)
            return false;

        return data.IsAFK;
    }

    public static boolean IsInCombat(Player player) {
        PlayerData data = CommonClass.GetPlayerData(player.getStringUUID());
        if (data == null)
            return false;

        return Duration.between(LocalDateTime.now(), data.LastCombatTime).toSeconds() < CommonClass.CONFIG().CombatTimeout;
    }

    public static boolean IsSleeping(Player player) {
        PlayerData data = CommonClass.GetPlayerData(player.getStringUUID());
        if (data == null)
            return false;

        return data.IsAFK || player.isSleeping();
    }

    public static ServerPlayer GetServerPlayer(Player player) {
        if (player instanceof ServerPlayer) {
            return (ServerPlayer) player; // Safe cast
        }
        return null; // If not a server player, return null or handle accordingly
    }

    public static ServerPlayer GetServerPlayer(MinecraftServer server, Player player) {
        return server.getPlayerList().getPlayer(player.getUUID());
    }

    public static PlayerTeam GetPlayerTeam(Player player) {
        // Get the scoreboard for the server
        Scoreboard scoreboard = player.getScoreboard();

        // Get the player's current team (returns null if no team is assigned)
        return scoreboard.getPlayersTeam(player.getName().getString());
    }

    public  static  void UpdateTablistName(Player player) {
        if (player == null)
        {
            CommonClass.LOG.error("The provided player was null.");
            return;
        }
        var server = player.getServer();
        if (server == null)
        {
            CommonClass.LOG.error("The provided player's server was null.");
            return;
        }

        ServerScoreboard scoreboard = server.getScoreboard();

        var data = CommonClass.GetPlayerData(player.getStringUUID());
        String playerName = (player.hasCustomName() ? player.getCustomName() : player.getName()).getString();

        PlayerTeam currentTeam = GetPlayerTeam(player);
        if (currentTeam != null)
            scoreboard.removePlayerFromTeam(playerName, currentTeam);

        if (data.IsAFK) {
            scoreboard.addPlayerToTeam(playerName, scoreboard.getPlayerTeam("afk"));
        }
        else  {
            if (CommonClass.CONFIG().ShowWorldTablist) {
                String worldName = WorldUtils.GetName(EntityUtils.GetLevel(player));
                scoreboard.addPlayerToTeam(playerName, scoreboard.getPlayerTeam("world_" + worldName));
            }
        }
    }
}

