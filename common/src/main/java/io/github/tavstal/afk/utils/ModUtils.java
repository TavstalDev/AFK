package io.github.tavstal.afk.utils;

import java.text.MessageFormat;

import com.mojang.brigadier.LiteralMessage;

import io.github.tavstal.afk.CommonClass;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Scoreboard;

public class ModUtils {
    public static Component Literal(String text) {
        return net.minecraft.network.chat.ComponentUtils.fromMessage(new LiteralMessage(text));
    }

    public static void BroadcastMessage(Entity entity, String text) {
        var server = entity.getServer();
        if (server == null)
        {
            CommonClass.LOG.error("BroadcastMessage -> Failed to get the server.");
            return;
        }

        var messageComponent = Literal(text);
        // Send Message to the server
        server.sendSystemMessage(messageComponent);
        // Send Message to all clients
        for (var player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(messageComponent);
        }
    }

    public static void BroadcastMessage(Entity entity, String text, Object ... args) {
        var server = entity.getServer();
        if (server == null)
        {
            CommonClass.LOG.error("BroadcastMessage -> Failed to get the server.");
            return;
        }

        var messageComponent = Literal(MessageFormat.format(text, args));
        // Send Message to the server
        server.sendSystemMessage(messageComponent);
        // Send Message to all clients
        for (var player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(messageComponent);
        }
    }

    public static void BroadcastMessageByWorld(Entity entity, String text, String worldKey) {
        var server = entity.getServer();
        if (server == null)
        {
            CommonClass.LOG.error("BroadcastMessageByWorld -> Failed to get the server.");
            return;
        }

        var messageComponent = Literal(text);
        // Send Message to the server
        server.sendSystemMessage(messageComponent);
        // Send Message to all clients
        for (var player : server.getPlayerList().getPlayers()) {
            if (WorldUtils.GetName(EntityUtils.GetLevel(player)).equals(worldKey))
                player.sendSystemMessage(messageComponent);
        }
    }

    public static void BroadcastMessageByWorld(Entity entity, String text, String worldKey, Object ... args) {
        var server = entity.getServer();
        if (server == null)
        {
            CommonClass.LOG.error("BroadcastMessageByWorld -> Failed to get the server.");
            return;
        }

        var messageComponent = Literal(MessageFormat.format(text, args));
        // Send Message to the server
        server.sendSystemMessage(messageComponent);
        // Send Message to all clients
        for (var player : server.getPlayerList().getPlayers()) {
            if (WorldUtils.GetName(EntityUtils.GetLevel(player)).equals(worldKey))
                player.sendSystemMessage(messageComponent);
        }
    }

    public static Scoreboard getServerScoreboard(MinecraftServer server) {
        try {
            // Get the main overworld level (you can adjust this if needed for other dimensions)
            ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);

            if (overworld != null) {
                return overworld.getScoreboard();
            }
            return  null;
        } catch (Exception e) {
            CommonClass.LOG.error("Failed to get server scoreboard.");
            CommonClass.LOG.error(e.getLocalizedMessage());
            return null;  // Or handle in a version-specific way
        }
    }
}
