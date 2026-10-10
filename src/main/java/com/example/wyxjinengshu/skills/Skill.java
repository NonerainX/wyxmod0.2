package com.example.wyxjinengshu.skills;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能定义。效果为被动加成，具体效果在 SkillEffectManager 中实现。
 */
public final class Skill {
    /** 技能类别：是否要求安装对应联动模组 */
    public enum Category {
        GENERAL(null),
        AE2("ae2"),
        MEK("mekanism");

        private final String requiredModId;

        Category(@Nullable String requiredModId) {
            this.requiredModId = requiredModId;
        }

        /** 该类别当前是否可用（未安装联动模组时不可用） */
        public boolean isAvailable() {
            return requiredModId == null || ModList.get().isLoaded(requiredModId);
        }

        public String getRequiredModId() {
            return requiredModId;
        }
    }

    private final String id;
    private final Category category;
    private final int maxLevel;
    private final ResourceLocation icon;

    public Skill(String id, Category category, int maxLevel, String iconItem) {
        this.id = id;
        this.category = category;
        this.maxLevel = maxLevel;
        this.icon = ResourceLocation.parse(iconItem);
    }

    public String getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public String getNameKey() {
        return "skill.wyxjinengshu." + id;
    }

    public String getDescKey() {
        return "skill.wyxjinengshu." + id + ".desc";
    }

    /** 升到下一级（目标等级 N）所需的技能点：N（随等级递增） */
    public int costForNextLevel(int currentLevel) {
        return currentLevel + 1;
    }

    /** 全部技能列表（顺序即存档中的索引顺序） */
    public static final List<Skill> ALL = new ArrayList<>(List.of(
            new Skill("general_capacity", Category.GENERAL, 5, "minecraft:barrel"),
            new Skill("general_power", Category.GENERAL, 5, "minecraft:lightning_rod"),
            new Skill("general_efficiency", Category.GENERAL, 10, "minecraft:gold_ingot"),
            new Skill("general_range", Category.GENERAL, 5, "minecraft:compass"),
            new Skill("ae2_assembler", Category.AE2, 5, "minecraft:crafting_table"),
            new Skill("ae2_inscriber", Category.AE2, 5, "minecraft:smithing_table"),
            new Skill("mek_overdrive", Category.MEK, 5, "minecraft:blast_furnace"),
            // 以下为新增技能，必须追加在列表末尾以保持旧存档的技能等级索引不变
            new Skill("ae2_global", Category.AE2, 5, "minecraft:beacon"),
            new Skill("mek_global", Category.MEK, 5, "minecraft:conduit"),
            new Skill("global_overclock", Category.GENERAL, 5, "minecraft:nether_star")
    ));

    @Nullable
    public static Skill byId(String id) {
        for (Skill s : ALL) {
            if (s.id.equals(id)) return s;
        }
        return null;
    }

    public static int indexOf(Skill skill) {
        return ALL.indexOf(skill);
    }
}