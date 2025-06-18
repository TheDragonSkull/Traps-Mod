package net.thedragonskull.trapsmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.thedragonskull.trapsmod.block.ModBlocks;

import javax.annotation.Nullable;

/**
 * Utility class for cage trap structure detection and behavior.
 */
public class CageTrapUtils {

    /**
     * Checks if a valid cage trap structure exists at the given origin position.
     * <p>
     * The structure is 3x3 wide and 5 blocks tall, with specific block types expected at each layer:
     * - Layers 0–2: iron bars / fences at the borders, air in the center
     * - Layer 3: stairs at the edges, a fence in the center
     * - Layer 4: strong chain in the center (optional if ignoreChain is true)
     *
     * @param level             The current world level
     * @param origin            The bottom center position of the cage
     * @param ignoreChain       Whether to skip validating the chain block
     * @param cachedChainState  Cached state of the chain (optional), to avoid redundant lookups
     * @return true if the full structure is valid
     */
    public static boolean isCageTrapStructure(Level level, BlockPos origin, boolean ignoreChain, @Nullable BlockState cachedChainState) {
        for (int y = 0; y <= 4; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = origin.offset(dx, y, dz);
                    BlockState state = level.getBlockState(pos);

                    boolean isCenter = dx == 0 && dz == 0;

                    if (y <= 2) {
                        // Layers 0–2: expect air in center, walls on borders
                        if (isCenter) {
                            if (!state.isAir()) {
                                return false;
                            }
                        } else {
                            if (!(state.is(ModTags.Blocks.CAGE_TRAP_WALLS))) {
                                return false;
                            }
                        }
                    } else if (y == 3) {
                        // Layer 3: stairs on edges, fence in the center
                        if (isCenter) {
                            if (!state.is(BlockTags.FENCES)) {
                                return false;
                            }
                        } else {
                            if (!state.is(BlockTags.STAIRS)) {
                                return false;
                            }
                        }
                    } else if (y == 4) {
                        // Layer 4: vertical strong chain (unless ignored)
                        if (isCenter && !ignoreChain) {
                            BlockState chain = cachedChainState != null ? cachedChainState : level.getBlockState(pos);

                            if (!chain.is(ModBlocks.STRONG_CHAIN.get())) {
                                return false;
                            }

                            if (chain.getValue(ChainBlock.AXIS) != Direction.Axis.Y) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Converts one horizontal layer of the cage into falling blocks.
     *
     * @param level The world
     * @param base  The origin position (bottom center of the cage)
     * @param y     The layer height relative to the base (0–3)
     */
    public static void dropCageLayer(Level level, BlockPos base, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

                BlockPos pos = base.offset(dx, y, dz);
                BlockState state = level.getBlockState(pos);

                if (state.is(ModBlocks.CAGE_TRAP_TICKER.get())) continue;

                if (state.isAir()) continue;

                // Skip center blocks for layers 0–2 if they are air
                if (dx == 0 && dz == 0 && y <= 2 && state.isAir()) continue;

                Block block = state.getBlock();

                if (state.is(BlockTags.STAIRS)) {
                    state = getConfiguredStairState(state, dx, dz);

                } else if (block instanceof IronBarsBlock || block instanceof FenceBlock) {
                    state = getConfiguredBarState(state, dx, dz);

                }

                FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
                falling.dropItem = false;

                level.removeBlock(pos, false);
                level.addFreshEntity(falling);
            }
        }
    }

    /**
     * Applies the correct visual orientation to a stair block based on its offset within the 3x3 grid.
     */
    public static BlockState getConfiguredStairState(BlockState state, int dx, int dz) {
        Direction facing = determineStairFacingFromOffset(dx, dz);
        state = state
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, Half.BOTTOM);

        // Set corner shape for visual smoothness
        if (dx == 1 && dz == 1) {
            state = state.setValue(StairBlock.FACING, Direction.NORTH)
                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT);
        } else if (dx == -1 && dz == -1) {
            state = state.setValue(StairBlock.FACING, Direction.SOUTH)
                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT);
        } else if (dx == -1 && dz == 1) {
            state = state.setValue(StairBlock.FACING, Direction.NORTH)
                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT);
        } else if (dx == 1 && dz == -1) {
            state = state.setValue(StairBlock.FACING, Direction.SOUTH)
                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT);
        }


        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
        }

        return state;
    }

    /**
     * Determines stair facing based on offset within the grid.
     */
    private static Direction determineStairFacingFromOffset(int dx, int dz) {
        if (dx == -1 && dz == -1) return Direction.SOUTH; // northwest corner
        if (dx == -1 && dz == 0)  return Direction.EAST;  // west edge
        if (dx == -1 && dz == 1)  return Direction.NORTH; // southwest corner
        if (dx == 0  && dz == -1) return Direction.SOUTH; // north edge
        if (dx == 0  && dz == 1)  return Direction.NORTH; // south edge
        if (dx == 1  && dz == -1) return Direction.SOUTH; // northeast corner
        if (dx == 1  && dz == 0)  return Direction.WEST;  // east edge
        if (dx == 1  && dz == 1)  return Direction.WEST;  // southeast corner
        return Direction.NORTH; // fallback
    }

    /**
     * Sets the iron bars connections depending on the block's position in the 3x3 grid.
     */
    public static BlockState getConfiguredBarState(BlockState state, int dx, int dz) {
        if (dx == -1 && dz == -1) { // northwest
            return state.setValue(IronBarsBlock.EAST, true)
                    .setValue(IronBarsBlock.SOUTH, true);
        } else if (dx == -1 && dz == 0) { // west
            return state.setValue(IronBarsBlock.NORTH, true)
                    .setValue(IronBarsBlock.SOUTH, true);
        } else if (dx == -1 && dz == 1) { // southwest
            return state.setValue(IronBarsBlock.EAST, true)
                    .setValue(IronBarsBlock.NORTH, true);
        } else if (dx == 0 && dz == -1) { // north
            return state.setValue(IronBarsBlock.EAST, true)
                    .setValue(IronBarsBlock.WEST, true);
        } else if (dx == 0 && dz == 1) { // south
            return state.setValue(IronBarsBlock.EAST, true)
                    .setValue(IronBarsBlock.WEST, true);
        } else if (dx == 1 && dz == -1) { // northeast
            return state.setValue(IronBarsBlock.WEST, true)
                    .setValue(IronBarsBlock.SOUTH, true);
        } else if (dx == 1 && dz == 0) { // east
            return state.setValue(IronBarsBlock.NORTH, true)
                    .setValue(IronBarsBlock.SOUTH, true);
        } else if (dx == 1 && dz == 1) { // southeast
            return state.setValue(IronBarsBlock.WEST, true)
                    .setValue(IronBarsBlock.NORTH, true);
        }

        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
        }

        return state;
    }

}
