package io.github.tavstal.afk.commands;

import com.mojang.brigadier.*;
import io.github.tavstal.afk.utils.ModUtils;
import net.minecraft.commands.*;
import com.mojang.brigadier.context.*;
import io.github.tavstal.afk.CommonClass;
import io.github.tavstal.afk.utils.PlayerUtils;
import net.minecraft.world.entity.player.Player;

public class AFKCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("afk").executes((command) -> {
            return execute(command);
        }));
    }
    private static int execute(CommandContext<CommandSourceStack> command){
        if(command.getSource().getEntity() instanceof Player){
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
}
