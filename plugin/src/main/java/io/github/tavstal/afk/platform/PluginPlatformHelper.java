package io.github.tavstal.afk.platform;

import io.github.tavstal.afk.platform.services.IPlatformHelper;

public class PluginPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Paper";
    }

    // Note: plugin is always server side
    @Override
    public boolean isClientSide() {

        return false;
    }

    @Override
    public boolean isServerSide() {

        return true;
    }

    @Override
    public boolean isModLoaded(String modId) {

        return false;
    }

    /*@Override
    public boolean isPlugin() {
        return true;
    }*/

    @Override
    public boolean isDevelopmentEnvironment() {

        return false;
    }
}
