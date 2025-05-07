package net.serex.itemmodifiers;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.config.CustomConfigCache;

@Mod.EventBusSubscriber(modid = "itemmodifiers", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandHandler {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
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
}
