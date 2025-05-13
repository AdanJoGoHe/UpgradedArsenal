package net.serex.upgradedarsenal.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.serex.upgradedarsenal.ItemProcessingQueue;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;

import java.util.List;

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

    private static int globalTickCounter = 0;
    private static final int SYNC_INTERVAL_TICKS = 20 * 60; // 1 minuto

//    @SubscribeEvent
//    public static void onServerTick(TickEvent.ServerTickEvent event) {
//        if (event.phase != TickEvent.Phase.END) return;
//
//        globalTickCounter++;
//        if (globalTickCounter >= SYNC_INTERVAL_TICKS) {
//            globalTickCounter = 0;
//
//            // Forzar sync a todos los jugadores
//            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
//            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
//                ModifierHandler.syncAllItems(player);
//                player.sendSystemMessage(Component.literal("[upgradedarsenal] Sincronización automática de modificadores"));
//            }
//        }
//    }

}

