package net.thedragonskull.trapsmod.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.thedragonskull.trapsmod.TrapsMod;

public class ModTags {

    public static class Blocks {
        public static final TagKey<Block> CAGE_TRAP_WALLS = TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "cage_trap_walls"));
    }

}
