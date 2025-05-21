package net.serex.upgradedarsenal.eventHanlders;

import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for chest opening events.
 * Handles events related to opening chests and processing their contents.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class ChestOpenEventHandler {
    
    /**
     * Event handler for chest open.
     * Processes items in chests when they are opened.
     */
    @SubscribeEvent
    public static void onChestOpen(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof ChestMenu chestMenu && !event.getEntity().level().isClientSide()) {
            Level level = event.getEntity().level();
            EventUtil.processChestItems(chestMenu, level);
        }
    }
}