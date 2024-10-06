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
        try {
            PlayerData data = CommonClass.GetPlayerData(uuid);
            if (data == null)
                return false;

            return data.IsAFK;
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Failed to determine 'is the player afk':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
            return  false;
        }
    }

    public static boolean IsInCombat(Player player) {
        try {
            PlayerData data = CommonClass.GetPlayerData(player.getStringUUID());
            if (data == null)
                return false;

            if (data.LastCombatTime == null) {
                return false;
            }

            long duration = Duration.between(data.LastCombatTime, LocalDateTime.now()).toSeconds();
            return duration  < CommonClass.CONFIG().CombatTimeout;
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Failed to determine 'is the player in combat':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
            return  false;
        }
    }

    public  static boolean IsFake(Player player) {
        try
        {
            // This might look silly, but here is an explanation using Create as example.
            // This is how a normal player's DisplayName looks:
            // # START
            // literal{}[style={clickEvent=ClickEvent{action=SUGGEST_COMMAND, value='/tell Tavstal '},
            // hoverEvent=HoverEvent{action=<action show_entity>,
            // value='net.minecraft.network.chat.HoverEvent$EntityTooltipInfo@e6e072c2'},insertion=Tavstal},
            // siblings=[empty[style={color=white}, siblings=[empty[style={}], literal{Tavstal},
            // literal{ [overworld] }[style={}]]]]]
            // # END
            // This is how Create's Deployer's DisplayName looks:
            // # translation{key='create.block.deployer.damage_source_name', args=[]}
            // Because player.getName() (literal{Tavstal}) is the same on both and only the real player's DisplayName contains it.
            // So we can use it to determine the Player object is real or fake.
            return !player.getDisplayName().toString().contains(player.getName().toString());
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Failed to determine 'is the player fake':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
            return  false;
        }
    }

    public static boolean IsSleeping(Player player) {
        try {
            PlayerData data = CommonClass.GetPlayerData(player.getStringUUID());
            if (data == null)
                return false;

            return data.IsAFK || player.isSleeping();
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Failed to determine 'is the player sleeping':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
            return  false;
        }
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
        try {
            if (player == null) {
                CommonClass.LOG.error("The provided player was null.");
                return;
            }
            var server = player.getServer();
            if (server == null) {
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
            } else {
                if (CommonClass.CONFIG().ShowWorldTablist) {
                    String worldName = WorldUtils.GetName(EntityUtils.GetLevel(player));
                    scoreboard.addPlayerToTeam(playerName, scoreboard.getPlayerTeam("world_" + worldName));
                }
            }
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing method 'UpdateTablistName':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
        }
    }
}

