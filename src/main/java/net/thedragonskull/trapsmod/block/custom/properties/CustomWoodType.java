package net.thedragonskull.trapsmod.block.custom.properties;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum CustomWoodType implements StringRepresentable {
    OAK("oak", Blocks.OAK_PLANKS),
    SPRUCE("spruce", Blocks.SPRUCE_PLANKS),
    BIRCH("birch", Blocks.BIRCH_PLANKS),
    JUNGLE("jungle", Blocks.JUNGLE_PLANKS),
    ACACIA("acacia", Blocks.ACACIA_PLANKS),
    DARK_OAK("dark_oak", Blocks.DARK_OAK_PLANKS),
    BAMBOO("bamboo", Blocks.BAMBOO_PLANKS),
    CHERRY("cherry", Blocks.CHERRY_PLANKS),
    MANGROVE("mangrove", Blocks.MANGROVE_PLANKS),
    CRIMSON("crimson", Blocks.CRIMSON_PLANKS),
    WARPED("warped", Blocks.WARPED_PLANKS);

    private final String name;
    private final Block planks;

    CustomWoodType(String name, Block planks) {
        this.name = name;
        this.planks = planks;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Block getPlanks() {
        return this.planks;
    }
}
