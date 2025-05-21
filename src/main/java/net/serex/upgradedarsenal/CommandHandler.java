package net.serex.upgradedarsenal;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.config.CustomConfigCache;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.modifier.Modifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "upgradedarsenal", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandHandler {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        configReload(event);
        applyModifier(event);
        listModifiers(event);
    }

    private static void configReload(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("upgradedarsenal")
                        .then(Commands.literal("reloadconfig")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    CustomConfigCache.reload();
                                    context.getSource().sendSuccess(() -> Component.literal("upgradedarsenal config reloaded."), true);
                                    return 1;
                                })
                        )
        );
    }

    private static void applyModifier(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("upgradedarsenal")
                        .then(Commands.literal("applymodifier")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("modifier", ResourceLocationArgument.id())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            ResourceLocation modifierId = ResourceLocationArgument.getId(context, "modifier");

                                            // Ensure the modifier ID has the correct namespace
                                            if (!modifierId.getNamespace().equals("upgradedarsenal")) {
                                                modifierId = ResourceLocation.parse("upgradedarsenal:" + modifierId.getPath());
                                            }

                                            Modifier modifier = Modifiers.getModifier(modifierId);

                                            if (modifier == null) {
                                                context.getSource().sendFailure(Component.literal("Modifier not found: " + modifierId));
                                                return 0;
                                            }

                                            ItemStack heldItem = player.getMainHandItem();

                                            if (heldItem.isEmpty()) {
                                                context.getSource().sendFailure(Component.literal("You must hold an item in your main hand"));
                                                return 0;
                                            }

                                            if (!ModifierHandler.canHaveModifiers(heldItem)) {
                                                context.getSource().sendFailure(Component.literal("This item cannot have modifiers"));
                                                return 0;
                                            }

                                            ModifierHandler.applyModifier(heldItem, modifier, player);
                                            context.getSource().sendSuccess(() -> Component.literal("Applied modifier: " + modifier.getFormattedName().getString()), true);

                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static void listModifiers(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("upgradedarsenal")
                        .then(Commands.literal("listmodifiers")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                    // Get all modifiers
                                    Map<ResourceLocation, Modifier> modifiers = Modifiers.MODIFIERS;

                                    if (modifiers.isEmpty()) {
                                        context.getSource().sendFailure(Component.literal("No modifiers found"));
                                        return 0;
                                    }

                                    // Group modifiers by rarity
                                    List<Component> messages = new ArrayList<>();
                                    messages.add(Component.literal("Available Modifiers:").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

                                    for (Modifier.Rarity rarity : Modifier.Rarity.values()) {
                                        List<Modifier> rarityModifiers = new ArrayList<>();

                                        for (Modifier modifier : modifiers.values()) {
                                            if (modifier.rarity == rarity && !modifier.name.getPath().equals("unchanged")) {
                                                rarityModifiers.add(modifier);
                                            }
                                        }

                                        if (!rarityModifiers.isEmpty()) {
                                            messages.add(Component.literal(""));
                                            messages.add(Component.literal(rarity.name() + ":").withStyle(rarity.getColor(), ChatFormatting.BOLD));

                                            for (Modifier modifier : rarityModifiers) {
                                                String commandSuggestion = "/upgradedarsenal applymodifier " + modifier.name.getPath();
                                                Component modifierName = Component.literal("  • " + modifier.name.getPath())
                                                        .withStyle(style -> style
                                                                .withColor(rarity.getColor())
                                                                .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                                                        net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND,
                                                                        commandSuggestion
                                                                ))
                                                                .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                                                        net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                                                        Component.literal("Click to apply this modifier")
                                                                ))
                                                        );
                                                messages.add(modifierName);
                                            }
                                        }
                                    }

                                    // Send messages to player
                                    for (Component message : messages) {
                                        player.sendSystemMessage(message);
                                    }

                                    return 1;
                                })
                        )
        );
    }
}
