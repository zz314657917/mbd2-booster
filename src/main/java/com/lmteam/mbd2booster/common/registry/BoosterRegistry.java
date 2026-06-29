package com.lmteam.mbd2booster.common.registry;

import com.lmteam.mbd2booster.MBD2Booster;
import com.lmteam.mbd2booster.common.block.BoosterBaseBlock;
import com.lmteam.mbd2booster.common.blockentity.BoosterBaseBlockEntity;
import com.lmteam.mbd2booster.common.capability.BoosterCapabilities;
import com.lmteam.mbd2booster.common.item.BindingToolItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BoosterRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MBD2Booster.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MBD2Booster.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MBD2Booster.MOD_ID);

    public static final RegistryObject<Block> BOOSTER_BASE = BLOCKS.register("booster_base",
            () -> new BoosterBaseBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f, 6.0f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> BOOSTER_BASE_ITEM = ITEMS.register("booster_base",
            () -> new BlockItem(BOOSTER_BASE.get(), new Item.Properties()));
    public static final RegistryObject<Item> BINDING_TOOL = ITEMS.register("binding_tool",
            () -> new BindingToolItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockEntityType<BoosterBaseBlockEntity>> BOOSTER_BASE_ENTITY = BLOCK_ENTITIES.register("booster_base",
            () -> BlockEntityType.Builder.of(BoosterBaseBlockEntity::new, BOOSTER_BASE.get()).build(null));

    private BoosterRegistry() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

    public static void registerCapabilities(net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent event) {
        BoosterCapabilities.register(event);
    }

    public static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(BOOSTER_BASE_ITEM);
            event.accept(BINDING_TOOL);
        }
    }
}
