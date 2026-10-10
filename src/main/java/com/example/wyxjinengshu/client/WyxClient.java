package com.example.wyxjinengshu.client;

import com.example.wyxjinengshu.WyxJinengshu;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端 mod 总线注册：按键绑定。
 */
@EventBusSubscriber(modid = WyxJinengshu.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class WyxClient {

    public static final KeyMapping OPEN_SKILL = new KeyMapping(
            "key.wyxjinengshu.open_skill",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "key.categories.wyxjinengshu");

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SKILL);
    }
}