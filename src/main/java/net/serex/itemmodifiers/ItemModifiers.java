package net.serex.itemmodifiers;/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.api.distmarker.OnlyIn
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
 *  net.minecraftforge.fml.loading.FMLEnvironment
 */


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.serex.itemmodifiers.TooltipHandler;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.client.BowAnimationHandler;
import net.serex.itemmodifiers.event.ArmorEventHandler;
import net.serex.itemmodifiers.event.ChestOpenHandler;
import net.serex.itemmodifiers.event.CombatEventHandler;
import net.serex.itemmodifiers.event.DurabilityEventHandler;
import net.serex.itemmodifiers.event.EquipmentChangeHandler;
import net.serex.itemmodifiers.event.GrindstoneHandler;
import net.serex.itemmodifiers.event.MiningSpeedHandler;
import net.serex.itemmodifiers.event.ModifierEvents;
import net.serex.itemmodifiers.event.RangedWeaponEventHandler;
import net.serex.itemmodifiers.modifier.Modifiers;

@Mod(value="itemmodifiers")
public class ItemModifiers {
    public static final String MODID = "itemmodifiers";

    public ItemModifiers() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register((Object)this);
        modEventBus.addListener(this::setup);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        this.registerCommonEventHandlers();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            this.registerClientEventHandlers();
        }
        ModRegistry.init(modEventBus);
    }

    private void registerCommonEventHandlers() {
        MinecraftForge.EVENT_BUS.register(ModifierEvents.class);
        MinecraftForge.EVENT_BUS.register(TooltipHandler.class);
        MinecraftForge.EVENT_BUS.register(MiningSpeedHandler.class);
        MinecraftForge.EVENT_BUS.register(ArmorEventHandler.class);
        MinecraftForge.EVENT_BUS.register(CombatEventHandler.class);
        MinecraftForge.EVENT_BUS.register(net.serex.itemmodifiers.modifier.ModifierHandler.class);
        MinecraftForge.EVENT_BUS.register(EquipmentChangeHandler.class);
        MinecraftForge.EVENT_BUS.register(ChestOpenHandler.class);
        MinecraftForge.EVENT_BUS.register(DurabilityEventHandler.class);
        MinecraftForge.EVENT_BUS.register(RangedWeaponEventHandler.class);
        MinecraftForge.EVENT_BUS.register(GrindstoneHandler.class);
    }

    @OnlyIn(value=Dist.CLIENT)
    private void registerClientEventHandlers() {
        MinecraftForge.EVENT_BUS.register((Object)new BowAnimationHandler());
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(Modifiers::init);
    }
}
