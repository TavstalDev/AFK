package io.github.tavstal.afk;

import io.github.tavstal.afk.models.ConfigField;

public class CommonConfig {
    @ConfigField(order = 1, comment = "Shows more logs than usual. Helps locating errors.")
    public boolean EnableDebugMode;

    @ConfigField(order = 2)
    public boolean ShouldBroadcastMessages;
    @ConfigField(order = 3)
    public String TablistFormat;
    @ConfigField(order = 4)
    public String Prefix;
    @ConfigField(order = 5)
    public String Suffix;
    @ConfigField(order = 6)
    public int AutoAFKInterval;
    @ConfigField(order = 7)
    public int PlayerPercentToResetTime;
    @ConfigField(order = 8)
    public boolean ShowWorldTablist;
    @ConfigField(order = 9)
    public  String WorldPrefix;
    @ConfigField(order = 10)
    public String WorldSuffix;

    @ConfigField(order = 11)
    public boolean DisableOnAttackBlock;
    @ConfigField(order = 12)
    public boolean DisableOnAttackEntity;
    @ConfigField(order = 13)
    public boolean DisableOnUseBlock;
    @ConfigField(order = 14)
    public boolean DisableOnUseEntity;
    @ConfigField(order = 15)
    public boolean DisableOnUseItem;
    @ConfigField(order = 16)
    public boolean DisableOnWorldChange;
    @ConfigField(order = 17)
    public boolean DisableOnChatting;
    @ConfigField(order = 18)
    public boolean DisableOnMove;
    @ConfigField(order = 19)
    public boolean DisableOnRespawn;

    @ConfigField(order = 20)
    public String AFKOnMessage;
    @ConfigField(order = 21)
    public String AFKOffMessage;
    @ConfigField(order = 22)
    public String SleepStartMessage;
    @ConfigField(order = 23)
    public String SleepStopMessage;
    @ConfigField(order = 24)
    public String SleepResetMessage;
    @ConfigField(order = 25)
    public String CommandAFKCombatError;
    @ConfigField(order = 26)
    public String DayText;
    @ConfigField(order = 27)
    public  String NightText;
    @ConfigField(order = 28, comment = "How long does it take (in seconds) between hits to consider the player is not in combat.")
    public int CombatTimeout;

    @ConfigField(order = 100, comment = "DO NOT TOUCH THIS. This helps handlig config related changes after updates.")
    public int FileVersion;

    public CommonConfig() {
        EnableDebugMode = false;
        ShouldBroadcastMessages = false;
        TablistFormat = " §7[{0}§7] ";
        Prefix = "";
        Suffix = "§6AFK";
        AutoAFKInterval = 600;
        PlayerPercentToResetTime = 100;
        ShowWorldTablist = true;
        WorldPrefix = "";
        WorldSuffix = "§2{0}";

        DisableOnAttackBlock = true;
        DisableOnChatting = true;
        DisableOnMove = true;
        DisableOnAttackEntity = true;
        DisableOnRespawn = true;
        DisableOnUseBlock = true;
        DisableOnUseEntity = true;
        DisableOnUseItem = true;
        DisableOnWorldChange = true;

        AFKOnMessage = "§6{0} is now AFK.";
        AFKOffMessage = "§6{0} is no longer AFK.";
        SleepStartMessage = "§e{0} is sleeping. {1} player(s) needed to skip the {2}.";
        SleepStopMessage = "§c{0} stopped sleeping. {1} player(s) needed to skip the {2}.";
        SleepResetMessage = "§aSleeping through this {0}.";
        CommandAFKCombatError = "§cYou can not change your AFK state during combat.";
        DayText = "day";
        NightText = "night";

        CombatTimeout = 5;

        FileVersion = 1;
    }
}
