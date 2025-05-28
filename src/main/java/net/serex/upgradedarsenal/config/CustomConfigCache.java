package net.serex.upgradedarsenal.config;

import net.minecraft.ChatFormatting;
import net.serex.upgradedarsenal.modifier.ModifierRegistry;
import java.util.HashMap;
import java.util.Map;

public class CustomConfigCache {
    public static int REROLL_XP_COST;
    public static int MAX_REROLLS;

    // Cache for rarity configuration
    public static Map<ModifierRegistry.Rarity, RarityConfig> RARITY_CONFIG = new HashMap<>();

    public static class RarityConfig {
        public final ChatFormatting color;
        public final int weight;

        public RarityConfig(ChatFormatting color, int weight) {
            this.color = color;
            this.weight = weight;
        }
    }

    public static void reload() {
        REROLL_XP_COST = CustomConfig.REROLL_XP_COST.get();
        MAX_REROLLS = CustomConfig.MAX_REROLLS.get();

        // Load rarity configuration
        loadRarityConfig();
    }

    private static void loadRarityConfig() {
        RARITY_CONFIG.clear();
        System.out.println("[CustomConfigCache] Loading rarity configurations...");

        // Load each rarity configuration
        loadRarity(ModifierRegistry.Rarity.UNCHANGED, CustomConfig.UNCHANGED_COLOR.get(), CustomConfig.UNCHANGED_WEIGHT.get());
        loadRarity(ModifierRegistry.Rarity.COMMON, CustomConfig.COMMON_COLOR.get(), CustomConfig.COMMON_WEIGHT.get());
        loadRarity(ModifierRegistry.Rarity.UNCOMMON, CustomConfig.UNCOMMON_COLOR.get(), CustomConfig.UNCOMMON_WEIGHT.get());
        loadRarity(ModifierRegistry.Rarity.RARE, CustomConfig.RARE_COLOR.get(), CustomConfig.RARE_WEIGHT.get());
        loadRarity(ModifierRegistry.Rarity.EPIC, CustomConfig.EPIC_COLOR.get(), CustomConfig.EPIC_WEIGHT.get());
        loadRarity(ModifierRegistry.Rarity.LEGENDARY, CustomConfig.LEGENDARY_COLOR.get(), CustomConfig.LEGENDARY_WEIGHT.get());
        loadRarity(ModifierRegistry.Rarity.MYTHIC, CustomConfig.MYTHIC_COLOR.get(), CustomConfig.MYTHIC_WEIGHT.get());
        loadRarity(ModifierRegistry.Rarity.HERO, CustomConfig.HERO_COLOR.get(), CustomConfig.HERO_WEIGHT.get());

        // Calculate total weight for summary
        int totalWeight = RARITY_CONFIG.values().stream().mapToInt(config -> config.weight).sum();
        System.out.println("[CustomConfigCache] Loaded " + RARITY_CONFIG.size() + " rarities with total weight: " + totalWeight);
    }

    private static void loadRarity(ModifierRegistry.Rarity rarity, String colorName, int weight) {
        ChatFormatting color = ChatFormatting.getByName(colorName.toUpperCase());
        if (color == null) {
            System.err.println("[CustomConfigCache] Invalid color for " + rarity.name() + ": " + colorName + ". Using GRAY as default.");
            color = ChatFormatting.GRAY;
        }
        RARITY_CONFIG.put(rarity, new RarityConfig(color, weight));
        System.out.println("[CustomConfigCache] Loaded rarity: " + rarity.name() + " (Color: " + colorName + ", Weight: " + weight + ")");
    }
}
