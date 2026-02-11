package com.benbenlaw.refinedsticks.network;

import com.benbenlaw.refinedsticks.RefinedSticks;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RefinedSticksMessages {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(RefinedSticks.MOD_ID);

        registrar.playToClient(StickJobPacket.TYPE, StickJobPacket.STREAM_CODEC, StickJobPacket.HANDLER);
    }
}
