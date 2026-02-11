package com.benbenlaw.refinedsticks.client;

import net.minecraft.core.BlockPos;

import java.util.Set;

public class ClientStickJobHandler {
    private static Set<BlockPos> positions;

    public static void setPositions(Set<BlockPos> pos) {
        positions = pos;
    }

    public static Set<BlockPos> getPositions() {
        return positions;
    }

    public static void clear() {
        positions = null;
    }
}