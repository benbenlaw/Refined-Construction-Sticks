package com.benbenlaw.refinedsticks.integration;


import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;
import mrbysco.constructionstick.items.stick.ItemStickBasic;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Set;

public class RSUtil {

    public static boolean isLinkedToGrid(ItemStack stick, Player player) {
        return stick.getItem() instanceof ItemStickBasic && getLinkedPos(stick) != null;
    }

    public static GlobalPos getLinkedPos(ItemStack stick) {
        return stick.has(DataComponents.INSTANCE.getNetworkLocation()) ? stick.get(DataComponents.INSTANCE.getNetworkLocation()) : null;
    }

    public static Network getStorage(ItemStack stick, Player player) {
        GlobalPos pos = getLinkedPos(stick);
        if (pos == null) return null;

        Level level = player.level();
        BlockEntity be = level.getBlockEntity(pos.pos());
        if (be == null) return null;

        NetworkNodeContainerProvider provider =
                RefinedStorageNeoForgeApi.INSTANCE
                        .getNetworkNodeContainerProviderCapability()
                        .getCapability(level, pos.pos(), be.getBlockState(), be, null);

        if (provider == null) return null;

        for (InWorldNetworkNodeContainer container : provider.getContainers()) {
            Network network = container.getNode().getNetwork();
            if (network != null) {
                return network;
            }
        }

        return null;
    }
}