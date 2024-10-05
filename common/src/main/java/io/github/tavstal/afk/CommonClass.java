package io.github.tavstal.afk;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Dictionary;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Hashtable;

import io.github.tavstal.afk.utils.*;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import io.github.tavstal.afk.models.PlayerData;

import net.minecraft.ChatFormatting;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonClass {
    public static final String MOD_ID = "afk";
    public static final String MOD_NAME = "TAFK";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    private static String _lastWorldSleepReset;

    public static String GetLastWorldSleepReset() {
        return _lastWorldSleepReset;
    }

    //#region Player Data
    private static Dictionary<String, PlayerData> _playerDataList = new Hashtable<>();

    public static Dictionary<String, PlayerData> GetPlayerDataList() {
        return _playerDataList;
    }

    public static void PutPlayerData(String uuid, PlayerData newData) {
        _playerDataList.put(uuid, newData);
    }

    public static PlayerData GetPlayerData(String uuid) {
        return _playerDataList.get(uuid);
    }

    public static void RemovePlayerData(String uuid) {
        _playerDataList.remove(uuid);
    }

    ////#endregion
    private static CommonConfig _config = null;

    public static CommonConfig CONFIG() {
        if (_config == null) {
            _config = ConfigUtils.LoadConfig();
            LOG.debug("Config null ? " + (_config == null));
        }
        return _config;
    }

    public static void init(MinecraftServer server) {
        if (CONFIG().EnableDebugMode) {
            SetLogLevel("DEBUG");
        }

        // Create scoreboard team
        var scoreboard = ModUtils.getServerScoreboard(server);
        if (scoreboard.getPlayerTeam("afk") == null) {
            PlayerTeam team = scoreboard.addPlayerTeam("afk");
            if (CONFIG().Prefix.isBlank())
                team.setPlayerPrefix(null);
            else
                team.setPlayerPrefix(ModUtils.Literal(MessageFormat.format(CONFIG().TablistFormat, CONFIG().Prefix)));

            if (CONFIG().Suffix.isBlank())
                team.setPlayerSuffix(null);
            else
                team.setPlayerSuffix(ModUtils.Literal(MessageFormat.format(CONFIG().TablistFormat,CONFIG().Suffix)));
            team.setColor(ChatFormatting.WHITE);
        }

        if (CONFIG().ShowWorldTablist) {
            for (var level : server.getAllLevels()) {
                String worldName = WorldUtils.GetName(level);
                String worldDisplayName = "";
                if (worldName.contains(":"))
                    worldDisplayName = worldName.split(":")[1];
                else worldDisplayName = worldName;

                if (scoreboard.getPlayerTeam("world_" + worldName) == null) {
                    PlayerTeam team = scoreboard.addPlayerTeam("world_" + worldName);
                    if (CONFIG().WorldPrefix.isBlank())
                        team.setPlayerPrefix(null);
                    else
                        team.setPlayerPrefix(ModUtils.Literal(MessageFormat.format(CONFIG().TablistFormat, MessageFormat.format(CONFIG().WorldPrefix, worldDisplayName))));

                    if (CONFIG().WorldSuffix.isBlank())
                        team.setPlayerSuffix(null);
                    else
                        team.setPlayerSuffix(ModUtils.Literal(MessageFormat.format(CONFIG().TablistFormat, MessageFormat.format(CONFIG().WorldSuffix, worldDisplayName))));
                    team.setColor(ChatFormatting.WHITE);
                }
            }
        }

        LOG.info(MOD_NAME + " has been loaded.");
    }

    private static void SetLogLevel(String level) {
        // Set the logging level for the logger
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        LoggerConfig loggerConfig = loggerContext.getConfiguration().getLoggerConfig(LOG.getName());
        loggerConfig.setLevel(Level.valueOf(level.toUpperCase()));
        loggerContext.updateLoggers();
    }

    public static int GetRequiredPlayersToReset(MinecraftServer server, String worldKey) {
        int playersSleeping = 0;
        int playersRequiredToResetTime = 0;

        for (var serverPlayer : server.getPlayerList().getPlayers()) {
            String playerWorld = WorldUtils.GetName(EntityUtils.GetLevel(serverPlayer));
            if (worldKey.equals(playerWorld)) {
                playersRequiredToResetTime++;
            }

            if ((serverPlayer.isSleeping() && worldKey.equals(playerWorld)) || PlayerUtils.IsAFK(serverPlayer.getStringUUID())) {
                playersSleeping++;
            }
        }

        double value = ((double) playersRequiredToResetTime / 100.0 * (double) 100) - (double) playersSleeping;
        return (int) value;
    }

    public static void ChangeAFKMode(Player player, boolean enable) {
        var uuid = player.getStringUUID();
        var playerName = EntityUtils.GetName(player);
        var data = GetPlayerData(uuid);
        if (enable) {
            if (!data.IsAFK) {
                ModUtils.BroadcastMessage(player, CONFIG().AFKOnMessage, playerName);
                data.IsAFK = true;
                var server = player.getServer();
                if (server != null) {
                    //var scoreboard = server.getScoreboard();
                    //scoreboard.addPlayerToTeam(playerName, scoreboard.getPlayerTeam("afk"));
                    PlayerUtils.UpdateTablistName(player);
                } else
                    LOG.error("ChangeAFKMode -> Failed to get the server.");
            }

            data.LastPosition = EntityUtils.GetPosition(player);
            data.LastBlockPosition = EntityUtils.GetBlockPosition(player);
            data.HeadRotation = player.yHeadRot;
            data.Date = LocalDateTime.now();
            PutPlayerData(uuid, data);
        } else {
            if (data.IsAFK) {
                ModUtils.BroadcastMessage(player, CONFIG().AFKOffMessage, playerName);
                data.IsAFK = false;
                var server = player.getServer();
                if (server != null) {
                    //var scoreboard = server.getScoreboard();
                    //scoreboard.removePlayerFromTeam(playerName, scoreboard.getPlayerTeam("afk"));
                    PlayerUtils.UpdateTablistName(player);
                } else
                    LOG.error("ChangeAFKMode -> Failed to get the server. 2");
            }

            data.LastPosition = EntityUtils.GetPosition(player);
            data.LastBlockPosition = EntityUtils.GetBlockPosition(player);
            data.HeadRotation = player.yHeadRot;
            data.Date = LocalDateTime.now();
            PutPlayerData(uuid, data);
        }
    }

    public static void WakeUp(ServerLevel world, MinecraftServer server) {
        var worldKey = WorldUtils.GetName(world);
        LOG.debug("World Key: {}", worldKey);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {

            if (GetRequiredPlayersToReset(server, worldKey) <= 0) {
                _lastWorldSleepReset = worldKey;
                if (world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                    var currentDayTime = world.getDayTime();
                    world.setDayTime(currentDayTime + 24000L - currentDayTime % 24000L);
                }

                if (world.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
                    world.setWeatherParameters(0, 0, false, false);
                }


                for (var serverPlayerEntity : server.getPlayerList().getPlayers()) {
                    if (serverPlayerEntity.isSleeping()) {
                        serverPlayerEntity.stopSleepInBed(true, true);
                    }
                }
            }
        }, 3, TimeUnit.SECONDS);

        executorService.schedule(() -> {
            if (_lastWorldSleepReset.equals(worldKey))
                _lastWorldSleepReset = null;
        }, 3, TimeUnit.SECONDS);
        executorService.shutdown();
    }
}
