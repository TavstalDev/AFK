package io.github.tavstal.afk;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CommonClass.MOD_ID)
public class NeoForgeMain {

    public NeoForgeMain(IEventBus eventBus) {
        var helper = new io.github.tavstal.afk.platform.NeoForgePlatformHelper();
        if (helper.isClientSide()) {
            CommonClass.LOG.error("{} should be only loaded on the server.",CommonClass.MOD_NAME);
            return;
        }

        NeoForge.EVENT_BUS.register(new EventListener());
    }
}
