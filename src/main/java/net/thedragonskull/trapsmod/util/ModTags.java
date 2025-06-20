package net.thedragonskull.trapsmod.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.thedragonskull.trapsmod.TrapsMod;

public class ModTags {

    public static class Blocks {

        public static final TagKey<Block> CAGE_TRAP_WALLS = TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "cage_trap_walls"));

        public static final TagKey<Block> SHARPENED_BAMBOO_PLANTABLE_ON = TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "sharpened_bamboo_plantable_on"));

        public static final TagKey<Block> BURYABLE_ON = TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "buryable_on"));
    }

    public static class Items {

        public static final TagKey<Item> VALID_TRAP_ITEMS = TagKey.create(
                Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "valid_trap_items"));

        public static final TagKey<Item> TEMPT_ITEMS = TagKey.create(
                Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "tempt_items"));

    }

}
