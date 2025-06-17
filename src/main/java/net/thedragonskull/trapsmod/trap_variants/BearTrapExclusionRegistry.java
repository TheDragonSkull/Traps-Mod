package net.thedragonskull.trapsmod.trap_variants;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public class BearTrapExclusionRegistry {

    private static final Set<EntityType<?>> excludedTypes = new HashSet<>();

    public static void register(EntityType<?> type) {
        excludedTypes.add(type);
    }

    public static boolean isExcluded(Entity entity) {
        if (excludedTypes.contains(entity.getType())) return true;

        if (entity.getBbWidth() > 1.5 || entity.getBbHeight() > 3.0) return true;
        if (entity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "bosses")))) return true;

        return false;
    }
}
