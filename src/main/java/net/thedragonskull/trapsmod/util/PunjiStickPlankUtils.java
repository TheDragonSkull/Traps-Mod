package net.thedragonskull.trapsmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

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

        if (shape.isEmpty()) return true; // Can break if no collision

        return shape.bounds().maxY <= 0.1D;
    }

    public static final Map<Direction, VoxelShape> OPEN_SHAPES = Map.of(
            Direction.SOUTH, Block.box(0, 0, 15, 16, 16, 17),
            Direction.NORTH, Block.box(0, 0, -1, 16, 16, 1),
            Direction.EAST,  Block.box(15, 0, 0, 17, 16, 16),
            Direction.WEST,  Block.box(-1, 0, 0, 1, 16, 16)
    );

}
