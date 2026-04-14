package com.benbenlaw.refinedsticks;


import com.benbenlaw.refinedsticks.containers.handlers.RefinedSticksContainerRegistrar;
import com.benbenlaw.refinedsticks.network.RefinedSticksMessages;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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