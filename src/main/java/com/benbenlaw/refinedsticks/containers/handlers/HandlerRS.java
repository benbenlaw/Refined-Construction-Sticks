package com.benbenlaw.refinedsticks.containers.handlers;

import com.benbenlaw.refinedsticks.integration.RSUtil;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import mrbysco.constructionstick.api.IContainerHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class HandlerRS implements IContainerHandler {

    @Override
    public boolean matches(Player player, ItemStack itemStack, ItemStack inventoryStack) {

        if (!inventoryStack.is(TagKey.create(
                Registries.ITEM,
                ResourceLocation.parse("constructionstick:construction_sticks")
        ))) {
            return false;
        }

        boolean isHeld =
                ItemStack.isSameItemSameComponents(inventoryStack, player.getMainHandItem()) ||
                        ItemStack.isSameItemSameComponents(inventoryStack, player.getOffhandItem());

        if (!isHeld) {
            return false;
        }

        if (!RSUtil.isLinkedToGrid(inventoryStack, player)) {
            return false;
        }

        return RSUtil.getStorage(inventoryStack, player) != null;
    }

    @Override
    public int countItems(Player player, ItemStack itemStack, ItemStack inventoryStack) {
        Network network = RSUtil.getStorage(inventoryStack, player);
        if (network == null) return 0;

        StorageNetworkComponent storage =
                network.getComponent(StorageNetworkComponent.class);
        if (storage == null) return 0;

        ItemResource resource = ItemResource.ofItemStack(itemStack);
        if (resource == null) return 0;

        long amount = storage.get(resource);
        return (int) Math.min(Integer.MAX_VALUE, amount);
    }



    @Override
    public int useItems(Player player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        Network network = RSUtil.getStorage(inventoryStack, player);
        if (network == null) return count;

        StorageNetworkComponent storage =
                network.getComponent(StorageNetworkComponent.class);
        if (storage == null) return count;

        ItemResource resource = ItemResource.ofItemStack(itemStack);
        if (resource == null) return count;

        long canExtract = storage.extract(resource, count, Action.SIMULATE, Actor.EMPTY);
        if (canExtract <= 0) return count;

        long extracted = storage.extract(resource, canExtract, Action.EXECUTE, Actor.EMPTY);
        return count - (int) extracted;
    }
}
