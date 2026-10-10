package com.example.wyxjinengshu.energy;

import com.example.wyxjinengshu.WyxJinengshu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * 为技能核心方块注册 FE 能量能力，使其可被各类发电机/能量管道输入电量。
 */
@EventBusSubscriber(modid = WyxJinengshu.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EnergyRegistration {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                WyxJinengshu.SKILL_CORE_BE.get(),
                (be, side) -> be);
    }
}