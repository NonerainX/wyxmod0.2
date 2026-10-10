package com.example.wyxjinengshu.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置：能量-技能点换算与范围参数。
 */
public class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 1 个技能点所需的 FE 电量 */
    public static final ModConfigSpec.IntValue FE_PER_POINT = BUILDER
            .comment("每 1 个技能点所需的 FE 电量")
            .defineInRange("fePerPoint", 100_000, 1, Integer.MAX_VALUE);

    /** 技能核心每 tick 基础能量转换吞吐 */
    public static final ModConfigSpec.IntValue BASE_INTAKE_PER_TICK = BUILDER
            .comment("技能核心每 tick 基础能量转换量 (FE/t)")
            .defineInRange("baseIntakePerTick", 10_000, 1, Integer.MAX_VALUE);

    /** 技能核心基础 FE 缓冲容量 */
    public static final ModConfigSpec.IntValue BASE_CAPACITY = BUILDER
            .comment("技能核心基础 FE 缓冲容量")
            .defineInRange("baseCapacity", 1_000_000, 1, Integer.MAX_VALUE);

    /** 技能影响范围（以玩家为中心的半径） */
    public static final ModConfigSpec.IntValue BASE_RANGE = BUILDER
            .comment("技能影响范围基础半径（格）")
            .defineInRange("baseRange", 3, 1, 8);

    /** 转化效率下限（百分比） */
    public static final ModConfigSpec.IntValue EFFICIENCY_MIN_PERCENT = BUILDER
            .comment("转化效率技能的下限（百分比，50 = 每点仅需一半电量）")
            .defineInRange("efficiencyMinPercent", 50, 10, 100);

    /** 全域加速技能最多同时加速的机器数量（每类） */
    public static final ModConfigSpec.IntValue GLOBAL_MAX_MACHINES = BUILDER
            .comment("全域加速技能每类最多同时加速的机器数量")
            .defineInRange("globalMaxMachines", 512, 1, 65536);

    /** 全域加速的机器列表刷新间隔（tick） */
    public static final ModConfigSpec.IntValue GLOBAL_RESCAN_INTERVAL = BUILDER
            .comment("全域加速技能的机器列表刷新间隔（tick）")
            .defineInRange("globalRescanInterval", 100, 20, 6000);

    /** 全域加速每刻额外工作的最大次数（技能叠加后上限） */
    public static final ModConfigSpec.IntValue GLOBAL_MAX_EXTRA = BUILDER
            .comment("全域加速每刻额外工作的最大次数（技能等级叠加后的上限）")
            .defineInRange("globalMaxExtra", 10, 1, 64);

    public static final ModConfigSpec SPEC = BUILDER.build();
}