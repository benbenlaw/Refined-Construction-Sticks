package com.benbenlaw.refinedsticks.network;


import com.benbenlaw.refinedsticks.RefinedSticks;
import com.benbenlaw.refinedsticks.client.ClientStickJobHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.HashSet;
import java.util.Set;

public record StickJobPacket(Set<BlockPos> positions) implements CustomPacketPayload {

    public static final Type<StickJobPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RefinedSticks.MOD_ID, "stick_job_sync"));

    public static final IPayloadHandler<StickJobPacket> HANDLER = (packet, context) -> {

        ClientStickJobHandler.setPositions(packet.positions);
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, StickJobPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)),  StickJobPacket::positions,
            StickJobPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
