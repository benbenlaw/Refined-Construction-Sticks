package com.benbenlaw.refinedsticks.integration;

import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.support.network.item.NetworkItemHelperImpl;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Optional;

public class RSIntegration {

    public static final NetworkItemHelperImpl HELPER = new NetworkItemHelperImpl();

    public static InteractionResult tryBind(UseOnContext ctx) {
        return HELPER.bind(ctx);
    }

    public static Optional<GlobalPos> getLinkedNetwork(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.INSTANCE.getNetworkLocation()));
    }
}