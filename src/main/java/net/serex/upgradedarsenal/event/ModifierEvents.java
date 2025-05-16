package net.serex.upgradedarsenal.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.ItemProcessingQueue;
import net.serex.upgradedarsenal.modifier.ModifierHandler;

@Mod.EventBusSubscriber(modid="upgradedarsenal")
public class ModifierEvents {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack craftedItem = event.getCrafting();
        Player player = event.getEntity();
        if (ModifierHandler.canHaveModifiers(craftedItem)) {
            ModifierHandler.processNewItem(craftedItem, player, player.getRandom());
            ModifierHandler.updateItemNameAndColor(craftedItem);
        }
        processInventory(player);
    }

    @SubscribeEvent
    public static void onInventoryChanged(PlayerEvent.ItemPickupEvent event) {
        ModifierEvents.processInventory(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ModifierEvents.processInventory(event.getEntity());
    }

    private static void processInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!ModifierHandler.canHaveModifiers(stack) || ModifierHandler.hasBeenProcessed(stack)) continue;
            ItemProcessingQueue.addItem(stack, player);
        }
    }
}

