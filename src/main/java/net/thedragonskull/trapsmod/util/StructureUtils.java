package net.thedragonskull.trapsmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.thedragonskull.trapsmod.block.ModBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StructureUtils {

    public static boolean isCageTrapStructure(Level level, BlockPos origin, boolean ignoreChain) {
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
                                System.out.println("[DEBUG] Capa " + y + " centro no es aire en " + pos);
                                return false;
                            }
                        } else {
                            if (!state.is(Blocks.IRON_BARS)) {
                                System.out.println("[DEBUG] Capa " + y + " borde no es iron_bars en " + pos);
                                return false;
                            }
                        }
                    } else if (y == 3) {
                        // Techo
                        if (isCenter) {
                            if (!state.is(BlockTags.FENCES)) {
                                System.out.println("[DEBUG] Centro de capa 3 no es copper_block en " + pos);
                                return false;
                            }
                        } else {
                            if (!state.is(Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS)) {
                                System.out.println("[DEBUG] Borde de capa 3 no es copper_slab en " + pos);
                                return false;
                            }
                        }
                    } else if (y == 4) {
                        // Cuerda (solo centro)
                        if (isCenter) {
                            if (!ignoreChain && !state.is(ModBlocks.STRONG_CHAIN.get())) {
                                System.out.println("[DEBUG] Centro de capa 4 no es custom chain en " + pos);
                                return false;
                            }
                        } else {
                            if (!state.isAir()) {
                                System.out.println("[DEBUG] Borde de capa 4 no es aire en " + pos);
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public static void triggerFallingCage(Level level, BlockPos base) {
        System.out.println("[DEBUG] Iniciando caída de jaula...");

        for (int y = 0; y <= 3; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = base.offset(dx, y, dz);
                    BlockState state = level.getBlockState(pos);

                    if (state.is(Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS)) {
                        Direction facing = determineStairFacingFromOffset(dx, dz);
                        state = state
                                .setValue(StairBlock.FACING, facing)
                                .setValue(StairBlock.HALF, Half.BOTTOM); // o BOTTOM según encaje visualmente

                        if (dx == 1 && dz == 1) {
                            state = state.setValue(StairBlock.FACING, Direction.NORTH)
                                    .setValue(StairBlock.HALF, Half.BOTTOM)
                                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT);
                        }
                    } else if (state.is(Blocks.IRON_BARS)) {
                        if (state.is(Blocks.IRON_BARS)) {
                            boolean north = dz == -1;
                            boolean south = dz == 1;
                            boolean west  = dx == -1;
                            boolean east  = dx == 1;

                            state = state
                                    .setValue(IronBarsBlock.NORTH, north)
                                    .setValue(IronBarsBlock.SOUTH, south)
                                    .setValue(IronBarsBlock.WEST, west)
                                    .setValue(IronBarsBlock.EAST, east);
                        }

                    }


                    if (state.isAir()) continue;

                    FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
                    falling.dropItem = false;

                    level.removeBlock(pos, false);
                    level.addFreshEntity(falling);

                }
            }
        }

    }

    private static Direction determineStairFacingFromOffset(int dx, int dz) {
        if (dx == -1 && dz == -1) return Direction.SOUTH; // esquina NO
        if (dx == -1 && dz == 0)  return Direction.EAST;  // lado O
        if (dx == -1 && dz == 1)  return Direction.NORTH; // esquina SO
        if (dx == 0  && dz == -1) return Direction.SOUTH; // lado N
        if (dx == 0  && dz == 1)  return Direction.NORTH; // lado S
        if (dx == 1  && dz == -1) return Direction.SOUTH; // esquina NE
        if (dx == 1  && dz == 0)  return Direction.WEST;  // lado E
        if (dx == 1  && dz == 1)  return Direction.WEST; // esquina SE

        return Direction.NORTH; // fallback
    }







}
