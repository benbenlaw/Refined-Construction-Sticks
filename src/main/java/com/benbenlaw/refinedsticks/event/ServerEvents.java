package com.benbenlaw.refinedsticks.event;

import com.benbenlaw.refinedsticks.RefinedSticks;
import com.benbenlaw.refinedsticks.integration.RSIntegration;
import com.benbenlaw.refinedsticks.network.StickJobPacket;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.controller.AbstractControllerBlock;
import com.refinedmods.refinedstorage.common.controller.ControllerBlock;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import mrbysco.constructionstick.items.stick.ItemStick;
import mrbysco.constructionstick.items.stick.ItemStickBasic;
import mrbysco.constructionstick.stick.StickJob;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

@EventBusSubscriber(modid = RefinedSticks.MOD_ID)
public class ServerEvents {

    private static final HashMap<Player, Set<BlockPos>> lastSentPositions = new HashMap<>();

    @SubscribeEvent
    public static void updateStickJob(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (!level.isClientSide()) {
            ItemStack heldItem = player.getMainHandItem();
            HitResult result = player.pick(player.blockInteractionRange(), 0.0F, false);

            if (heldItem.has(DataComponents.INSTANCE.getNetworkLocation())
                    && heldItem.is(TagKey.create(Registries.ITEM, ResourceLocation.parse("constructionstick:construction_sticks")))
                    && result instanceof BlockHitResult hitResult) {

                StickJob job = ItemStick.getStickJob(player, level, hitResult, heldItem);
                Set<BlockPos> currentPositions = job.getBlockPositions();

                if (!Objects.equals(lastSentPositions.get(player), currentPositions)) {
                    lastSentPositions.put(player, currentPositions);
                    PacketDistributor.sendToPlayer((ServerPlayer) player, new StickJobPacket(currentPositions));
                }
            } else {
                lastSentPositions.remove(player);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        Block block = event.getLevel().getBlockState(event.getHitVec().getBlockPos()).getBlock();

        if (!(block instanceof AbstractControllerBlock<?>)) return;
        if (!(stack.getItem() instanceof ItemStickBasic)) return;
        if (player.level().isClientSide) return;
        if (!Minecraft.getInstance().player.isCrouching()) return;

        UseOnContext ctx = new UseOnContext(
                player,
                event.getHand(),
                event.getHitVec()
        );

        InteractionResult result = RSIntegration.tryBind(ctx);

        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

}
