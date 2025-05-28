package net.serex.upgradedarsenal.modifier;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.serex.upgradedarsenal.AttributeEntry;
import net.serex.upgradedarsenal.ArsenalAttributes;

public class Modifiers {
    public static final Map<ResourceLocation, ModifierRegistry> MODIFIERS = new HashMap<>();
    public static final ModifierPool WEAPON_POOL = new ModifierPool();
    public static final ModifierPool TOOL_POOL = new ModifierPool();
    public static final ModifierPool ARMOR_POOL = new ModifierPool();

    public static ModifierRegistry UNCHANGED;

    static {
        ModifierRegistry.ModifierType held = ModifierRegistry.ModifierType.HELD;
        UNCHANGED = create("unchanged", "unchanged", held, ModifierRegistry.Rarity.UNCHANGED);
    }


    public static void clearPools() {
        WEAPON_POOL.clear();
        TOOL_POOL.clear();
        ARMOR_POOL.clear();
        MODIFIERS.clear();
    }

    private static ModifierRegistry create(String id, String name, ModifierRegistry.ModifierType type, ModifierRegistry.Rarity rarity, AttributeEntry... attributes) {
        ModifierRegistry.ModifierBuilder builder = new ModifierRegistry.ModifierBuilder(new ResourceLocation("upgradedarsenal", id), name, type)
                .setRarity(rarity);
        for (AttributeEntry attr : attributes) {
            builder.addModifier(attr.attribute(), attr.modifier());
        }
        return register(builder.build());
    }

    public static ModifierRegistry register(ModifierRegistry modifier) {
        MODIFIERS.put(modifier.name, modifier);
        classify(modifier);
        return modifier;
    }

    private static void classify(ModifierRegistry modifier) {
        if (modifier.type == ModifierRegistry.ModifierType.HELD) {
            if (modifier.hasAttribute(
                    ArsenalAttributes.MINING_SPEED.get(),
                    ArsenalAttributes.DOUBLE_DROP_CHANCE.get(),
                    ArsenalAttributes.MELTING_TOUCH.get(),
                    ArsenalAttributes.VEIN_MINER.get())
            ) {
                TOOL_POOL.add(modifier);
            } else {
                WEAPON_POOL.add(modifier);
            }
        } else if (modifier.type == ModifierRegistry.ModifierType.EQUIPPED) {
            ARMOR_POOL.add(modifier);
        }
    }

    public static ModifierRegistry getModifier(ResourceLocation name) {
        return MODIFIERS.get(name);
    }

    public static void init() {
        UNCHANGED = create("unchanged", "unchanged", ModifierRegistry.ModifierType.HELD, ModifierRegistry.Rarity.UNCHANGED);
    }
}
