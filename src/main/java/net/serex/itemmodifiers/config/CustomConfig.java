package net.serex.itemmodifiers.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CustomConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> DULL_DAMAGE_MODIFIER;
    public static final ForgeConfigSpec.ConfigValue<Double> SHARP_DAMAGE_MODIFIER;
    public static final ForgeConfigSpec.ConfigValue<Double> SWIFT_ATTACK_SPEED_MODIFIER;

    public static final ForgeConfigSpec.IntValue REROLL_XP_COST;
    public static final ForgeConfigSpec.IntValue MAX_REROLLS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_DUPLICATION_BLOCKS;

    static {
        BUILDER.push("Item Modifiers Config");

        DULL_DAMAGE_MODIFIER = BUILDER
                .comment("Damage modifier for Dull weapons")
                .define("dullDamageModifier", -1.5);

        SHARP_DAMAGE_MODIFIER = BUILDER
                .comment("Damage modifier for Sharp weapons")
                .define("sharpDamageModifier", 2.5);

        SWIFT_ATTACK_SPEED_MODIFIER = BUILDER
                .comment("Attack speed modifier for Swift weapons")
                .define("swiftAttackSpeedModifier", 2.0);

        BUILDER.pop(); // 👈 pop antes de abrir otra sección

        BUILDER.push("Duplication Settings");

        ALLOWED_DUPLICATION_BLOCKS = BUILDER
                .comment("List of blocks allowed to be duplicated when modifiers are active.")
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
                                "minecraft:deepslate_coal_ore",
                                "minecraft:iron_ore",
                                "minecraft:deepslate_iron_ore",
                                "minecraft:diamond_ore",
                                "minecraft:deepslate_diamond_ore",
                                "minecraft:ice",
                                "minecraft:packed_ice",
                                "minecraft:blue_ice",
                                "minecraft:cobblestone",
                                "minecraft:deepslate",
                                "minecraft:stone_bricks"
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
                .defineInRange("maxRerolls", 15, 0, 100);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
