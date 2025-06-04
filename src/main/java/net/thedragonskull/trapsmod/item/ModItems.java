package net.thedragonskull.trapsmod.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.thedragonskull.trapsmod.TrapsMod;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TrapsMod.MOD_ID);





    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
