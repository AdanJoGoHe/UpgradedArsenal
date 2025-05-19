package net.serex.upgradedarsenal;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.serex.upgradedarsenal.modifier.ApplyModifiersToLoot;

public class ModRegistry {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create((ResourceKey)ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, (String)"upgradedarsenal");
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> MODIFIER_LOOT = LOOT_MODIFIER_SERIALIZERS.register("modifier_loot", ApplyModifiersToLoot.CODEC);

    public static void init(IEventBus modEventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
    }
}

