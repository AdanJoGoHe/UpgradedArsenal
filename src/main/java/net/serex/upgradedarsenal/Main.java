package net.serex.upgradedarsenal;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.config.CustomConfig;
import net.serex.upgradedarsenal.config.CustomConfigCache;
import net.serex.upgradedarsenal.config.ModifierLoader;
import net.serex.upgradedarsenal.eventHanlders.*;
import net.serex.upgradedarsenal.modifier.Modifiers;
import net.serex.upgradedarsenal.util.EventUtil;


@Mod(value= Main.MODID)
public class Main {
    public static final String MODID = "upgradedarsenal";

    public Main() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::setup);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        this.registerCommonEventHandlers();
        ModRegistry.init(modEventBus);
    }

    private void registerConfigSettings() {
         EventUtil.loadAllowedBlocks();
    }

    private void registerCommonEventHandlers() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CustomConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(ModifierEvents.class);
        MinecraftForge.EVENT_BUS.register(TooltipHandler.class);
        MinecraftForge.EVENT_BUS.register(net.serex.upgradedarsenal.modifier.ModifierHandler.class);
        MinecraftForge.EVENT_BUS.register(ServerStartingEvent.class);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Modifiers.init();
            ModifierLoader.loadAll();
        });
        registerConfigSettings();
        CustomConfigCache.reload();
    }
}
