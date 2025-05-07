package net.serex.itemmodifiers.modifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.serex.itemmodifiers.attribute.ModAttributes;

public class Modifiers {
    public static final Map<ResourceLocation, Modifier> MODIFIERS = new HashMap<>();
    public static final ModifierPool WEAPON_POOL = new ModifierPool();
    public static final ModifierPool TOOL_POOL = new ModifierPool();
    public static final ModifierPool ARMOR_POOL = new ModifierPool();
    public static final ModifierPool RANGED_POOL = new ModifierPool();

    // Default Modifier
    public static final Modifier UNCHANGED = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:unchanged"), "unchanged", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.UNCHANGED)
                    .build()
    );

    // WEAPON MODIFIERS - COMMON
    public static final Modifier DULL = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:dull"), "dull", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.COMMON)
                    .addModifier(() -> Attributes.ATTACK_DAMAGE, new Modifier.AttributeModifierSupplier(-0.1, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    public static final Modifier HEAVY = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:heavy"), "heavy", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.COMMON)
                    .addModifier(() -> Attributes.ATTACK_SPEED, new Modifier.AttributeModifierSupplier(-0.1, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .addModifier(() -> Attributes.ATTACK_DAMAGE, new Modifier.AttributeModifierSupplier(0.05, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    // WEAPON MODIFIERS - UNCOMMON
    public static final Modifier SHARP = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:sharp"), "sharp", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.UNCOMMON)
                    .addModifier(() -> Attributes.ATTACK_DAMAGE, new Modifier.AttributeModifierSupplier(0.1, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    public static final Modifier QUICK = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:quick"), "quick", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.UNCOMMON)
                    .addModifier(() -> Attributes.ATTACK_SPEED, new Modifier.AttributeModifierSupplier(0.1, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    // WEAPON MODIFIERS - RARE
    public static final Modifier DEADLY = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:deadly"), "deadly", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.RARE)
                    .addModifier(() -> Attributes.ATTACK_DAMAGE, new Modifier.AttributeModifierSupplier(0.15, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .addModifier(() -> Attributes.ATTACK_SPEED, new Modifier.AttributeModifierSupplier(0.05, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    // WEAPON MODIFIERS - EPIC
    public static final Modifier VICIOUS = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:vicious"), "vicious", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.EPIC)
                    .addModifier(() -> Attributes.ATTACK_DAMAGE, new Modifier.AttributeModifierSupplier(0.25, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    // WEAPON MODIFIERS - LEGENDARY
    public static final Modifier LEGENDARY_WEAPON = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:legendary"), "legendary", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.LEGENDARY)
                    .addModifier(() -> Attributes.ATTACK_DAMAGE, new Modifier.AttributeModifierSupplier(0.3, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .addModifier(() -> Attributes.ATTACK_SPEED, new Modifier.AttributeModifierSupplier(0.2, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    // TOOL MODIFIERS - COMMON
    public static final Modifier RUSTY = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:rusty"), "rusty", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.COMMON)
                    .addModifier(() -> ModAttributes.MINING_SPEED.get(), new Modifier.AttributeModifierSupplier(-0.1, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    // TOOL MODIFIERS - UNCOMMON
    public static final Modifier EFFICIENT = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:efficient"), "efficient", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.UNCOMMON)
                    .addModifier(() -> ModAttributes.MINING_SPEED.get(), new Modifier.AttributeModifierSupplier(0.15, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    // TOOL MODIFIERS - RARE
    public static final Modifier PROSPECTOR = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:prospector_tool"), "prospector_tool", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.RARE)
                    .addModifier(() -> ModAttributes.DOUBLE_DROP_CHANCE.get(), new Modifier.AttributeModifierSupplier(0.1, AttributeModifier.Operation.ADDITION))
                    .build()
    );
    // TOOL MODIFIERS - RARE
    public static final Modifier SEREXWILL = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:serex_will"), "serex_will", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.COMMON)
                    .addModifier(() -> ModAttributes.DOUBLE_DROP_CHANCE.get(), new Modifier.AttributeModifierSupplier(1, AttributeModifier.Operation.ADDITION))
                    .build()
    );

    // ARMOR MODIFIERS - COMMON
    public static final Modifier PADDED = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:padded"), "padded", Modifier.ModifierType.EQUIPPED)
                    .setRarity(Modifier.Rarity.COMMON)
                    .addModifier(() -> Attributes.ARMOR, new Modifier.AttributeModifierSupplier(1.0, AttributeModifier.Operation.ADDITION))
                    .build()
    );

    // ARMOR MODIFIERS - UNCOMMON
    public static final Modifier TOUGH = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:tough"), "tough", Modifier.ModifierType.EQUIPPED)
                    .setRarity(Modifier.Rarity.UNCOMMON)
                    .addModifier(() -> Attributes.ARMOR_TOUGHNESS, new Modifier.AttributeModifierSupplier(1.0, AttributeModifier.Operation.ADDITION))
                    .build()
    );

    // ARMOR MODIFIERS - RARE
    public static final Modifier PROTECTIVE = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:protective"), "protective", Modifier.ModifierType.EQUIPPED)
                    .setRarity(Modifier.Rarity.RARE)
                    .addModifier(() -> Attributes.ARMOR, new Modifier.AttributeModifierSupplier(2.0, AttributeModifier.Operation.ADDITION))
                    .addModifier(() -> Attributes.ARMOR_TOUGHNESS, new Modifier.AttributeModifierSupplier(1.0, AttributeModifier.Operation.ADDITION))
                    .build()
    );

    // RANGED MODIFIERS - COMMON
    public static final Modifier LIGHTWEIGHT_BOW = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:lightweightbow"), "lightweightbow", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.COMMON)
                    .addModifier(() -> ModAttributes.DRAW_SPEED.get(), new Modifier.AttributeModifierSupplier(0.1, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    // RANGED MODIFIERS - UNCOMMON
    public static final Modifier POWERFUL = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:powerful"), "powerful", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.UNCOMMON)
                    .addModifier(() -> ModAttributes.PROJECTILE_DAMAGE.get(), new Modifier.AttributeModifierSupplier(0.15, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

    // RANGED MODIFIERS - RARE
    public static final Modifier PRECISE = register(
            new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers:precisebow"), "precisebow", Modifier.ModifierType.HELD)
                    .setRarity(Modifier.Rarity.RARE)
                    .addModifier(() -> ModAttributes.PROJECTILE_ACCURACY.get(), new Modifier.AttributeModifierSupplier(0.2, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .build()
    );

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
