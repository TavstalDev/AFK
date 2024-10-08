package io.github.tavstal.afk.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.tavstal.afk.CommonClass;
import io.github.tavstal.afk.utils.ModUtils;
import io.github.tavstal.afk.utils.PlayerUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class AFKCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("afk").executes((command) -> {
            return execute(command);
        }));
    }
    private static int execute(CommandContext<CommandSourceStack> command){
        try {
            if (command.getSource().getEntity() instanceof Player) {
                Player player = (Player) command.getSource().getEntity();

                if (player == null)
                    return 0;

                if (PlayerUtils.IsInCombat(player)) {
                    player.sendSystemMessage(ModUtils.Literal(CommonClass.CONFIG().CommandAFKCombatError));
                    return 0;
                }

                if (PlayerUtils.IsAFK(player.getStringUUID()))
                    CommonClass.ChangeAFKMode(player, false);
                else
                    CommonClass.ChangeAFKMode(player, true);
            }
            return Command.SINGLE_SUCCESS;
        }
        catch (Exception ex)
        {
            CommonClass.LOG.error("Error during executing command 'afk':");
            CommonClass.LOG.error(ex.getLocalizedMessage());
            return 0;
        }
    }
}