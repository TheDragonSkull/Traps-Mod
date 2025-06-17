package net.thedragonskull.trapsmod.trap_variants;

import net.minecraft.world.entity.EntityType;

public class ModTrapExclusions {

    public static void registerDefaults() {
        BearTrapExclusionRegistry.register(EntityType.ENDER_DRAGON);
        BearTrapExclusionRegistry.register(EntityType.GHAST);
        BearTrapExclusionRegistry.register(EntityType.WITHER);
        BearTrapExclusionRegistry.register(EntityType.GIANT);
        BearTrapExclusionRegistry.register(EntityType.ENDERMAN);
        BearTrapExclusionRegistry.register(EntityType.ILLUSIONER);

        BearTrapExclusionRegistry.register(EntityType.RAVAGER);
        BearTrapExclusionRegistry.register(EntityType.WARDEN);
    }
}

