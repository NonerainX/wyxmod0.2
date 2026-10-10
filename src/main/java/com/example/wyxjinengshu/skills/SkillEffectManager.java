package com.example.wyxjinengshu.skills;

import com.example.wyxjinengshu.WyxJinengshu;
import com.example.wyxjinengshu.integration.Ae2Accelerator;
import com.example.wyxjinengshu.integration.MekAccelerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 范围型技能效果：每刻对玩家附近的 AE2 / Mekanism 机器施加加速。
 * 无范围限制的全域加速见 {@link GlobalAccelerationManager}。
 * 通过主类 NeoForge.EVENT_BUS.register(SkillEffectManager.class) 注册（双端可用）。
 */
public final class SkillEffectManager {

    /** 延迟加载加速器，避免未安装联动模组时触发类加载 */
    private static final class Accelerators {
        static final MekAccelerator MEK = ModList.get().isLoaded("mekanism") ? new MekAccelerator() : null;
        static final Ae2Accelerator AE2 = ModList.get().isLoaded("ae2") ? new Ae2Accelerator() : null;
    }

    private static final class Cache {
        BlockPos center = BlockPos.ZERO;
        long scannedTick = Long.MIN_VALUE;
        final List<BlockPos> mek = new ArrayList<>();
        final List<BlockPos> inscribers = new ArrayList<>();
        final List<BlockPos> assemblers = new ArrayList<>();
    }

    private static final Map<UUID, Cache> CACHES = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long tick = server.getTickCount();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            SkillData data = player.getData(WyxJinengshu.SKILL_DATA);
            int mekLevel = level(data, "mek_overdrive");
            int assemblerLevel = level(data, "ae2_assembler");
            int inscriberLevel = level(data, "ae2_inscriber");
            if (mekLevel == 0 && assemblerLevel == 0 && inscriberLevel == 0) continue;

            int range = SkillManager.getRange(player);
            Cache cache = CACHES.computeIfAbsent(player.getUUID(), u -> new Cache());
            if (tick - cache.scannedTick >= 10 || cache.center.distSqr(player.blockPosition()) > 16) {
                rescan(cache, player.serverLevel(), player.blockPosition(), range, tick);
            }

            if (Accelerators.MEK != null && mekLevel > 0) {
                Accelerators.MEK.apply(player.serverLevel(), cache.mek, mekLevel);
            }
            if (Accelerators.AE2 != null) {
                if (inscriberLevel > 0) {
                    Accelerators.AE2.apply(player.serverLevel(), cache.inscribers, inscriberLevel);
                }
                if (assemblerLevel > 0) {
                    Accelerators.AE2.apply(player.serverLevel(), cache.assemblers, assemblerLevel);
                }
            }
        }

        if (CACHES.size() > 64 && tick % 600 == 0) {
            CACHES.entrySet().removeIf(e -> server.getPlayerList().getPlayer(e.getKey()) == null);
        }
    }

    private static int level(SkillData data, String skillId) {
        Skill skill = Skill.byId(skillId);
        return skill == null ? 0 : data.getLevel(skill);
    }

    private static void rescan(Cache cache, ServerLevel level, BlockPos center, int range, long tick) {
        cache.center = center;
        cache.mek.clear();
        cache.inscribers.clear();
        cache.assemblers.clear();
        BlockPos.betweenClosed(center.offset(-range, -range, -range), center.offset(range, range, range))
                .forEach(pos -> {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be == null) return;
                    if (Accelerators.MEK != null && Accelerators.MEK.isMachine(be)) {
                        cache.mek.add(be.getBlockPos());
                    }
                    if (Accelerators.AE2 != null) {
                        if (Accelerators.AE2.isInscriber(be)) {
                            cache.inscribers.add(be.getBlockPos());
                        } else if (Accelerators.AE2.isAssembler(be)) {
                            cache.assemblers.add(be.getBlockPos());
                        }
                    }
                });
        cache.scannedTick = tick;
    }
}
