package com.example.wyxjinengshu;

import com.example.wyxjinengshu.config.ModConfig;
import com.example.wyxjinengshu.energy.SkillCoreBlock;
import com.example.wyxjinengshu.energy.SkillCoreBlockEntity;
import com.example.wyxjinengshu.network.ModNetworking;
import com.example.wyxjinengshu.skills.GlobalAccelerationManager;
import com.example.wyxjinengshu.skills.SkillData;
import com.example.wyxjinengshu.skills.SkillEffectManager;
import com.example.wyxjinengshu.command.ModCommands;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(WyxJinengshu.MODID)
public class WyxJinengshu {
    public static final String MODID = "wyxjinengshu";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    // 技能核心方块：接入 FE 能量，按固定比例转换为玩家的技能点
    public static final DeferredBlock<Block> SKILL_CORE_BLOCK = BLOCKS.register("skill_core",
            () -> new SkillCoreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(3.0F, 6.0F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)));

    public static final DeferredItem<BlockItem> SKILL_CORE_ITEM = ITEMS.registerSimpleBlockItem(SKILL_CORE_BLOCK);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SkillCoreBlockEntity>> SKILL_CORE_BE =
            BLOCK_ENTITIES.register("skill_core",
                    () -> BlockEntityType.Builder.of(SkillCoreBlockEntity::new, SKILL_CORE_BLOCK.get()).build(null));

    // 玩家技能存档（技能点 + 各技能等级）
    public static final Supplier<AttachmentType<SkillData>> SKILL_DATA =
            ATTACHMENTS.register("skill_data", () -> AttachmentType.serializable(SkillData::new).copyOnDeath().build());

    public WyxJinengshu(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ATTACHMENTS.register(modEventBus);

        modEventBus.addListener(ModNetworking::register);
        modEventBus.addListener(this::addCreative);

        // 游戏事件（双端）：范围型技能效果 + 全域加速技能 + /wyx 命令
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(SkillEffectManager.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(GlobalAccelerationManager.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ModCommands::register);

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);

        LOGGER.info("[wyxjinengshu] 技能书模组加载中... AE2: {} / Mekanism: {}",
                net.neoforged.fml.ModList.get().isLoaded("ae2"),
                net.neoforged.fml.ModList.get().isLoaded("mekanism"));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(SKILL_CORE_ITEM);
        }
    }
}