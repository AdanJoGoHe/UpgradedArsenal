package net.serex.upgradedarsenal.modifier;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.serex.upgradedarsenal.AttributeEntry;
import net.serex.upgradedarsenal.attribute.ModAttributes;

public class Modifiers {
    public static final Map<ResourceLocation, Modifier> MODIFIERS = new HashMap<>();
    public static final ModifierPool WEAPON_POOL = new ModifierPool();
    public static final ModifierPool TOOL_POOL = new ModifierPool();
    public static final ModifierPool ARMOR_POOL = new ModifierPool();
    public static final ModifierPool RANGED_POOL = new ModifierPool();

    // Public access to modifiers
    public static Modifier UNCHANGED;

    static {
        Modifier.ModifierType held = Modifier.ModifierType.HELD;

        // Unchanged Modifier
        UNCHANGED = create("unchanged", "unchanged", held, Modifier.Rarity.UNCHANGED);
    }


    public static void clearPools() {
        WEAPON_POOL.clear();
        TOOL_POOL.clear();
        ARMOR_POOL.clear();
        RANGED_POOL.clear();
        MODIFIERS.clear();
    }

    private static Modifier create(String id, String name, Modifier.ModifierType type, Modifier.Rarity rarity, AttributeEntry... attributes) {
        Modifier.ModifierBuilder builder = new Modifier.ModifierBuilder(new ResourceLocation("upgradedarsenal", id), name, type)
                .setRarity(rarity);
        for (AttributeEntry attr : attributes) {
            builder.addModifier(attr.attribute(), attr.modifier());
        }
        return register(builder.build());
    }

    public static Modifier register(Modifier modifier) {
        MODIFIERS.put(modifier.name, modifier);
        classify(modifier);
        return modifier;
    }

    private static void classify(Modifier modifier) {
        if (modifier.type == Modifier.ModifierType.HELD) {
            if (modifier.hasAttribute(
                    ModAttributes.DRAW_SPEED.get(),
                    ModAttributes.PROJECTILE_VELOCITY.get(),
                    ModAttributes.PROJECTILE_DAMAGE.get(),
                    ModAttributes.PROJECTILE_ACCURACY.get())
            ) {
                RANGED_POOL.add(modifier);
            } else if (modifier.hasAttribute(
                    ModAttributes.MINING_SPEED.get(),
                    ModAttributes.DOUBLE_DROP_CHANCE.get())
            ) {
                TOOL_POOL.add(modifier);
            } else {
                WEAPON_POOL.add(modifier);
            }
        } else if (modifier.type == Modifier.ModifierType.EQUIPPED) {
            ARMOR_POOL.add(modifier);
        }
    }

    public static Modifier getModifier(ResourceLocation name) {
        return MODIFIERS.get(name);
    }

    public static void init() {
        UNCHANGED = create("unchanged", "unchanged", Modifier.ModifierType.HELD, Modifier.Rarity.UNCHANGED);
    }
}
