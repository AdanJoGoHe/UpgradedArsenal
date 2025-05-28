package net.serex.upgradedarsenal.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ArsenalAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, "upgradedarsenal");

    private static final String PREFIX = "attribute.upgradedarsenal.";

    private static RegistryObject<Attribute> register(String name, double def, double min, double max) {
        return ATTRIBUTES.register(name,
                () -> new RangedAttribute(PREFIX + name, def, min, max).setSyncable(true));
    }

    public static final RegistryObject<Attribute> CRITICAL_CHANCE = register("critical_hit_chance", 0.0, 0.0, 1.0);
    public static final RegistryObject<Attribute> CRITICAL_DAMAGE = register("critical_damage", 0.0, 0.0, 5.0);
    public static final RegistryObject<Attribute> DOUBLE_DROP_CHANCE = register("double_drop_chance", 0.0, 0.0, 1.0);
    public static final RegistryObject<Attribute> DURABILITY_INCREASE = register("durability_increase", 1.0, 1.0, 10.0);
    public static final RegistryObject<Attribute> FALL_DAMAGE_RESISTANCE = register("fall_damage_resistance", 0.0, 0.0, 1.0);
    public static final RegistryObject<Attribute> FIRE_RESISTANCE = register("fire_resistance", 0.0, 0.0, 1.0);
    public static final RegistryObject<Attribute> JUMP_HEIGHT = register("jump_height", 1.0, 0.0, 5.0);
    public static final RegistryObject<Attribute> LIFESTEAL = register("lifesteal", 0.0, 0.0, 1.0);
    public static final RegistryObject<Attribute> MAX_DURABILITY = register("max_durability", 1.0, 0.0, 1024.0);
    public static final RegistryObject<Attribute> MELTING_TOUCH = register("melting_touch", 0.0, 0.0, 1.0);
    public static final RegistryObject<Attribute> MINING_SPEED = register("mining_speed", 1.0, 0.0, 1024.0);
    public static final RegistryObject<Attribute> MOVEMENT_SPEED = register("movement_speed", 1.0, 0.0, 5.0);
    public static final RegistryObject<Attribute> REGENERATION = register("regeneration", 0.0, 0.0, 10.0);
    public static final RegistryObject<Attribute> RESPIRATION_EFFICIENCY = register("respiration_efficiency", 1.0, 0.1, 10.0);
    public static final RegistryObject<Attribute> VEIN_MINER = register("vein_miner", 0.0, 0.0, 1.0);
    public static final RegistryObject<Attribute> XP_GAIN_BONUS = register("xp_gain_bonus", 0.0, 0.0, 5.0);
}
