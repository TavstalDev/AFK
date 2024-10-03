package io.github.tavstal.afk.platform;

import io.github.tavstal.afk.platform.services.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isClientSide() {

        return FMLLoader.getDist().isClient();
    }

    @Override
    public boolean isServerSide() {

        return FMLLoader.getDist().isDedicatedServer();
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }
}
