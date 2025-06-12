package net.thedragonskull.trapsmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.trapsmod.TrapsMod;
import net.thedragonskull.trapsmod.block.custom.*;
import net.thedragonskull.trapsmod.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TrapsMod.MOD_ID);

    public static final RegistryObject<Block> PUNJI_STICKS_PLANK = registerBlock("punji_sticks_plank",
            () -> new PunjiSticksPlank(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .strength(2.0F, 3.0F).sound(SoundType.BAMBOO_WOOD).noOcclusion().ignitedByLava()));

    public static final RegistryObject<Block> STRONG_CHAIN = registerBlock("strong_chain",
            () -> new StrongChainBlock(BlockBehaviour.Properties.copy(Blocks.CHAIN)));

    public static final RegistryObject<Block> CAGE_TRAP_TICKER = registerBlock("cage_trap_ticker",
            () -> new CageTrapTickerBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER).noCollission().noOcclusion().noLootTable()));

    public static final RegistryObject<Block> SHARPENED_BAMBOO = registerBlock("sharpened_bamboo",
            () -> new SharpenedBamboo(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).forceSolidOn().instabreak()
                    .strength(1.0F).sound(SoundType.BAMBOO).noOcclusion().dynamicShape().offsetType(BlockBehaviour.OffsetType.XZ)
                    .ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor(ModBlocks::never)));

    public static final RegistryObject<Block> CREAKING_FLOOR = registerBlock("creaking_floor",
            () -> new CreakingFloorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).ignitedByLava().noOcclusion()));

    public static final RegistryObject<Block> BEAR_TRAP = registerBlock("bear_trap",
            () -> new BearTrap(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN)
                    .requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.ANVIL).noOcclusion()));//todo destroy time alto


    private static boolean never(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_) {
        return false;
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
