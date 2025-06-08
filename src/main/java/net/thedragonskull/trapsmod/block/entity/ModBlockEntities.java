package net.thedragonskull.trapsmod.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.trapsmod.TrapsMod;
import net.thedragonskull.trapsmod.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TrapsMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<PunjiSticksPlankBE>> PUNJI_STICKS_PLANK_BE =
            BLOCK_ENTITIES.register("punji_sticks_plank_be", () ->
                    BlockEntityType.Builder.of(PunjiSticksPlankBE::new,
                            ModBlocks.PUNJI_STICKS_PLANK.get()).build(null));

    public static final RegistryObject<BlockEntityType<CageTrapTickerBlockEntity>> CAGE_TRAP_TICKER_BE =
            BLOCK_ENTITIES.register("cage_trap_ticker_be", () ->
                    BlockEntityType.Builder.of(CageTrapTickerBlockEntity::new,
                            ModBlocks.CAGE_TRAP_TICKER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
