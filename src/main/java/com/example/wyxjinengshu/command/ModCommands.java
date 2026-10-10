package com.example.wyxjinengshu.command;

import com.example.wyxjinengshu.WyxJinengshu;
import com.example.wyxjinengshu.skills.Skill;
import com.example.wyxjinengshu.skills.SkillData;
import com.example.wyxjinengshu.skills.SkillManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * /wyx 调试命令：addpoint / points / setskill / max。
 * 通过主类 NeoForge.EVENT_BUS.addListener(ModCommands::register) 注册（双端可用）。
 */
public class ModCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("wyx")
                .then(Commands.literal("addpoint")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    SkillManager.addPoints(player, amount);
                                    SkillManager.syncToClient(player);
                                    ctx.getSource().sendSuccess(() -> Component.translatable(
                                                    "command.wyxjinengshu.addpoint", amount, SkillManager.getPoints(player)),
                                            false);
                                    return amount;
                                })))
                .then(Commands.literal("points")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            int points = SkillManager.getPoints(player);
                            ctx.getSource().sendSuccess(() -> Component.translatable(
                                            "command.wyxjinengshu.points", points),
                                    false);
                            return points;
                        }))
                .then(Commands.literal("setskill")
                        .then(Commands.argument("skill", StringArgumentType.word())
                                .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            String id = StringArgumentType.getString(ctx, "skill");
                                            int level = IntegerArgumentType.getInteger(ctx, "level");
                                            Skill skill = Skill.byId(id);
                                            if (skill == null) {
                                                ctx.getSource().sendFailure(Component.literal("未知技能: " + id));
                                                return 0;
                                            }
                                            player.getData(WyxJinengshu.SKILL_DATA).setLevel(skill, level);
                                            SkillManager.syncToClient(player);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "已设置技能 " + id + " 等级为 " + level),
                                                    true);
                                            return level;
                                        }))))
                .then(Commands.literal("max")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            SkillData data = player.getData(WyxJinengshu.SKILL_DATA);
                            data.setPoints(999999999);
                            for (Skill skill : Skill.ALL) {
                                if (skill.getCategory().isAvailable()) {
                                    data.setLevel(skill, skill.getMaxLevel());
                                }
                            }
                            SkillManager.syncToClient(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("已将所有可用技能升至满级"), true);
                            return 1;
                        })));
    }
}