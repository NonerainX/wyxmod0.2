package com.example.wyxjinengshu.ui;

import com.example.wyxjinengshu.network.AddSkillPayload;
import com.example.wyxjinengshu.network.ClientPayloadHandler;
import com.example.wyxjinengshu.skills.Skill;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 技能书界面。
 * 独立 Screen（不依附容器界面），因此不会被 JEI 的物品面板覆盖。
 */
public class SkillScreen extends Screen {

    private static final int PANEL_X = 8;
    private static final int PANEL_Y = 28;
    private static final int BOX_WIDTH = 260;
    private static final int BOX_HEIGHT = 26;
    private static final int BOX_GAP = 3;
    private static final int CATEGORY_GAP = 12;

    private record Box(Skill skill, int x, int y) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + BOX_WIDTH && mouseY >= y && mouseY <= y + BOX_HEIGHT;
        }
    }

    private final List<Box> boxes = new ArrayList<>();

    public SkillScreen() {
        super(Component.translatable("screen.wyxjinengshu.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xE0101010);
        graphics.fillGradient(0, 0, this.width, this.height, 0xE0203048, 0xE0080A12);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        layout();

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFD700);

        int points = ClientPayloadHandler.CACHE.points();
        graphics.drawString(this.font,
                Component.translatable("screen.wyxjinengshu.points", points), PANEL_X + 4, PANEL_Y - 12, 0xFFAAAAFF);

        int y = PANEL_Y;
        for (Skill.Category category : Skill.Category.values()) {
            List<Skill> skills = new ArrayList<>();
            for (Skill s : Skill.ALL) {
                if (s.getCategory() == category) skills.add(s);
            }
            if (skills.isEmpty()) continue;

            graphics.drawString(this.font,
                    Component.translatable("category.wyxjinengshu." + category.name().toLowerCase()),
                    PANEL_X + 2, y, 0xFF9FD6FF);
            y += 14;

            for (Skill skill : skills) {
                drawSkillBox(graphics, skill, PANEL_X, y);
                y += BOX_HEIGHT + BOX_GAP;
            }
            y += CATEGORY_GAP;
        }

        graphics.drawString(this.font,
                Component.translatable("screen.wyxjinengshu.hint"),
                PANEL_X + 2, this.height - 14, 0xFF888888);

        super.render(graphics, mouseX, mouseY, partialTick);
        renderHoverTooltip(graphics, mouseX, mouseY);
    }

    private void drawSkillBox(GuiGraphics graphics, Skill skill, int x, int y) {
        boolean available = skill.getCategory().isAvailable();
        int level = ClientPayloadHandler.CACHE.level(skill);
        boolean maxed = level >= skill.getMaxLevel();

        graphics.fill(x, y, x + BOX_WIDTH, y + BOX_HEIGHT, available ? 0xFF232A33 : 0xFF1A1A1A);
        graphics.fill(x + 1, y + 1, x + BOX_WIDTH - 1, y + BOX_HEIGHT - 1, available ? 0xFF2E3640 : 0xFF202020);
        // 底边框：满级金色
        graphics.fill(x + 1, y + BOX_HEIGHT - 2, x + BOX_WIDTH - 1, y + BOX_HEIGHT - 1,
                maxed ? 0xFFFFD700 : (available ? 0xFF3D82B8 : 0xFF444444));

        // 图标
        Optional.ofNullable(BuiltInRegistries.ITEM.get(skill.getIcon()))
                .map(ItemStack::new)
                .ifPresent(stack -> {
                    graphics.renderItem(stack, x + 3, y + BOX_HEIGHT / 2 - 8);
                    graphics.renderItemDecorations(this.font, stack, x + 3, y + BOX_HEIGHT / 2 - 8);
                });

        graphics.drawString(this.font, Component.translatable(skill.getNameKey()), x + 24, y + 7,
                available ? 0xFFFFFFFF : 0xFF777777);

        String levelText = "Lv." + level + "/" + skill.getMaxLevel();
        graphics.drawString(this.font, levelText, x + BOX_WIDTH - 118, y + 9,
                maxed ? 0xFFFFD700 : 0xFFB0B0B0);

        // 右侧按钮区
        int btnX = x + BOX_WIDTH - 58;
        if (!available) {
            graphics.fill(btnX, y + 3, btnX + 52, y + BOX_HEIGHT - 3, 0xFF3A3A3A);
            graphics.drawCenteredString(this.font, Component.translatable("screen.wyxjinengshu.unavailable_short"),
                    btnX + 26, y + 8, 0xFF888888);
        } else if (maxed) {
            graphics.fill(btnX, y + 3, btnX + 52, y + BOX_HEIGHT - 3, 0xFF574B1E);
            graphics.drawCenteredString(this.font, Component.translatable("screen.wyxjinengshu.maxed"),
                    btnX + 26, y + 8, 0xFFFFD700);
        } else {
            int cost = skill.costForNextLevel(level);
            graphics.fill(btnX, y + 3, btnX + 52, y + BOX_HEIGHT - 3,
                    ClientPayloadHandler.CACHE.points() >= cost ? 0xFF2E6B30 : 0xFF4A3232);
            graphics.drawCenteredString(this.font, Component.translatable("screen.wyxjinengshu.cost", cost),
                    btnX + 26, y + 8, 0xFFFFFFFF);
        }
    }

    private void renderHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        Box hovered = hoveredBox(mouseX, mouseY);
        if (hovered == null) return;
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable(hovered.skill().getNameKey()));
        tooltip.add(Component.translatable(hovered.skill().getDescKey()).withStyle(style -> style.withColor(0xAAAAAA)));
        if (!hovered.skill().getCategory().isAvailable()) {
            tooltip.add(Component.translatable("screen.wyxjinengshu.unavailable").withStyle(style -> style.withColor(0xFF5555)));
        } else {
            int level = ClientPayloadHandler.CACHE.level(hovered.skill());
            if (level >= hovered.skill().getMaxLevel()) {
                tooltip.add(Component.translatable("screen.wyxjinengshu.maxed").withStyle(style -> style.withColor(0xFFD700)));
            } else {
                tooltip.add(Component.translatable("screen.wyxjinengshu.cost",
                        hovered.skill().costForNextLevel(level)));
            }
        }
        graphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    private void layout() {
        boxes.clear();
        int y = PANEL_Y;
        for (Skill.Category category : Skill.Category.values()) {
            boolean hasSkills = false;
            for (Skill s : Skill.ALL) {
                if (s.getCategory() == category) {
                    if (!hasSkills) {
                        y += 14; // 分类标题行
                        hasSkills = true;
                    }
                    boxes.add(new Box(s, PANEL_X, y));
                    y += BOX_HEIGHT + BOX_GAP;
                }
            }
            if (hasSkills) {
                y += CATEGORY_GAP;
            }
        }
    }

    @Nullable
    private Box hoveredBox(int mouseX, int mouseY) {
        for (Box box : boxes) {
            if (box.contains(mouseX, mouseY)) {
                return box;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            layout();
            Box box = hoveredBox((int) mouseX, (int) mouseY);
            if (box != null) {
                if (box.skill().getCategory().isAvailable()
                        && ClientPayloadHandler.CACHE.level(box.skill()) < box.skill().getMaxLevel()) {
                    PacketDistributor.sendToServer(new AddSkillPayload(box.skill().getId()));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
