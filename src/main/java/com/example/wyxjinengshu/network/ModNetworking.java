package com.example.wyxjinengshu.network;

import com.example.wyxjinengshu.WyxJinengshu;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络通道注册。
 */
public final class ModNetworking {
    private ModNetworking() {
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(WyxJinengshu.MODID).versioned("1");

        registrar.playToServer(AddSkillPayload.TYPE, AddSkillPayload.STREAM_CODEC,
                ServerPayloadHandler::handleAddSkill);
        registrar.playToServer(RequestOpenPayload.TYPE, RequestOpenPayload.STREAM_CODEC,
                ServerPayloadHandler::handleRequestOpen);
        registrar.playToClient(SyncSkillDataPayload.TYPE, SyncSkillDataPayload.STREAM_CODEC,
                ClientPayloadHandler::handleSync);
    }
}