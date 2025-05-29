package net.serex.upgradedarsenal.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import java.util.List;

@Mod.EventBusSubscriber(modid="upgradedarsenal", bus=Mod.EventBusSubscriber.Bus.MOD)
public class CustomConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue REROLL_XP_COST;
    public static final ForgeConfigSpec.IntValue MAX_REROLLS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_DUPLICATION_BLOCKS;

    // Rarity configuration
    public static final ForgeConfigSpec.ConfigValue<String> UNCHANGED_COLOR;
    public static final ForgeConfigSpec.IntValue UNCHANGED_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<String> COMMON_COLOR;
    public static final ForgeConfigSpec.IntValue COMMON_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<String> UNCOMMON_COLOR;
    public static final ForgeConfigSpec.IntValue UNCOMMON_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<String> RARE_COLOR;
    public static final ForgeConfigSpec.IntValue RARE_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<String> EPIC_COLOR;
    public static final ForgeConfigSpec.IntValue EPIC_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<String> LEGENDARY_COLOR;
    public static final ForgeConfigSpec.IntValue LEGENDARY_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<String> MYTHIC_COLOR;
    public static final ForgeConfigSpec.IntValue MYTHIC_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<String> HERO_COLOR;
    public static final ForgeConfigSpec.IntValue HERO_WEIGHT;

    static {
        BUILDER.push("Duplication/Smelting Settings");

        ALLOWED_DUPLICATION_BLOCKS = BUILDER
                .comment("List of blocks allowed to be duplicated and/or smelted when modifiers are active.")
                .defineListAllowEmpty(
                        "allowedDuplicationBlocks",
                        List.of(
                                "minecraft:stone",
                                "minecraft:andesite",
                                "minecraft:granite",
                                "minecraft:diorite",
                                "minecraft:oak_log",
                                "minecraft:spruce_log",
                                "minecraft:birch_log",
                                "minecraft:jungle_log",
                                "minecraft:acacia_log",
                                "minecraft:dark_oak_log",
                                "minecraft:coal_ore",
                                "minecraft:copper_ore",
                                "minecraft:deepslate_coal_ore",
                                "minecraft:iron_ore",
                                "minecraft:gold_ore",
                                "minecraft:deepslate_iron_ore",
                                "minecraft:diamond_ore",
                                "minecraft:deepslate_diamond_ore",
                                "minecraft:ice",
                                "minecraft:packed_ice",
                                "minecraft:blue_ice",
                                "minecraft:cobblestone",
                                "minecraft:deepslate",
                                "minecraft:stone_bricks",
                                "minecraft:sand"
                        ),
                        obj -> obj instanceof String
                );

        BUILDER.pop();

        BUILDER.push("Grindstone Settings");

        REROLL_XP_COST = BUILDER
                .comment("XP cost for each grindstone re-roll")
                .defineInRange("rerollXpCost", 5, 0, 100);

        MAX_REROLLS = BUILDER
                .comment("Maximum number of grindstone re-rolls allowed per item")
                .defineInRange("maxRerolls", 30, 0, 100);

        BUILDER.pop();

        BUILDER.push("Rarities");
        BUILDER.comment("Configuration for item rarities. Colors must be valid Minecraft color names (gray, green, blue, light_purple, yellow, gold, dark_red, etc.)");

        UNCHANGED_COLOR = BUILDER
                .comment("Color for UNCHANGED rarity")
                .define("unchanged.color", "gray");
        UNCHANGED_WEIGHT = BUILDER
                .comment("Weight for UNCHANGED rarity (higher values = more common)")
                .defineInRange("unchanged.weight", 175, 1, 1000);

        COMMON_COLOR = BUILDER
                .comment("Color for COMMON rarity")
                .define("common.color", "gray");
        COMMON_WEIGHT = BUILDER
                .comment("Weight for COMMON rarity (higher values = more common)")
                .defineInRange("common.weight", 350, 1, 1000);

        UNCOMMON_COLOR = BUILDER
                .comment("Color for UNCOMMON rarity")
                .define("uncommon.color", "green");
        UNCOMMON_WEIGHT = BUILDER
                .comment("Weight for UNCOMMON rarity (higher values = more common)")
                .defineInRange("uncommon.weight", 450, 1, 1000);

        RARE_COLOR = BUILDER
                .comment("Color for RARE rarity")
                .define("rare.color", "blue");
        RARE_WEIGHT = BUILDER
                .comment("Weight for RARE rarity (higher values = more common)")
                .defineInRange("rare.weight", 300, 1, 1000);

        EPIC_COLOR = BUILDER
                .comment("Color for EPIC rarity")
                .define("epic.color", "light_purple");
        EPIC_WEIGHT = BUILDER
                .comment("Weight for EPIC rarity (higher values = more common)")
                .defineInRange("epic.weight", 250, 1, 1000);

        LEGENDARY_COLOR = BUILDER
                .comment("Color for LEGENDARY rarity")
                .define("legendary.color", "yellow");
        LEGENDARY_WEIGHT = BUILDER
                .comment("Weight for LEGENDARY rarity (higher values = more common)")
                .defineInRange("legendary.weight", 125, 1, 1000);

        MYTHIC_COLOR = BUILDER
                .comment("Color for MYTHIC rarity")
                .define("mythic.color", "gold");
        MYTHIC_WEIGHT = BUILDER
                .comment("Weight for MYTHIC rarity (higher values = more common)")
                .defineInRange("mythic.weight", 75, 1, 1000);

        HERO_COLOR = BUILDER
                .comment("Color for HERO rarity")
                .define("hero.color", "dark_red");
        HERO_WEIGHT = BUILDER
                .comment("Weight for HERO rarity (higher values = more common)")
                .defineInRange("hero.weight", 50, 1, 1000);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
