package net.thedragonskull.trapsmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PunjiStickPlankUtils {

    public static boolean isNonBlocking(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.isAir() || state.canBeReplaced()) return true;

        VoxelShape shape = state.getCollisionShape(level, pos);
        return shape.isEmpty() || shape.bounds().maxY <= 0.1D;
    }

    public static boolean shouldDestroyAndDrop(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.isAir()) return false;

        if (state.canBeReplaced()) return true;

        VoxelShape shape = state.getCollisionShape(level, pos);

        if (shape.isEmpty()) return true; // ← si no tiene colisión, se puede romper

        return shape.bounds().maxY <= 0.1D;
    }


}
