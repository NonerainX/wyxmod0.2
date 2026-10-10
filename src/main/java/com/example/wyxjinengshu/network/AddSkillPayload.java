package com.example.wyxjinengshu.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.example.wyxjinengshu.WyxJinengshu;

/**
 * 客户端 -> 服务端：请求给指定技能升一级。
 */
public record AddSkillPayload(String skillId) implements CustomPacketPayload {

    public static final Type<AddSkillPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(WyxJinengshu.MODID, "add_skill"));

    public static final StreamCodec<ByteBuf, AddSkillPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
                    AddSkillPayload::skillId, AddSkillPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}