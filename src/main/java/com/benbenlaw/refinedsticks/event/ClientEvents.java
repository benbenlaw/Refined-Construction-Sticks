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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Set;


@EventBusSubscriber(modid = RefinedSticks.MOD_ID, value = Dist.CLIENT)
class ClientEvents {

    @SubscribeEvent
    public static void tooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();

        if (!Minecraft.getInstance().hasShiftDown()) return;

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

        if (!stack.has(DataComponents.INSTANCE.getNetworkLocation()) && stack.is(TagKey.create(Registries.ITEM, Identifier.parse("constructionstick:construction_sticks")))) {
            lines.add(upgradesIndex++, Component.translatable("tooltip.refinedsticks.unlinked")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            GlobalPos globalPos = stack.get(DataComponents.INSTANCE.getNetworkLocation());
            if (globalPos != null) {
                BlockPos pos = globalPos.pos();
                String dim = globalPos.dimension().identifier().toString();

                lines.add(upgradesIndex++, Component.translatable("tooltip.refinedsticks.linked")
                        .withStyle(ChatFormatting.AQUA));
                lines.add(upgradesIndex++, Component.literal(dim).withStyle(ChatFormatting.GREEN));
                lines.add(upgradesIndex++, Component.literal(" [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }

    @SubscribeEvent
    public static void onExtractBlockOutlineRenderStateEvent(ExtractBlockOutlineRenderStateEvent event) {
        event.addCustomRenderer(new PreviewRender(event.getHitResult(), event.getCamera()));
    }


    private static StickJob stickJob;
    public static Set<BlockPos> undoBlocks;


    private static class PreviewRender implements CustomBlockOutlineRenderer {
        private final BlockHitResult target;
        private final Camera camera;

        public PreviewRender(BlockHitResult hitResult, Camera camera) {
            this.target = hitResult;
            this.camera = camera;
        }

        @Override
        public boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource buffer,
                              PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState) {
            Entity entity = camera.entity();
            if (!(entity instanceof Player player)) return false;
            Set<BlockPos> blocks;
            float colorR = 0, colorG = 0, colorB = 0;

            ItemStack stick = StickUtil.holdingStick(player);
            if (stick == null) return false;

            if (!KeybindHandler.KEY_SHOW_PREVIOUS.isDown()) {
                // Use cached stickJob for previews of the same target pos/dir
                // Exception: always update if blockCount < 2 to prevent 1-block previews when block updates
                // from the last placement are lagging
                if (stickJob == null || !compareRTR(stickJob.blockHitResult, target) || !(stickJob.stick.equals(stick))
                        || stickJob.blockCount() < 2) {
                    stickJob = ItemStick.getStickJob(player, player.level(), target, stick);
                }
                blocks = stickJob.getBlockPositions();
            } else {
                blocks = undoBlocks;
                colorG = 1;
            }

            if (stick.has(DataComponents.INSTANCE.getNetworkLocation())) {
                blocks = ClientStickJobHandler.getPositions();
                colorB = 1.0F;
            }

            if (blocks == null || blocks.isEmpty()) return false;

            VertexConsumer lineBuilder = buffer.getBuffer(RenderTypes.lines());

            double d0 = camera.position().x();
            double d1 = camera.position().y();
            double d2 = camera.position().z();

            for (BlockPos block : blocks) {
                AABB aabb = new AABB(block).move(-d0, -d1, -d2);
                ShapeRenderer.renderShape(poseStack, lineBuilder, Shapes.create(aabb), 0, 0, 0,
                        ARGB.colorFromFloat(0.4F, colorR, colorG, colorB), 2F);
            }

            return true;
        }


        public static void reset() {
            stickJob = null;
        }

        private static boolean compareRTR(BlockHitResult rtr1, BlockHitResult rtr2) {
            return rtr1.getBlockPos().equals(rtr2.getBlockPos()) && rtr1.getDirection().equals(rtr2.getDirection());
        }
    }
}

