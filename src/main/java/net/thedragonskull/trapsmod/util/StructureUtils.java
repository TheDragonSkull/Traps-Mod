package net.thedragonskull.trapsmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thedragonskull.trapsmod.block.ModBlocks;

public class StructureUtils {

    public static boolean isCageTrapStructure(Level level, BlockPos origin) {
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
                            if (!state.is(Blocks.WAXED_WEATHERED_CUT_COPPER)) {
                                System.out.println("[DEBUG] Centro de capa 3 no es copper_block en " + pos);
                                return false;
                            }
                        } else {
                            if (!state.is(Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB)) {
                                System.out.println("[DEBUG] Borde de capa 3 no es copper_slab en " + pos);
                                return false;
                            }
                        }
                    } else if (y == 4) {
                        // Cuerda (solo centro)
                        if (isCenter) {
                            if (!state.is(ModBlocks.BELL_TRAP_CHAIN.get())) {
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

                    if (state.isAir()) continue;

                    FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
                    falling.dropItem = false;

                    level.removeBlock(pos, false);
                    level.addFreshEntity(falling);
                }
            }
        }
    }




}
