package com.example.wyxjinengshu.integration;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.me.helpers.IGridConnectedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * AE2 加速器（软依赖：无 ae2 时本类不会被加载）。
 * 通过 AE2 网格计时接口 {@link IGridTickable#tickingRequest} 推进机器工作进度，
 * 因此对 AE2 本体全部机器以及实现了该接口的附属模组机器均有效。
 */
public class Ae2Accelerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("wyxjinengshu/Ae2Accelerator");

    private boolean disabled;

    /** 任意由 AE 网格驱动的机器（含附属模组） */
    public boolean isMachine(BlockEntity be) {
        return be instanceof IGridTickable;
    }

    public boolean isInscriber(BlockEntity be) {
        return be instanceof InscriberBlockEntity;
    }

    public boolean isAssembler(BlockEntity be) {
        return be instanceof MolecularAssemblerBlockEntity;
    }

    /**
     * 对给定位置上的 AE2 机器额外推进 {@code extraTicks} 次工作。
     */
    public void apply(ServerLevel level, List<BlockPos> positions, int extraTicks) {
        if (disabled || extraTicks <= 0 || positions.isEmpty()) return;
        for (BlockPos pos : positions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof IGridTickable tickable) || !(be instanceof IGridConnectedBlockEntity host)) continue;
            IGridNode node = host.getGridNode();
            if (node == null || !node.isActive()) continue;
            try {
                for (int i = 0; i < extraTicks; i++) {
                    tickable.tickingRequest(node, 1);
                }
            } catch (Throwable t) {
                LOGGER.warn("AE2 机器加速失败，已停用: {}", t.toString());
                disabled = true;
                return;
            }
        }
    }
}
