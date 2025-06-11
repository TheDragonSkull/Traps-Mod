package net.thedragonskull.trapsmod.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.trapsmod.TrapsMod;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TrapsMod.MOD_ID);

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, name)));
    }

    public static final RegistryObject<SoundEvent> CREAKING_1 =
            registerSoundEvents("creaking_1");

    public static final RegistryObject<SoundEvent> CREAKING_2 =
            registerSoundEvents("creaking_2");

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
