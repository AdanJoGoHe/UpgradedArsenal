package net.serex.itemmodifiers.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.modifier.ModifierHandler;

@Mod.EventBusSubscriber(modid = "itemmodifiers")
public class ChestOpenHandler {

    @SubscribeEvent
    public static void onChestOpen(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof ChestMenu chestMenu && !event.getEntity().level().isClientSide()) {
            Level level = event.getEntity().level();
            processChestItems(chestMenu, level.getRandom());
        }
    }

    private static void processChestItems(ChestMenu chestMenu, RandomSource random) {
        Container container = chestMenu.getContainer();
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty() || !ModifierHandler.canHaveModifiers(stack) || ModifierHandler.hasBeenProcessed(stack)) {
                continue;
            }
            ModifierHandler.processNewItem(stack, random);
            container.setItem(i, stack);
        }
    }
}


