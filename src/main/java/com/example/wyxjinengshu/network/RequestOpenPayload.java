package com.example.wyxjinengshu.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.example.wyxjinengshu.WyxJinengshu;

/**
 * 客户端 -> 服务端：请求打开技能书界面。
 */
public record RequestOpenPayload() implements CustomPacketPayload {

    public static final Type<RequestOpenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(WyxJinengshu.MODID, "request_open"));

    public static final StreamCodec<ByteBuf, RequestOpenPayload> STREAM_CODEC =
            StreamCodec.unit(new RequestOpenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}