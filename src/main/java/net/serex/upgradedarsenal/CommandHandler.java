package net.serex.upgradedarsenal;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.config.CustomConfigCache;

@Mod.EventBusSubscriber(modid = "upgradedarsenal", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandHandler {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        configReload(event);

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
}

