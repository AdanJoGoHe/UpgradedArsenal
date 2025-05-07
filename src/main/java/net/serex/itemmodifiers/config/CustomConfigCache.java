package net.serex.itemmodifiers.config;

public class CustomConfigCache {
    public static int REROLL_XP_COST;
    public static int MAX_REROLLS;

    public static void reload() {
        REROLL_XP_COST = CustomConfig.REROLL_XP_COST.get();
        MAX_REROLLS = CustomConfig.MAX_REROLLS.get();
    }
}
