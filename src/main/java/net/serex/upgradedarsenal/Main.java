package net.serex.upgradedarsenal;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.client.BowAnimationHandler;
import net.serex.upgradedarsenal.config.CustomConfig;
import net.serex.upgradedarsenal.config.CustomConfigCache;
import net.serex.upgradedarsenal.config.ModifierLoader;
import net.serex.upgradedarsenal.config.RarityConfigLoader;
import net.serex.upgradedarsenal.event.*;
import net.serex.upgradedarsenal.modifier.Modifiers;


@Mod(value= Main.MODID)
public class Main {
    public static final String MODID = "upgradedarsenal";

    public Main() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::setup);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        this.registerCommonEventHandlers();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            this.registerClientEventHandlers();
        }
        ModRegistry.init(modEventBus);
    }

    private void registerConfigSettings() {
         ModifierEventHandler.loadAllowedBlocks();
    }

    private void registerCommonEventHandlers() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CustomConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(ModifierEvents.class);
        MinecraftForge.EVENT_BUS.register(TooltipHandler.class);
        MinecraftForge.EVENT_BUS.register(net.serex.upgradedarsenal.modifier.ModifierHandler.class);
        MinecraftForge.EVENT_BUS.register(net.serex.upgradedarsenal.event.ModifierEventHandler.class);
        MinecraftForge.EVENT_BUS.register(ServerStartingEvent.class);
    }

    @OnlyIn(value=Dist.CLIENT)
    private void registerClientEventHandlers() {
        MinecraftForge.EVENT_BUS.register(new BowAnimationHandler());
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Modifiers.init();
            ModifierLoader.loadAll();
        });
        registerConfigSettings();
        CustomConfigCache.reload();
    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static class ModEventHandler {
        @SubscribeEvent
        public static void onAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener(new RarityConfigLoader());
        }
    }
}
