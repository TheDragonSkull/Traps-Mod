package net.thedragonskull.trapsmod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.trapsmod.TrapsMod;
import net.thedragonskull.trapsmod.block.ModBlocks;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TrapsMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TRAPS_TAB = CREATIVE_MODE_TABS.register("traps_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.BEAR_TRAP.get()))
                    .title(Component.translatable("creativetab.traps_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        pOutput.accept(ModBlocks.STRONG_CHAIN.get());
                        pOutput.accept(ModBlocks.PUNJI_STICKS_PLANK.get());
                        pOutput.accept(ModBlocks.SHARPENED_BAMBOO.get());
                        pOutput.accept(ModBlocks.CREAKING_FLOOR.get());
                        pOutput.accept(ModBlocks.BEAR_TRAP.get());
                        pOutput.accept(ModBlocks.FAKE_FLOOR.get());
                        pOutput.accept(ModBlocks.B_FAKE_FLOOR.get());

                        pOutput.accept(ModItems.HAMMER.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
