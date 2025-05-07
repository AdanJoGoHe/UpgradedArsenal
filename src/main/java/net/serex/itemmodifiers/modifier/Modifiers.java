package net.serex.itemmodifiers.modifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.serex.itemmodifiers.attribute.ModAttributes;

public class Modifiers {
    public static final Map<ResourceLocation, Modifier> MODIFIERS = new HashMap<>();
    public static final ModifierPool WEAPON_POOL = new ModifierPool();
    public static final ModifierPool TOOL_POOL = new ModifierPool();
    public static final ModifierPool ARMOR_POOL = new ModifierPool();
    public static final ModifierPool RANGED_POOL = new ModifierPool();

    // Example Modifier
    public static final Modifier UNCHANGED = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers", "unchanged"), "unchanged", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.UNCHANGED)
                    .build()
    );

    // ... All other Modifier definitions (shortened for readability)
    // public static final Modifier LIGHTWEIGHT_BOW = register(...);

    private static Modifier register(Modifier modifier) {
        MODIFIERS.put(modifier.name, modifier);
        if (modifier.type == Modifier.ModifierType.HELD) {
            if (modifier.modifiers.stream().anyMatch(pair -> {
                Attribute attr = ((Supplier<Attribute>) pair.getKey()).get();
                return attr == ModAttributes.DRAW_SPEED.get()
                        || attr == ModAttributes.PROJECTILE_VELOCITY.get()
                        || attr == ModAttributes.PROJECTILE_DAMAGE.get()
                        || attr == ModAttributes.PROJECTILE_ACCURACY.get();
            })) {
                RANGED_POOL.add(modifier);
            } else if (modifier.modifiers.stream().anyMatch(pair -> {
                Attribute attr = ((Supplier<Attribute>) pair.getKey()).get();
                return attr == ModAttributes.MINING_SPEED.get()
                        || attr == ModAttributes.DOUBLE_DROP_CHANCE.get();
            })) {
                TOOL_POOL.add(modifier);
            } else {
                WEAPON_POOL.add(modifier);
            }
        } else if (modifier.type == Modifier.ModifierType.EQUIPPED) {
            ARMOR_POOL.add(modifier);
        }
        return modifier;
    }

    public static Modifier getModifier(ResourceLocation name) {
        return MODIFIERS.get(name);
    }

    public static void init() {
        // Ensures static initialization
    }
}


