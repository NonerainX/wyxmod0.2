package com.example.wyxjinengshu.skills;

import com.example.wyxjinengshu.WyxJinengshu;
import com.example.wyxjinengshu.config.ModConfig;
import com.example.wyxjinengshu.network.SyncSkillDataPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 技能点/技能等级的读写与加点逻辑。
 */
public final class SkillManager {
    private SkillManager() {
    }

    public static SkillData data(Player player) {
        return player.getData(WyxJinengshu.SKILL_DATA);
    }

    public static int getPoints(Player player) {
        return data(player).getPoints();
    }

    public static void addPoints(Player player, int amount) {
        data(player).addPoints(amount);
    }

    public static int getLevel(Player player, Skill skill) {
        return data(player).getLevel(skill);
    }

    /**
     * 尝试对指定技能升一级。消耗点数为当前等级 + 1（递增）。
     * 未安装联动模组时对应类别的技能不可加点。
     */
    public static boolean upgrade(ServerPlayer player, String skillId) {
        Skill skill = Skill.byId(skillId);
        if (skill == null) return false;
        if (!skill.getCategory().isAvailable()) return false;

        SkillData data = data(player);
        int current = data.getLevel(skill);
        if (current >= skill.getMaxLevel()) return false;

        int cost = skill.costForNextLevel(current);
        if (data.getPoints() < cost) return false;

        data.addPoints(-cost);
        data.setLevel(skill, current + 1);
        syncToClient(player);
        return true;
    }

    /** 读取玩家某技能的等级（技能 id 不存在时返回 0） */
    public static int getLevel(Player player, String skillId) {
        Skill skill = Skill.byId(skillId);
        return skill == null ? 0 : getLevel(player, skill);
    }

    /** 该玩家当前 1 个技能点所需的 FE 电量（受“转化效率”技能影响） */
    public static int getFEPerPoint(Player player) {
        int effLevel = getLevel(player, "general_efficiency");
        int percent = Math.max(ModConfig.EFFICIENCY_MIN_PERCENT.get(), 100 - 5 * effLevel);
        long fe = (long) ModConfig.FE_PER_POINT.get() * percent / 100;
        return (int) Math.max(1, Math.min(fe, Integer.MAX_VALUE));
    }

    /** 技能核心每 tick 能量转换吞吐（受“能量功率”技能影响） */
    public static int getIntakePerTick(Player player) {
        int lv = getLevel(player, "general_power");
        long base = ModConfig.BASE_INTAKE_PER_TICK.get();
        return (int) Math.min(Integer.MAX_VALUE, base * (100 + 50L * lv) / 100);
    }

    /** 技能核心缓冲容量（受“能量储量”技能影响） */
    public static int getBufferMax(Player player) {
        int lv = getLevel(player, "general_capacity");
        long base = ModConfig.BASE_CAPACITY.get();
        return (int) Math.min(Integer.MAX_VALUE, base * (100 + 50L * lv) / 100);
    }

    /** 技能影响范围半径（受“影响范围”技能影响） */
    public static int getRange(Player player) {
        return ModConfig.BASE_RANGE.get() + getLevel(player, "general_range");
    }

    /** 将玩家技能数据同步到客户端 */
    public static void syncToClient(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, SyncSkillDataPayload.of(data(player)));
    }
}