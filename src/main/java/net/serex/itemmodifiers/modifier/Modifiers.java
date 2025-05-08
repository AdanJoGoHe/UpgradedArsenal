package net.serex.itemmodifiers.modifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.serex.itemmodifiers.AttributeEntry;
import net.serex.itemmodifiers.attribute.ModAttributes;

public class Modifiers {
    public static final Map<ResourceLocation, Modifier> MODIFIERS = new HashMap<>();
    public static final ModifierPool WEAPON_POOL = new ModifierPool();
    public static final ModifierPool TOOL_POOL = new ModifierPool();
    public static final ModifierPool ARMOR_POOL = new ModifierPool();
    public static final ModifierPool RANGED_POOL = new ModifierPool();

    // Public access to modifiers
    public static Modifier UNCHANGED;
    public static Modifier DULL;
    public static Modifier HEAVY;
    public static Modifier SHARP;
    public static Modifier QUICK;
    public static Modifier DEADLY;
    public static Modifier VICIOUS;
    public static Modifier LEGENDARY_WEAPON;
    public static Modifier RUSTY;
    public static Modifier EFFICIENT;
    public static Modifier PROSPECTOR;
    public static Modifier SEREXWILL;
    public static Modifier PADDED;
    public static Modifier TOUGH;
    public static Modifier PROTECTIVE;
    public static Modifier LIGHTWEIGHT_BOW;
    public static Modifier POWERFUL;
    public static Modifier PRECISE;

    public static Modifier BLUNT;
    public static Modifier LOOSE_GRIP;
    public static Modifier SOFT_SOLE;
    public static Modifier HARDHEADED;

    public static Modifier KEEN;
    public static Modifier BALANCED;
    public static Modifier QUICK_HANDS;
    public static Modifier ENDURING;

    public static Modifier HEAVY_STRIKES;
    public static Modifier BERSERKER;
    public static Modifier GROUNDED;
    public static Modifier SWIFT_STEPS;

    public static Modifier PHANTOM;
    public static Modifier CRACKSHOT;
    public static Modifier FORTIFIED;
    public static Modifier LUCKY_TOUCH;

    public static Modifier JUGGERNAUT;
    public static Modifier THUNDERCALLER;
    public static Modifier ADAMANT;
    public static Modifier MIDAS_GIFT;

    public static Modifier BLOODTHIRSTY;
    public static Modifier WARPED_REFLEXES;
    public static Modifier DIVINE_MOMENTUM;
    public static Modifier DEADEYE;

    public static Modifier TITANS_WRATH;
    public static Modifier ETHEREAL_GRACE;
    public static Modifier LEGENDS_FORTUNE;
    public static Modifier UNBREAKABLE;
    static {
        Modifier.ModifierType held = Modifier.ModifierType.HELD;
        Modifier.ModifierType equipped = Modifier.ModifierType.EQUIPPED;
        Modifier.Rarity common = Modifier.Rarity.COMMON;
        Modifier.Rarity uncommon = Modifier.Rarity.UNCOMMON;
        Modifier.Rarity rare = Modifier.Rarity.RARE;
        Modifier.Rarity legendary = Modifier.Rarity.LEGENDARY;
        Modifier.Rarity epic = Modifier.Rarity.EPIC;
        Modifier.Rarity mythic = Modifier.Rarity.MYTHIC;
        Modifier.Rarity hero = Modifier.Rarity.HERO;

        // Unchanged Modifier
        UNCHANGED = create("unchanged", "unchanged", held, Modifier.Rarity.UNCHANGED);

        // Weapon Modifiers
        DULL = create("dull", "dull", held, common,
                entry(Attributes.ATTACK_DAMAGE, -0.1));

        HEAVY = create("heavy", "heavy", held, common,
                entry(Attributes.ATTACK_SPEED, -0.1),
                entry(Attributes.ATTACK_DAMAGE, 0.05));

        SHARP = create("sharp", "sharp", held, uncommon,
                entry(Attributes.ATTACK_DAMAGE, 0.1));

        QUICK = create("quick", "quick", held, uncommon,
                entry(Attributes.ATTACK_SPEED, 0.1));

        DEADLY = create("deadly", "deadly", held, rare,
                entry(Attributes.ATTACK_DAMAGE, 0.15),
                entry(Attributes.ATTACK_SPEED, 0.05));

        VICIOUS = create("vicious", "vicious", held, epic,
                entry(Attributes.ATTACK_DAMAGE, 0.25));

        LEGENDARY_WEAPON = create("legendary", "legendary", held, legendary,
                entry(Attributes.ATTACK_DAMAGE, 0.3),
                entry(Attributes.ATTACK_SPEED, 0.2));

        // Tool Modifiers
        RUSTY = create("rusty", "rusty", held, common,
                entry(ModAttributes.MINING_SPEED.get(), -0.1));

        EFFICIENT = create("efficient", "efficient", held, uncommon,
                entry(ModAttributes.MINING_SPEED.get(), 0.15));

        PROSPECTOR = create("prospector_tool", "prospector_tool", held, rare,
                entry(ModAttributes.DOUBLE_DROP_CHANCE.get(), 0.1, AttributeModifier.Operation.ADDITION));

        SEREXWILL = create("serex_will", "serex_will", held, common,
                entry(ModAttributes.DOUBLE_DROP_CHANCE.get(), 1, AttributeModifier.Operation.ADDITION));

        // Armor Modifiers
        PADDED = create("padded", "padded", equipped, common,
                entry(Attributes.ARMOR, 1.0, AttributeModifier.Operation.ADDITION));

        TOUGH = create("tough", "tough", equipped, uncommon,
                entry(Attributes.ARMOR_TOUGHNESS, 1.0, AttributeModifier.Operation.ADDITION));

        PROTECTIVE = create("protective", "protective", equipped, rare,
                entry(Attributes.ARMOR, 2.0, AttributeModifier.Operation.ADDITION),
                entry(Attributes.ARMOR_TOUGHNESS, 1.0, AttributeModifier.Operation.ADDITION));

        // Ranged Modifiers
        LIGHTWEIGHT_BOW = create("lightweightbow", "lightweightbow", held, common,
                entry(ModAttributes.DRAW_SPEED.get(), 0.1));

        POWERFUL = create("powerful", "powerful", held, uncommon,
                entry(ModAttributes.PROJECTILE_DAMAGE.get(), 0.15));

        PRECISE = create("precisebow", "precisebow", held, rare,
                entry(ModAttributes.PROJECTILE_ACCURACY.get(), 0.2));

        BLUNT = create("blunt", "blunt", held, common, entry(Attributes.ATTACK_DAMAGE, -0.1));
        LOOSE_GRIP = create("loose_grip", "loose grip", held, common, entry(Attributes.ATTACK_SPEED, -0.1));
        SOFT_SOLE = create("soft_sole", "soft sole", held, common, entry(ModAttributes.MOVEMENT_SPEED.get(), 0.05));
        HARDHEADED = create("hardheaded", "hardheaded", equipped, common, entry(Attributes.ARMOR, 1.0, AttributeModifier.Operation.ADDITION));

        // UNCOMMON
        KEEN = create("keen", "keen", held, uncommon, entry(Attributes.ATTACK_DAMAGE, 0.1));
        BALANCED = create("balanced", "balanced", held, uncommon, entry(Attributes.ATTACK_SPEED, 0.05), entry(Attributes.ATTACK_DAMAGE, 0.05));
        QUICK_HANDS = create("quick_hands", "quick hands", held, uncommon, entry(ModAttributes.MINING_SPEED.get(), 0.1));
        ENDURING = create("enduring", "enduring", held, uncommon, entry(ModAttributes.MAX_DURABILITY.get(), 0.2));

        // RARE
        HEAVY_STRIKES = create("heavy_strikes", "heavy strikes", held, rare, entry(Attributes.ATTACK_DAMAGE, 0.15), entry(Attributes.ATTACK_SPEED, -0.05));
        BERSERKER = create("berserker", "berserker", held, rare, entry(Attributes.ATTACK_DAMAGE, 0.15), entry(Attributes.ARMOR, -1.0, AttributeModifier.Operation.ADDITION));
        GROUNDED = create("grounded", "grounded", held, rare, entry(Attributes.KNOCKBACK_RESISTANCE, 0.2, AttributeModifier.Operation.ADDITION));
        SWIFT_STEPS = create("swift_steps", "swift steps", held, rare, entry(ModAttributes.MOVEMENT_SPEED.get(), 0.1), entry(ModAttributes.FALL_DAMAGE_RESISTANCE.get(), 0.1));

        // EPIC
        PHANTOM = create("phantom", "phantom", held, epic, entry(ModAttributes.MOVEMENT_SPEED.get(), 0.2), entry(ModAttributes.PROJECTILE_ACCURACY.get(), 0.1));
        CRACKSHOT = create("crackshot", "crackshot", held, epic, entry(ModAttributes.PROJECTILE_DAMAGE.get(), 0.25));
        FORTIFIED = create("fortified", "fortified", equipped, epic, entry(Attributes.ARMOR, 2.0, AttributeModifier.Operation.ADDITION), entry(Attributes.ARMOR_TOUGHNESS, 1.0, AttributeModifier.Operation.ADDITION));
        LUCKY_TOUCH = create("lucky_touch", "lucky touch", held, epic, entry(ModAttributes.DOUBLE_DROP_CHANCE.get(), 0.1, AttributeModifier.Operation.ADDITION), entry(Attributes.LUCK, 1.0, AttributeModifier.Operation.ADDITION));

        // LEGENDARY
        JUGGERNAUT = create("juggernaut", "juggernaut", equipped, legendary, entry(Attributes.ARMOR, 4.0, AttributeModifier.Operation.ADDITION), entry(Attributes.ARMOR_TOUGHNESS, 2.0, AttributeModifier.Operation.ADDITION));
        THUNDERCALLER = create("thundercaller", "thundercaller", held, legendary, entry(ModAttributes.PROJECTILE_DAMAGE.get(), 0.3), entry(ModAttributes.DRAW_SPEED.get(), 0.2));
        ADAMANT = create("adamant", "adamant", held, legendary, entry(ModAttributes.MAX_DURABILITY.get(), 0.5), entry(Attributes.KNOCKBACK_RESISTANCE, 0.2, AttributeModifier.Operation.ADDITION));
        MIDAS_GIFT = create("midas_gift", "midas' gift", held, legendary, entry(ModAttributes.MINING_SPEED.get(), 0.2), entry(ModAttributes.DOUBLE_DROP_CHANCE.get(), 0.15, AttributeModifier.Operation.ADDITION));

        // MYTHIC
        BLOODTHIRSTY = create("bloodthirsty", "bloodthirsty", held, mythic, entry(Attributes.ATTACK_DAMAGE, 0.3), entry(Attributes.MAX_HEALTH, -4.0, AttributeModifier.Operation.ADDITION));
        WARPED_REFLEXES = create("warped_reflexes", "warped reflexes", held, mythic, entry(Attributes.ATTACK_SPEED, 0.4), entry(ModAttributes.MOVEMENT_SPEED.get(), 0.4));
        DIVINE_MOMENTUM = create("divine_momentum", "divine momentum", held, mythic, entry(ModAttributes.MINING_SPEED.get(), 0.25), entry(ModAttributes.DURABILITY_INCREASE.get(), 0.25));
        DEADEYE = create("deadeye", "deadeye", held, mythic, entry(ModAttributes.PROJECTILE_ACCURACY.get(), 0.4), entry(ModAttributes.PROJECTILE_DAMAGE.get(), 0.4));

        // HERO
        TITANS_WRATH = create("titans_wrath", "titan's wrath", held, hero, entry(Attributes.ATTACK_DAMAGE, 0.5), entry(Attributes.MAX_HEALTH, 4.0, AttributeModifier.Operation.ADDITION), entry(ModAttributes.MOVEMENT_SPEED.get(), -0.15));
        ETHEREAL_GRACE = create("ethereal_grace", "ethereal grace", held, hero, entry(ModAttributes.MOVEMENT_SPEED.get(), 0.5), entry(ModAttributes.FALL_DAMAGE_RESISTANCE.get(), 0.2), entry(ModAttributes.PROJECTILE_ACCURACY.get(), 0.15));
        LEGENDS_FORTUNE = create("legends_fortune", "legend's fortune", held, hero, entry(ModAttributes.DOUBLE_DROP_CHANCE.get(), 0.3, AttributeModifier.Operation.ADDITION), entry(Attributes.LUCK, 2.0, AttributeModifier.Operation.ADDITION), entry(ModAttributes.MINING_SPEED.get(), 0.25));
    }

    // ...rest of class remains the same (register, classifyModifier, etc.)


    private static AttributeEntry entry(Attribute attribute, double value) {
        return new AttributeEntry(() -> attribute, new Modifier.AttributeModifierSupplier(value, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static AttributeEntry entry(Attribute attribute, double value, AttributeModifier.Operation op) {
        return new AttributeEntry(() -> attribute, new Modifier.AttributeModifierSupplier(value, op));
    }

    private static Modifier create(String id, String name, Modifier.ModifierType type, Modifier.Rarity rarity, AttributeEntry... attributes) {
        Modifier.ModifierBuilder builder = new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers", id), name, type)
                .setRarity(rarity);
        for (AttributeEntry attr : attributes) {
            builder.addModifier(attr.attribute(), attr.modifier());
        }
        return register(builder.build());
    }

    private static Modifier register(Modifier modifier) {
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
