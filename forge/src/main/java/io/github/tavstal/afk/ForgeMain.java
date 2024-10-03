package io.github.tavstal.afk;

import io.github.tavstal.afk.platform.ForgePlatformHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(CommonClass.MOD_ID)
public class ForgeMain {
    public ForgeMain() {
        var helper = new ForgePlatformHelper();
        if (helper.isClientSide()) {
            CommonClass.LOG.error("{} should be only loaded on the server.", CommonClass.MOD_NAME);
            return;
        }
        // Use Forge to bootstrap the Common mod.
        MinecraftForge.EVENT_BUS.register(new EventListener());
    }
}
