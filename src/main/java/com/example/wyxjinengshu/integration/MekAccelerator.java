package com.example.wyxjinengshu.integration;

import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Mekanism 机器加速器（软依赖：无 mekanism 时本类不会被加载）。
 * 通过额外调用机器的 tickServer 实现“每刻多工作 N 次”。
 */
public class MekAccelerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("wyxjinengshu/MekAccelerator");

    private Method tickServerMethod;
    private boolean disabled;

    /** 任意通用机械机器（含其附属模组，只要继承 TileEntityMekanism） */
    public boolean isMachine(BlockEntity be) {
        return be instanceof TileEntityMekanism;
    }

    public void apply(ServerLevel level, List<BlockPos> positions, int extraTicks) {
        if (disabled || extraTicks <= 0 || positions.isEmpty()) return;
        Method tick = tickServer();
        if (tick == null) return;
        for (BlockPos pos : positions) {
            if (!(level.getBlockEntity(pos) instanceof TileEntityMekanism machine)) continue;
            try {
                for (int i = 0; i < extraTicks; i++) {
                    tick.invoke(null, level, pos, level.getBlockState(pos), machine);
                }
            } catch (Throwable t) {
                LOGGER.warn("Mekanism 加速调用失败，已停用: {}", t.toString());
                disabled = true;
                return;
            }
        }
    }

    /** 查找 TileEntityMekanism 的公开静态 tickServer(Level, BlockPos, BlockState, TileEntityMekanism) */
    private Method tickServer() {
        if (tickServerMethod == null && !disabled) {
            for (Method m : TileEntityMekanism.class.getMethods()) {
                if (m.getName().equals("tickServer") && Modifier.isStatic(m.getModifiers())
                        && m.getParameterCount() == 4) {
                    tickServerMethod = m;
                    break;
                }
            }
            if (tickServerMethod == null) {
                LOGGER.warn("未找到 TileEntityMekanism.tickServer 方法，加速已停用");
                disabled = true;
            }
        }
        return tickServerMethod;
    }
}