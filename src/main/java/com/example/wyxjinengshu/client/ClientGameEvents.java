package com.example.wyxjinengshu.client;

import com.example.wyxjinengshu.WyxJinengshu;
import com.example.wyxjinengshu.network.RequestOpenPayload;
import com.example.wyxjinengshu.ui.SkillScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 客户端游戏总线事件：按 N 打开技能书。
 * 界面为独立 Screen，直接客户端打开（同时向服务端请求一次最新技能数据）。
 */
@EventBusSubscriber(modid = WyxJinengshu.MODID, value = Dist.CLIENT)
public class ClientGameEvents {

    @SubscribeEvent
    public static void onClientTick(PlayerTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null && mc.player != null && WyxClient.OPEN_SKILL.consumeClick()) {
            PacketDistributor.sendToServer(new RequestOpenPayload());
            mc.setScreen(new SkillScreen());
        }
    }
}