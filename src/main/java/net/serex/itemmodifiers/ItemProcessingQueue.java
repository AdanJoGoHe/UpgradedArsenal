package net.serex.itemmodifiers;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.modifier.ModifierHandler;

@Mod.EventBusSubscriber(modid="itemmodifiers")
public class ItemProcessingQueue {
    private static final Queue<QueuedItem> itemQueue = new ConcurrentLinkedQueue<QueuedItem>();

    public static void addItem(ItemStack stack, Player player) {
        itemQueue.offer(new QueuedItem(stack, player));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ItemProcessingQueue.processQueue();
        }
    }

    private static void processQueue() {
        QueuedItem queuedItem;
        while ((queuedItem = itemQueue.poll()) != null) {
            if (ModifierHandler.canHaveModifiers(queuedItem.stack) && !ModifierHandler.hasBeenProcessed(queuedItem.stack)) {
                ModifierHandler.processNewItem(queuedItem.stack, queuedItem.player, queuedItem.player.getRandom());
            }
            ModifierHandler.updateItemNameAndColor(queuedItem.stack);
        }
    }

    private static class QueuedItem {
        final ItemStack stack;
        final Player player;

        QueuedItem(ItemStack stack, Player player) {
            this.stack = stack;
            this.player = player;
        }
    }
}

