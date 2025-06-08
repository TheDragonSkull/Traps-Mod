package net.thedragonskull.trapsmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.thedragonskull.trapsmod.block.ModBlocks;

import javax.annotation.Nullable;

public class CageTrapUtils {

    public static boolean isCageTrapStructure(Level level, BlockPos origin, boolean ignoreChain, @Nullable BlockState cachedChainState) {
        // Altura relativa 0 a 4
        for (int y = 0; y <= 4; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = origin.offset(dx, y, dz);
                    BlockState state = level.getBlockState(pos);

                    // Centro de cada capa
                    boolean isCenter = dx == 0 && dz == 0;

                    if (y <= 2) {
                        // Jaula de Iron Bars (excepto centro)
                        if (isCenter) {
                            if (!state.isAir()) {
                                //System.out.println("[DEBUG] Capa " + y + " centro no es aire en " + pos);
                                return false;
                            }
                        } else {
                            if (!(state.is(ModTags.Blocks.CAGE_TRAP_WALLS))) {
                                //System.out.println("[DEBUG] Capa " + y + " borde no es bloque de pared válido en " + pos);
                                return false;
                            }
                        }
                    } else if (y == 3) {
                        // Techo
                        if (isCenter) {
                            if (!state.is(BlockTags.FENCES)) {
                                //System.out.println("[DEBUG] Centro de capa 3 no es copper_block en " + pos);
                                return false;
                            }
                        } else {
                            if (!state.is(BlockTags.STAIRS)) {
                                //System.out.println("[DEBUG] Borde de capa 3 no es copper_slab en " + pos);
                                return false;
                            }
                        }
                    } else if (y == 4) {
                        if (isCenter && !ignoreChain) {
                            BlockState chain = cachedChainState != null ? cachedChainState : level.getBlockState(pos);

                            if (!chain.is(ModBlocks.STRONG_CHAIN.get())) {
                                //System.out.println("[DEBUG] Centro de capa 4 no es cadena STRONG_CHAIN en " + pos);
                                return false;
                            }

                            if (chain.getValue(ChainBlock.AXIS) != Direction.Axis.Y) {
                                //System.out.println("[DEBUG] Cadena no está en vertical. AXIS = " + chain.getValue(ChainBlock.AXIS));
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public static void dropCageLayer(Level level, BlockPos base, int y) {

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

                BlockPos pos = base.offset(dx, y, dz);
                BlockState state = level.getBlockState(pos);
                if (state.isAir()) continue;

                Block block = state.getBlock();

                if (state.is(BlockTags.STAIRS)) {
                    state = getConfiguredStairState(state, dx, dz);

                } else if (block instanceof IronBarsBlock) {
                    state = getConfiguredBarState(state, dx, dz);

                } else if (block instanceof FenceBlock) {
                    state = getConfiguredBarState(state, dx, dz);
                }

                FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
                falling.dropItem = false;

                level.removeBlock(pos, false);
                level.addFreshEntity(falling);
            }
        }
    }


    public static BlockState getConfiguredStairState(BlockState state, int dx, int dz) {
        Direction facing = determineStairFacingFromOffset(dx, dz);
        state = state
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, Half.BOTTOM);

        // Ajustes visuales para esquinas específicas
        if (dx == 1 && dz == 1) {
            state = state.setValue(StairBlock.FACING, Direction.NORTH)
                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT);
        } else if (dx == -1 && dz == -1) {
            state = state.setValue(StairBlock.FACING, Direction.SOUTH)
                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT);
        } else if (dx == -1 && dz == 1) {
            state = state.setValue(StairBlock.FACING, Direction.NORTH)
                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT);
        }

        return state;
    }

    private static Direction determineStairFacingFromOffset(int dx, int dz) {
        if (dx == -1 && dz == -1) return Direction.SOUTH; // esquina NO
        if (dx == -1 && dz == 0)  return Direction.EAST;  // lado O
        if (dx == -1 && dz == 1)  return Direction.NORTH; // esquina SO
        if (dx == 0  && dz == -1) return Direction.SOUTH; // lado N
        if (dx == 0  && dz == 1)  return Direction.NORTH; // lado S
        if (dx == 1  && dz == -1) return Direction.SOUTH; // esquina NE
        if (dx == 1  && dz == 0)  return Direction.WEST;  // lado E
        if (dx == 1  && dz == 1)  return Direction.WEST;  // esquina SE
        return Direction.NORTH; // fallback
    }

    public static BlockState getConfiguredBarState(BlockState state, int dx, int dz) {
        if (dx == -1 && dz == -1) { // esquina NO
            return state.setValue(IronBarsBlock.EAST, true)
                    .setValue(IronBarsBlock.SOUTH, true);
        } else if (dx == -1 && dz == 0) { // lado O
            return state.setValue(IronBarsBlock.NORTH, true)
                    .setValue(IronBarsBlock.SOUTH, true);
        } else if (dx == -1 && dz == 1) { // esquina SO
            return state.setValue(IronBarsBlock.EAST, true)
                    .setValue(IronBarsBlock.NORTH, true);
        } else if (dx == 0 && dz == -1) { // lado N
            return state.setValue(IronBarsBlock.EAST, true)
                    .setValue(IronBarsBlock.WEST, true);
        } else if (dx == 0 && dz == 1) { // lado S
            return state.setValue(IronBarsBlock.EAST, true)
                    .setValue(IronBarsBlock.WEST, true);
        } else if (dx == 1 && dz == -1) { // esquina NE
            return state.setValue(IronBarsBlock.WEST, true)
                    .setValue(IronBarsBlock.SOUTH, true);
        } else if (dx == 1 && dz == 0) { // lado E
            return state.setValue(IronBarsBlock.NORTH, true)
                    .setValue(IronBarsBlock.SOUTH, true);
        } else if (dx == 1 && dz == 1) { // esquina SE
            return state.setValue(IronBarsBlock.WEST, true)
                    .setValue(IronBarsBlock.NORTH, true);
        }

        return state;
    }

}
