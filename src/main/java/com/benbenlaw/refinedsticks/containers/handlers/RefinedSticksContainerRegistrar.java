package com.benbenlaw.refinedsticks.containers.handlers;

import mrbysco.constructionstick.ConstructionStick;

public class RefinedSticksContainerRegistrar {

    public static void registerHandlers() {
        ConstructionStick.containerManager.register(new HandlerRS());
        System.out.println("Registered Refined Container Handler");
    }
}
