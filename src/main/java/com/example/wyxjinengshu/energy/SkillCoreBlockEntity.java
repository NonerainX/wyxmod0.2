package com.example.wyxjinengshu.energy;

import com.example.wyxjinengshu.WyxJinengshu;
import com.example.wyxjinengshu.skills.SkillManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.UUID;

/**
 * 技能核心方块实体：实现 IEnergyStorage，吸收 FE 并转换为所有者技能点。
 */
public class SkillCoreBlockEntity extends BlockEntity implements IEnergyStorage {

    private UUID owner;
    private int energy;

    public SkillCoreBlockEntity(BlockPos pos, BlockState state) {
        super(WyxJinengshu.SKILL_CORE_BE.get(), pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public UUID getOwner() {
        return owner;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (level.isClientSide || !(blockEntity instanceof SkillCoreBlockEntity core)) return;
        core.serverTick();
    }

    private void serverTick() {
        if (owner == null || level.getServer() == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null) return; // 所有者不在线时暂不转换

        int fePerPoint = SkillManager.getFEPerPoint(player);
        int intake = SkillManager.getIntakePerTick(player);
        int convertible = Math.min(energy, intake);
        int points = convertible / fePerPoint;
        if (points > 0) {
            energy -= points * fePerPoint;
            SkillManager.addPoints(player, points);
            setChanged();
        }
    }

    // ==== IEnergyStorage ====

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int capacity = getMaxEnergyStored();
        int canReceive = Math.min(maxReceive, capacity - energy);
        if (!simulate) {
            energy += canReceive;
            setChanged();
        }
        return canReceive;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0; // 只能进不能出
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        // 在线则按所有者技能加成计算容量
        if (owner != null && level != null && level.getServer() != null) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
            if (player != null) return SkillManager.getBufferMax(player);
        }
        return com.example.wyxjinengshu.config.ModConfig.BASE_CAPACITY.get();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    // ==== 存档 ====

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        if (tag.hasUUID("owner")) {
            owner = tag.getUUID("owner");
        }
    }
}