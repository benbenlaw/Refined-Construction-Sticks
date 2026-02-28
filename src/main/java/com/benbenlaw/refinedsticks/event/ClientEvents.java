package com.benbenlaw.refinedsticks.event;

import com.benbenlaw.refinedsticks.RefinedSticks;
import com.benbenlaw.refinedsticks.client.ClientStickJobHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import mrbysco.constructionstick.basics.StickUtil;
import mrbysco.constructionstick.client.KeybindHandler;
import mrbysco.constructionstick.items.stick.ItemStick;
import mrbysco.constructionstick.stick.StickJob;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Set;


@EventBusSubscriber(modid = RefinedSticks.MOD_ID, value = Dist.CLIENT)
class ClientEvents {

    @SubscribeEvent
    public static void tooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();

        if (!Screen.hasShiftDown()) return;

        int upgradesIndex = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i).getString().toLowerCase();
            if (text.contains("upgrade")) {
                upgradesIndex = i;
                break;
            }
        }

        while (upgradesIndex > 0 && lines.get(upgradesIndex - 1).getString().trim().isEmpty()) {
            upgradesIndex--;
        }

        if (!stack.has(DataComponents.INSTANCE.getNetworkLocation()) && stack.is(TagKey.create(Registries.ITEM, ResourceLocation.parse("constructionstick:construction_sticks")))) {
            lines.add(upgradesIndex++, Component.translatable("tooltip.refinedsticks.unlinked")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            GlobalPos globalPos = stack.get(DataComponents.INSTANCE.getNetworkLocation());
            if (globalPos != null) {
                BlockPos pos = globalPos.pos();
                String dim = globalPos.dimension().location().toString();

                lines.add(upgradesIndex++, Component.translatable("tooltip.refinedsticks.linked")
                        .withStyle(ChatFormatting.AQUA));
                lines.add(upgradesIndex++, Component.literal(dim).withStyle(ChatFormatting.GREEN));
                lines.add(upgradesIndex++, Component.literal(" [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }



    private static StickJob stickJob;
    public static Set<BlockPos> undoBlocks;

    @SubscribeEvent
    public static void renderBlockHighlight(RenderHighlightEvent.Block event) {
        if (event.getTarget().getType() == HitResult.Type.BLOCK) {
            BlockHitResult target = event.getTarget();
            Entity entity = event.getCamera().getEntity();
            if (entity instanceof Player player) {
                float colorR = 0.0F;
                float colorG = 0.0F;
                float colorB = 0.0F;
                ItemStack stick = StickUtil.holdingStick(player);
                if (stick != null) {
                    Set<BlockPos> blocks;
                    if (!KeybindHandler.KEY_SHOW_PREVIOUS.isDown()) {
                        if (stickJob == null || !compareRTR(stickJob.blockHitResult, target)
                                || !stickJob.stick.equals(stick) || stickJob.blockCount() < 2) {
                            stickJob = ItemStick.getStickJob(player, player.level(), target, stick);
                        }
                        blocks = stickJob.getBlockPositions();
                    } else {
                        blocks = undoBlocks;
                        colorG = 1.0F;
                    }

                    if (stick.has(DataComponents.INSTANCE.getNetworkLocation())) {
                        blocks = ClientStickJobHandler.getPositions();
                        colorB = 1.0F;
                    }

                    if (blocks != null && !blocks.isEmpty()) {
                        PoseStack ms = event.getPoseStack();
                        MultiBufferSource buffer = event.getMultiBufferSource();
                        VertexConsumer lineBuilder = buffer.getBuffer(RenderType.LINES);
                        Camera info = event.getCamera();
                        double d0 = info.getPosition().x();
                        double d1 = info.getPosition().y();
                        double d2 = info.getPosition().z();

                        for (BlockPos block : blocks) {
                            AABB aabb = (new AABB(block)).move(-d0, -d1, -d2);
                            LevelRenderer.renderLineBox(ms, lineBuilder, aabb, colorR, colorG, colorB, 0.4F);
                        }

                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    private static boolean compareRTR(BlockHitResult rtr1, BlockHitResult rtr2) {
        return rtr1.getBlockPos().equals(rtr2.getBlockPos()) && rtr1.getDirection().equals(rtr2.getDirection());
    }
}