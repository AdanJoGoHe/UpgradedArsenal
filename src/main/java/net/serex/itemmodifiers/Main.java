package net.serex.itemmodifiers;

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
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.client.BowAnimationHandler;
import net.serex.itemmodifiers.config.CustomConfig;
import net.serex.itemmodifiers.config.CustomConfigCache;
import net.serex.itemmodifiers.config.ModifierLoader;
import net.serex.itemmodifiers.config.RarityConfigLoader;
import net.serex.itemmodifiers.event.*;
import net.serex.itemmodifiers.modifier.Modifiers;


@Mod(value= Main.MODID)
public class Main {
    public static final String MODID = "itemmodifiers";

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
        MinecraftForge.EVENT_BUS.register(net.serex.itemmodifiers.modifier.ModifierHandler.class);
        MinecraftForge.EVENT_BUS.register(net.serex.itemmodifiers.event.ModifierEventHandler.class);
        MinecraftForge.EVENT_BUS.register(ServerStartingEvent.class);
    }

    @OnlyIn(value=Dist.CLIENT)
    private void registerClientEventHandlers() {
        MinecraftForge.EVENT_BUS.register(new BowAnimationHandler());
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(Modifiers::init);
        registerConfigSettings();
        CustomConfigCache.reload();

    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static class ModEventHandler {
        @SubscribeEvent
        public static void onAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener(new ModifierLoader());
            event.addListener(new RarityConfigLoader());
        }
    }
}
