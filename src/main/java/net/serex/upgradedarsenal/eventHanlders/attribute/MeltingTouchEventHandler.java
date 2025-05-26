package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;
import net.serex.upgradedarsenal.util.PlayerPlacedBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Event handler for the MELTING_TOUCH attribute.
 * Handles events related to auto-smelting blocks when broken.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class MeltingTouchEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.MELTING_TOUCH.get();
    }

    /**
     * Event handler for block breaking.
     * Auto-smelts drops based on the MELTING_TOUCH attribute.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative()) return;

        BlockState state = event.getState();
        LevelAccessor level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Asumiendo que EventUtil.loadAllowedBlocks() se llama en otro lugar (ej. al iniciar el juego)
        // EventUtil.loadAllowedBlocks(); // Usualmente no se carga en cada evento
        if (!EventUtil.getAllowedBlocks().contains(state.getBlock())) return;

        PlayerPlacedBlocks tracker = PlayerPlacedBlocks.get(serverLevel);
        if (tracker.isPlayerPlaced(event.getPos())) return;

        ItemStack heldItem = player.getMainHandItem();
        Modifier modifier = ModifierHandler.getModifier(heldItem);
        if (modifier == null) return;

        double meltingTouchChance = EventUtil.getMeltingTouchChance(modifier);
        if (meltingTouchChance <= 0 || serverLevel.getRandom().nextDouble() >= meltingTouchChance) return;

        // Cancela el evento original para prevenir los drops normales
        event.setCanceled(true);

        // Obtiene los drops del bloque
        List<ItemStack> originalDrops = Block.getDrops(state, serverLevel, event.getPos(), null, player, heldItem);
        if (originalDrops.isEmpty()) {
            // Si no hay drops, simplemente removemos el bloque
            serverLevel.removeBlock(event.getPos(), false);
            return;
        }

        // Procesa cada drop
        for (ItemStack drop : originalDrops) {
            if (drop.isEmpty()) continue;

            // Usamos un SimpleContainer para buscar la receta, ya que SingleItemContainer
            // podría no estar disponible directamente o tener otro nombre/paquete según la versión de MC/Forge.
            // También puedes crear tu propia implementación de Container que envuelva un solo ItemStack.
            Container container = new SimpleContainer(drop.copy()); // Es importante copiar el stack para no modificar el original
            // durante la búsqueda de receta si la receta lo consume.

            Optional<SmeltingRecipe> recipeOptional = serverLevel.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, container, serverLevel);

            if (recipeOptional.isPresent()) {
                SmeltingRecipe recipe = recipeOptional.get();
                ItemStack recipeOutput = recipe.getResultItem(serverLevel.registryAccess());

                if (!recipeOutput.isEmpty()) {
                    ItemStack smeltedItemResult = recipeOutput.copy();
                    // La cantidad final es la cantidad del drop original multiplicada por la cantidad que produce la receta.
                    // Para la mayoría de las recetas de fundición, recipeOutput.getCount() será 1.
                    // Esto maneja correctamente Fortuna (que afecta drop.getCount())
                    smeltedItemResult.setCount(drop.getCount() * recipeOutput.getCount());
                    Block.popResource(serverLevel, event.getPos(), smeltedItemResult);
                } else {
                    // Si la receta resulta en un item vacío (poco común para fundición), dropea el original
                    Block.popResource(serverLevel, event.getPos(), drop.copy());
                }
            } else {
                // Si no hay receta de fundición, dropea el ítem original
                Block.popResource(serverLevel, event.getPos(), drop.copy());
            }
        }

        // Rompe el bloque sin dropear ítems (ya los manejamos nosotros)
        serverLevel.removeBlock(event.getPos(), false);
    }

    // The onHarvestDrops method has been removed as we now handle everything in onBlockBreak

    /**
     * Simple container implementation for recipe matching
     */
    private static class SingleItemContainer implements net.minecraft.world.Container {
        private final ItemStack stack;

        public SingleItemContainer(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot == 0 ? stack : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }
    }
}
