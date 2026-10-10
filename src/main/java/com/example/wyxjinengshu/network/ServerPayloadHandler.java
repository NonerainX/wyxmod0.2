package com.example.wyxjinengshu.network;

import com.example.wyxjinengshu.skills.SkillManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端网络包处理。
 */
public final class ServerPayloadHandler {
    private ServerPayloadHandler() {
    }

    public static void handleAddSkill(final AddSkillPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SkillManager.upgrade(player, payload.skillId());
            }
        });
    }

    /** 技能界面已改为纯客户端 Screen，这里只负责把最新技能数据同步给客户端 */
    public static void handleRequestOpen(final RequestOpenPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SkillManager.syncToClient(player);
            }
        });
    }
}