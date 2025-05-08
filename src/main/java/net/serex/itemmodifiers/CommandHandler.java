package net.serex.itemmodifiers;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.config.CustomConfigCache;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = "itemmodifiers", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandHandler {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        configReload(event);
        giveAttribute(event);

    }

    private static void configReload(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("itemmodifiers")
                        .then(Commands.literal("reloadconfig")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    CustomConfigCache.reload();
                                    context.getSource().sendSuccess(() -> Component.literal("ItemModifiers config reloaded."), true);
                                    return 1;
                                })
                        )
        );
    }
    private static void giveAttribute(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("itemmodifiers")
                .requires(source -> source.hasPermission(2)) // Only admins
                .then(Commands.literal("giveattribute")
                        .then(Commands.argument("attribute", ResourceLocationArgument.id())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        ModAttributes.ATTRIBUTES.getEntries().stream()
                                                .map(entry -> ForgeRegistries.ATTRIBUTES.getKey(entry.get()))
                                                .filter(Objects::nonNull)
                                                .map(ResourceLocation::toString),
                                        builder
                                ))

                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            ItemStack stack = player.getMainHandItem();

                                            ResourceLocation id = ResourceLocationArgument.getId(ctx, "attribute");
                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");

                                            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
                                            if (attribute == null) {
                                                ctx.getSource().sendFailure(Component.literal("Unknown attribute: " + id));
                                                return 0;
                                            }

                                            Modifier current = ModifierHandler.getModifier(stack);
                                            Modifier.ModifierBuilder builder = (current != null)
                                                    ? new Modifier.ModifierBuilder(current)
                                                    : new Modifier.ModifierBuilder(new ResourceLocation("itemmodifiers", "custom_debug"),
                                                    "Custom Debug",
                                                    Modifier.ModifierType.HELD)
                                                    .setDisplayName("Custom Debug")
                                                    .setRarity(Modifier.Rarity.UNCHANGED);

                                            builder.addModifier(() -> attribute, new Modifier.AttributeModifierSupplier(amount, AttributeModifier.Operation.ADDITION));

// ¡Combina en lugar de reemplazar!
                                            ModifierHandler.applyModifier(stack, builder.build());
                                            ModifierHandler.markAsProcessed(stack);



                                            ctx.getSource().sendSuccess(() -> Component.literal("Added attribute " + id + " with amount " + amount), false);
                                            return 1;
                                        })))));
    }
}

