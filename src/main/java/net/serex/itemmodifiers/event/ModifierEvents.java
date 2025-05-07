package net.serex.itemmodifiers.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.ItemProcessingQueue;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

import java.util.List;

@Mod.EventBusSubscriber(modid="itemmodifiers")
public class ModifierEvents {


    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack craftedItem = event.getCrafting();
        Player player = event.getEntity();
        if (ModifierHandler.canHaveModifiers(craftedItem)) {
            ModifierHandler.processNewItem(craftedItem, player.getRandom().fork());
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

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Modifier modifier = ModifierHandler.getModifier(stack);
        if (modifier != null && modifier.rarity != Modifier.Rarity.UNCHANGED) {
            List<Component> tooltip = event.getToolTip();
            Component original = tooltip.get(0);
            Component updated = original.copy().withStyle(style -> style.withColor(modifier.rarity.getColor()).withItalic(false));
            tooltip.set(0, updated);
        }
    }
}

