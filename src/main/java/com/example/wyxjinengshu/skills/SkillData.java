package com.example.wyxjinengshu.skills;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 玩家技能存档数据：技能点 + 各技能等级。
 */
public class SkillData implements INBTSerializable<CompoundTag> {
    private int points;
    private final int[] levels = new int[Skill.ALL.size()];

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = Math.max(0, points);
    }

    public void addPoints(int amount) {
        this.points = Math.max(0, this.points + amount);
    }

    public int getLevel(Skill skill) {
        int i = Skill.indexOf(skill);
        return i >= 0 ? levels[i] : 0;
    }

    public void setLevel(Skill skill, int level) {
        int i = Skill.indexOf(skill);
        if (i < 0) return;
        levels[i] = Math.max(0, Math.min(skill.getMaxLevel(), level));
    }

    public int[] getAllLevels() {
        return levels.clone();
    }

    public void setAllLevels(int[] newLevels) {
        for (int i = 0; i < levels.length; i++) {
            levels[i] = 0;
            if (i < newLevels.length) {
                levels[i] = Math.max(0, Math.min(Skill.ALL.get(i).getMaxLevel(), newLevels[i]));
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registryLookup) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("points", points);
        tag.putIntArray("levels", levels);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registryLookup, CompoundTag tag) {
        this.points = Math.max(0, tag.getInt("points"));
        int[] arr = tag.getIntArray("levels");
        setAllLevels(arr);
    }
}