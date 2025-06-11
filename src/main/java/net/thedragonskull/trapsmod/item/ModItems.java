package net.thedragonskull.trapsmod.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.trapsmod.TrapsMod;
import net.thedragonskull.trapsmod.item.custom.Hammer;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TrapsMod.MOD_ID);

    public static RegistryObject<Item> HAMMER = ITEMS.register("hammer",
            () -> new Hammer(new Item.Properties().stacksTo(1).durability(50)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
