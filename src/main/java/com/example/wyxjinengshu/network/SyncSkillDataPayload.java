package com.example.wyxjinengshu.network;

import com.example.wyxjinengshu.WyxJinengshu;
import com.example.wyxjinengshu.skills.SkillData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 服务端 -> 客户端：技能点与技能等级全量同步。
 */
public record SyncSkillDataPayload(int points, int[] levels) implements CustomPacketPayload {

    public static final Type<SyncSkillDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(WyxJinengshu.MODID, "sync_skill_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkillDataPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SyncSkillDataPayload decode(RegistryFriendlyByteBuf buffer) {
                    int points = buffer.readVarInt();
                    int count = buffer.readVarInt();
                    int[] levels = new int[count];
                    for (int i = 0; i < count; i++) {
                        levels[i] = buffer.readVarInt();
                    }
                    return new SyncSkillDataPayload(points, levels);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, SyncSkillDataPayload value) {
                    buffer.writeVarInt(value.points);
                    buffer.writeVarInt(value.levels.length);
                    for (int level : value.levels) {
                        buffer.writeVarInt(level);
                    }
                }
            };

    public static SyncSkillDataPayload of(SkillData data) {
        return new SyncSkillDataPayload(data.getPoints(), data.getAllLevels());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}