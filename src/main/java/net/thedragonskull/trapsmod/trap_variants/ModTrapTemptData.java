package net.thedragonskull.trapsmod.trap_variants;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public class ModTrapTemptData {

    public static void registerDefaults() {
        TrapTemptRegistry.register(EntityType.COW, Ingredient.of(Items.WHEAT));
        TrapTemptRegistry.register(EntityType.SHEEP, Ingredient.of(Items.WHEAT));
        TrapTemptRegistry.register(EntityType.PIG, Ingredient.of(Items.CARROT, Items.POTATO, Items.BEETROOT));
        TrapTemptRegistry.register(EntityType.CHICKEN, Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD));
        TrapTemptRegistry.register(EntityType.RABBIT, Ingredient.of(Items.CARROT, Items.GOLDEN_CARROT, Items.DANDELION));
        TrapTemptRegistry.register(EntityType.CAT, Ingredient.of(Items.COD, Items.SALMON));
        TrapTemptRegistry.register(EntityType.OCELOT, Ingredient.of(Items.COD, Items.SALMON));
        TrapTemptRegistry.register(EntityType.PANDA, Ingredient.of(Blocks.BAMBOO.asItem()));
        TrapTemptRegistry.register(EntityType.HORSE, Ingredient.of(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE));
        TrapTemptRegistry.register(EntityType.MULE, Ingredient.of(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE));
        TrapTemptRegistry.register(EntityType.DONKEY, Ingredient.of(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE));
        TrapTemptRegistry.register(EntityType.TURTLE, Ingredient.of(Blocks.SEAGRASS.asItem()));


        // NEW
        TrapTemptRegistry.register(EntityType.PARROT, Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD));
        TrapTemptRegistry.register(EntityType.FOX, Ingredient.of(Items.SWEET_BERRIES, Items.GLOW_BERRIES));
        TrapTemptRegistry.register(EntityType.GOAT, Ingredient.of(Items.WHEAT));
        TrapTemptRegistry.register(EntityType.FROG, Ingredient.of(Items.SLIME_BALL));
        TrapTemptRegistry.register(EntityType.TADPOLE, Ingredient.of(Items.SLIME_BALL));
        TrapTemptRegistry.register(EntityType.AXOLOTL, Ingredient.of(Items.TROPICAL_FISH_BUCKET));
        TrapTemptRegistry.register(EntityType.AXOLOTL, Ingredient.of(ItemTags.FLOWERS));
        TrapTemptRegistry.register(EntityType.CAMEL, Ingredient.of(Items.CACTUS));
        TrapTemptRegistry.register(EntityType.LLAMA, Ingredient.of(Items.WHEAT, Blocks.HAY_BLOCK.asItem()));
        TrapTemptRegistry.register(EntityType.PIGLIN, Ingredient.of(Items.GOLD_INGOT));
        TrapTemptRegistry.register(EntityType.WOLF, Ingredient.of(Items.CHICKEN, Items.BEEF, Items.MUTTON));
    }
}


