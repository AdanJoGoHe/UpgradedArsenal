/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.ForgeConfigSpec
 *  net.minecraftforge.common.ForgeConfigSpec$Builder
 *  net.minecraftforge.common.ForgeConfigSpec$ConfigValue
 */
package net.serex.itemmodifiers.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Double> DULL_DAMAGE_MODIFIER;
    public static final ForgeConfigSpec.ConfigValue<Double> SHARP_DAMAGE_MODIFIER;
    public static final ForgeConfigSpec.ConfigValue<Double> SWIFT_ATTACK_SPEED_MODIFIER;

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

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}


