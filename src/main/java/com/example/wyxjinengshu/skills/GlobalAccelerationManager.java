package com.example.wyxjinengshu.skills;

import com.example.wyxjinengshu.WyxJinengshu;
import com.example.wyxjinengshu.config.ModConfig;
import com.example.wyxjinengshu.integration.Ae2Accelerator;
import com.example.wyxjinengshu.integration.MekAccelerator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全域加速技能：加点后永久生效，作用范围不受玩家位置限制，
 * 覆盖所有已加载区块内的 AE2（含附属模组）与通用机械机器。
 * 机器列表按 {@link ModConfig#GLOBAL_RESCAN_INTERVAL} 定期刷新并缓存。
 */
public final class GlobalAccelerationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("wyxjinengshu/GlobalAcceleration");

    /** 延迟加载加速器，未安装联动模组时不触发类加载 */
    private static final class Accelerators {
        static final MekAccelerator MEK = ModList.get().isLoaded("mekanism") ? new MekAccelerator() : null;
        static final Ae2Accelerator AE2 = ModList.get().isLoaded("ae2") ? new Ae2Accelerator() : null;
    }

    private static final class Cache {
        long scannedTick = Long.MIN_VALUE;
        final List<BlockPos> ae2 = new ArrayList<>();
        final List<BlockPos> mek = new ArrayList<>();
    }

    private static final Map<ResourceKey<Level>, Cache> CACHES = new HashMap<>();

    private static Method getChunksMethod;
    private static boolean chunksScanDisabled;

    private GlobalAccelerationManager() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        int maxExtra = ModConfig.GLOBAL_MAX_EXTRA.get();
        int overclock = Math.min(maxExtra, maxLevel(server, "global_overclock"));
        int ae2Extra = Math.min(maxExtra, maxLevel(server, "ae2_global") + overclock);
        int mekExtra = Math.min(maxExtra, maxLevel(server, "mek_global") + overclock);
        if (ae2Extra <= 0 && mekExtra <= 0) return;

        long tick = server.getTickCount();
        for (ServerLevel level : server.getAllLevels()) {
            Cache cache = CACHES.computeIfAbsent(level.dimension(), k -> new Cache());
            if (tick - cache.scannedTick >= ModConfig.GLOBAL_RESCAN_INTERVAL.get()) {
                rescan(level, cache, tick);
            }
            if (ae2Extra > 0 && Accelerators.AE2 != null) {
                Accelerators.AE2.apply(level, cache.ae2, ae2Extra);
            }
            if (mekExtra > 0 && Accelerators.MEK != null) {
                Accelerators.MEK.apply(level, cache.mek, mekExtra);
            }
        }

        if (CACHES.size() > 8 && tick % 6000 == 0) {
            CACHES.keySet().removeIf(dim -> server.getLevel(dim) == null);
        }
    }

    /** 取所有在线玩家中该技能的最高等级（任一玩家加点即全局生效） */
    private static int maxLevel(MinecraftServer server, String skillId) {
        Skill skill = Skill.byId(skillId);
        if (skill == null) return 0;
        int max = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            int lv = player.getData(WyxJinengshu.SKILL_DATA).getLevel(skill);
            if (lv > max) max = lv;
        }
        return max;
    }

    private static void rescan(ServerLevel level, Cache cache, long tick) {
        cache.ae2.clear();
        cache.mek.clear();
        int cap = ModConfig.GLOBAL_MAX_MACHINES.get();
        try {
            for (ChunkHolder holder : chunkHolders(level)) {
                LevelChunk chunk = holder.getTickingChunk();
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (Accelerators.AE2 != null && cache.ae2.size() < cap && Accelerators.AE2.isMachine(be)) {
                        cache.ae2.add(be.getBlockPos());
                    } else if (Accelerators.MEK != null && cache.mek.size() < cap && Accelerators.MEK.isMachine(be)) {
                        cache.mek.add(be.getBlockPos());
                    }
                }
            }
            cache.scannedTick = tick;
        } catch (Throwable t) {
            LOGGER.warn("扫描已加载区块失败，全域加速暂缓一帧: {}", t.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private static Iterable<ChunkHolder> chunkHolders(ServerLevel level) {
        Method method = chunksMethod();
        if (method == null) return List.of();
        ChunkSource source = level.getChunkSource();
        if (!(source instanceof ServerChunkCache serverCache)) return List.of();
        try {
            return (Iterable<ChunkHolder>) method.invoke(serverCache.chunkMap);
        } catch (Throwable t) {
            LOGGER.warn("无法遍历已加载区块，全域加速已停用: {}", t.toString());
            chunksScanDisabled = true;
            getChunksMethod = null;
            return List.of();
        }
    }

    /** ChunkMap#getChunks 为 protected，这里反射获取（仅在已安装联动模组时使用） */
    private static Method chunksMethod() {
        if (getChunksMethod == null && !chunksScanDisabled) {
            try {
                Method method = ChunkMap.class.getDeclaredMethod("getChunks");
                method.setAccessible(true);
                getChunksMethod = method;
            } catch (Throwable t) {
                LOGGER.warn("未找到 ChunkMap#getChunks，全域加速已停用: {}", t.toString());
                chunksScanDisabled = true;
            }
        }
        return getChunksMethod;
    }
}
