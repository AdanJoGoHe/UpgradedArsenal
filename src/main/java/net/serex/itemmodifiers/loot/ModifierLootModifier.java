package net.serex.itemmodifiers.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Supplier;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

public class ModifierLootModifier extends LootModifier {
    public static final Supplier<Codec<ModifierLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst ->
                    codecStart(inst).apply(inst, ModifierLootModifier::new)
            )
    );

    protected ModifierLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        boolean isChestLoot = context.getQueriedLootTableId().getPath().contains("chests");

        generatedLoot.forEach(stack -> {
            if (ModifierHandler.canHaveModifiers(stack) && !ModifierHandler.hasBeenProcessed(stack)) {
                RandomSource random = context.getRandom();
                if (isChestLoot) {
                    ModifierHandler.processChestLoot(stack, random, context.getQueriedLootTableId());
                } else {
                    ModifierHandler.processNewItem(stack, null ,random);
                }
            }
        });

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}


