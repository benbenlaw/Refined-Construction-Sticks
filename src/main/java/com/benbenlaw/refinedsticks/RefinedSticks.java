package com.benbenlaw.refinedsticks;


import com.benbenlaw.refinedsticks.containers.handlers.RefinedSticksContainerRegistrar;
import com.benbenlaw.refinedsticks.network.RefinedSticksMessages;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(RefinedSticks.MOD_ID)
public class RefinedSticks {
    public static final String MOD_ID = "refinedsticks";
    public static final Logger LOGGER = LogManager.getLogger();

    public RefinedSticks(final IEventBus eventBus, final ModContainer modContainer) {
        eventBus.addListener(this::networkingSetup);
        eventBus.addListener(RefinedSticks::commonSetup);
    }

    public void networkingSetup(RegisterPayloadHandlersEvent event) {
        RefinedSticksMessages.registerNetworking(event);
    }

    public static void commonSetup(final FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("refinedstorage")) {
            RefinedSticksContainerRegistrar.registerHandlers();
        }
    }
}