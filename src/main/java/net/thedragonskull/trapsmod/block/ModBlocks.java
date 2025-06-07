package net.thedragonskull.trapsmod.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.trapsmod.TrapsMod;
import net.thedragonskull.trapsmod.block.custom.BellTrapChainBlock;
import net.thedragonskull.trapsmod.block.custom.PunjiSticksPlank;
import net.thedragonskull.trapsmod.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TrapsMod.MOD_ID);

    public static final RegistryObject<Block> PUNJI_STICKS_PLANK = registerBlock("punji_sticks_plank",
            () -> new PunjiSticksPlank(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .strength(2.0F, 3.0F).sound(SoundType.BAMBOO_WOOD).noOcclusion().ignitedByLava()));

    public static final RegistryObject<Block> BELL_TRAP_CHAIN = registerBlock("bell_trap_chain",
            () -> new BellTrapChainBlock(BlockBehaviour.Properties.copy(Blocks.CHAIN)));


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
