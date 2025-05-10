package net.serex.itemmodifiers.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ATTRIBUTES, (String)"itemmodifiers");
    public static final RegistryObject<Attribute> MINING_SPEED = ATTRIBUTES.register("mining_speed", () -> new RangedAttribute("attribute.itemmodifiers.mining_speed", 1.0, 0.0, 1024.0).setSyncable(true));
    public static final RegistryObject<Attribute> MAX_DURABILITY = ATTRIBUTES.register("max_durability", () -> new RangedAttribute("attribute.itemmodifiers.max_durability", 1.0, 0.0, 1024.0).setSyncable(true));
    public static final RegistryObject<Attribute> MOVEMENT_SPEED = ATTRIBUTES.register("movement_speed", () -> new RangedAttribute("attribute.itemmodifiers.movement_speed", 1.0, 0.0, 5.0).setSyncable(true));
    public static final RegistryObject<Attribute> DOUBLE_DROP_CHANCE = ATTRIBUTES.register("double_drop_chance", () -> new RangedAttribute("attribute.itemmodifiers.double_drop_chance", 0.0, 0.0, 1.0).setSyncable(true));
    public static final RegistryObject<Attribute> FALL_DAMAGE_RESISTANCE = ATTRIBUTES.register("fall_damage_resistance", () -> new RangedAttribute("attribute.itemmodifiers.fall_damage_resistance", 0.0, 0.0, 1.0).setSyncable(true));
    public static final RegistryObject<Attribute> DURABILITY_INCREASE = ATTRIBUTES.register("durability_increase", () -> new RangedAttribute("attribute.itemmodifiers.durability_increase", 1.0, 1.0, 10.0).setSyncable(true));
    public static final RegistryObject<Attribute> DRAW_SPEED = ATTRIBUTES.register("draw_speed", () -> new RangedAttribute("attribute.itemmodifiers.draw_speed", 1.0, 0.1, 5.0).setSyncable(true));
    public static final RegistryObject<Attribute> PROJECTILE_VELOCITY = ATTRIBUTES.register("projectile_velocity", () -> new RangedAttribute("attribute.itemmodifiers.projectile_velocity", 1.0, 0.0, 3.0).setSyncable(true));
    public static final RegistryObject<Attribute> PROJECTILE_DAMAGE = ATTRIBUTES.register("projectile_damage", () -> new RangedAttribute("attribute.itemmodifiers.projectile_damage", 1.0, 0.0, 5.0).setSyncable(true));
    public static final RegistryObject<Attribute> PROJECTILE_ACCURACY = ATTRIBUTES.register("projectile_accuracy", () -> new RangedAttribute("attribute.itemmodifiers.projectile_accuracy", 1.0, 0.0, 2.0).setSyncable(true));
    public static final RegistryObject<Attribute> JUMP_HEIGHT = ATTRIBUTES.register("jump_height", () -> new RangedAttribute("attribute.itemmodifiers.jump_height", 1.0, 0.0, 5.0).setSyncable(true));
    public static final RegistryObject<Attribute> FIRE_RESISTANCE = ATTRIBUTES.register("fire_resistance", () -> new RangedAttribute("attribute.itemmodifiers.fire_resistance", 0.0, 0.0, 1.0).setSyncable(true));
    public static final RegistryObject<Attribute> XP_GAIN_BONUS = ATTRIBUTES.register("xp_gain_bonus", () -> new RangedAttribute("attribute.itemmodifiers.xp_gain_bonus", 0.0, 0.0, 5.0).setSyncable(true));
    public static final RegistryObject<Attribute> RESPIRATION_EFFICIENCY = ATTRIBUTES.register("respiration_efficiency", () -> new RangedAttribute("attribute.itemmodifiers.respiration_efficiency", 1.0, 0.1, 10.0).setSyncable(true));
    public static final RegistryObject<Attribute> REGENERATION = ATTRIBUTES.register("regeneration", () -> new RangedAttribute("attribute.itemmodifiers.regeneration", 0.0, 0.0, 10.0).setSyncable(true));



}

