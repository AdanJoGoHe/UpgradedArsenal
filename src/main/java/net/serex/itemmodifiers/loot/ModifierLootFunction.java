package net.serex.itemmodifiers.loot;/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonObject
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.storage.loot.LootContext
 *  net.minecraft.world.level.storage.loot.Serializer
 *  net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
 *  net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction$Serializer
 *  net.minecraft.world.level.storage.loot.functions.LootItemFunctionType
 *  net.minecraft.world.level.storage.loot.predicates.LootItemCondition
 */

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.serex.itemmodifiers.modifier.ModifierHandler;

public class ModifierLootFunction
extends LootItemConditionalFunction {
    private static final LootItemFunctionType MODIFIER_FUNCTION = new LootItemFunctionType((net.minecraft.world.level.storage.loot.Serializer)new Serializer());

    protected ModifierLootFunction(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    protected ItemStack run(ItemStack stack, LootContext context) {
        if (ModifierHandler.canHaveModifiers(stack) && !ModifierHandler.hasBeenProcessed(stack)) {
            boolean isChestLoot = context.getQueriedLootTableId().getPath().contains("chests");
            RandomSource random = context.getRandom();
            if (isChestLoot) {
                ModifierHandler.processChestLoot(stack, random, context.getQueriedLootTableId());
            } else {
                ModifierHandler.processNewItem(stack, random);
            }
        }
        return stack;
    }

    public LootItemFunctionType m_7162_() {
        return MODIFIER_FUNCTION;
    }

    public static LootItemFunctionType getModifierFunction() {
        return MODIFIER_FUNCTION;
    }

    @Override
    public LootItemFunctionType getType() {
        return null;
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<ModifierLootFunction> {
        public ModifierLootFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootItemCondition[] conditionsIn) {
            return new ModifierLootFunction(conditionsIn);
        }
    }
}

