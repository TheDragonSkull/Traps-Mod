package net.thedragonskull.trapsmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thedragonskull.trapsmod.block.entity.CageTrapTickerBlockEntity;
import net.thedragonskull.trapsmod.block.entity.FakeFloorBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class FakeFloor extends HorizontalDirectionalBlock implements EntityBlock {
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    protected static final VoxelShape TOP_AABB = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public FakeFloor(Properties pProperties) {
        super(pProperties.sound(FAKE_FLOOR_SOUND));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM));
    }

    public static final SoundType FAKE_FLOOR_SOUND = new SoundType(
            1.0f,
            1.0f,
            SoundEvents.BAMBOO_WOOD_FENCE_GATE_CLOSE, // break sound
            SoundEvents.BAMBOO_WOOD_FENCE_GATE_OPEN,  // step sound
            SoundEvents.BAMBOO_PLACE,  // place sound
            SoundEvents.BAMBOO_WOOD_FENCE_GATE_CLOSE, // hit sound
            SoundEvents.BAMBOO_WOOD_FENCE_GATE_CLOSE  // fall sound
    );


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, HALF);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.BAMBOO_PLACE, SoundSource.BLOCKS);
        }
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(oldState, level, pos, newState, isMoving);

        if (!level.isClientSide()
                && oldState.getBlock() instanceof FakeFloor
                && oldState.getBlock() != newState.getBlock()) {

            Half originHalf = oldState.getValue(HALF);

            Queue<BlockPos> toCheck = new LinkedList<>();
            Set<BlockPos> visited = new HashSet<>();

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = pos.relative(dir);
                toCheck.add(neighbor);
            }

            while (!toCheck.isEmpty()) {
                BlockPos current = toCheck.poll();

                if (!visited.add(current)) continue;

                BlockState state = level.getBlockState(current);
                if (!(state.getBlock() instanceof FakeFloor)) continue;
                if (state.getValue(HALF) != originHalf) continue;

                level.destroyBlock(current, false);

                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor)) {
                        toCheck.add(neighbor);
                    }
                }
            }
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);

        if (entity instanceof ItemEntity) return;

        if (!level.isClientSide() && state.getValue(HALF) == Half.TOP) {
            boolean isOnThinBlock = isBlockAboveThin(level, pos);
            BlockPos abovePos = pos.above();
            boolean standingOnTop = entity.getY() >= abovePos.getY() || isOnThinBlock;
            BlockEntity be = level.getBlockEntity(pos);

            if (standingOnTop && be instanceof FakeFloorBlockEntity fakeFloor) {
                fakeFloor.setEntityPresent(true);
            }
        }
    }

    private boolean isBlockAboveThin(Level level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return above.isAir() ||
                above.getCollisionShape(level, pos.above()).isEmpty() ||
                above.canBeReplaced();
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        super.entityInside(pState, pLevel, pPos, pEntity);

        if (pEntity instanceof ItemEntity) return;

        double blockTopY = pPos.getY() + 0.25;
        double entityFeetY = pEntity.getBoundingBox().minY;
        BlockEntity be = pLevel.getBlockEntity(pPos);

        if (!pLevel.isClientSide() && pState.getBlock() instanceof FakeFloor) {

            if (pState.getValue(HALF) == Half.BOTTOM) {
                if (entityFeetY <= blockTopY && entityFeetY >= pPos.getY()) {
                    if (be instanceof FakeFloorBlockEntity fakeFloor) {
                        fakeFloor.setEntityPresent(true);
                    }
                }

            }
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState blockstate = this.defaultBlockState();
        Direction direction = pContext.getClickedFace();
        if (!pContext.replacingClickedOnBlock() && direction.getAxis().isHorizontal()) {
            blockstate = blockstate.setValue(FACING, direction).setValue(HALF, pContext.getClickLocation().y - (double)pContext.getClickedPos().getY() > 0.5D ? Half.TOP : Half.BOTTOM);
        } else {
            blockstate = blockstate.setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP);
        }

        return blockstate;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return pState.getValue(HALF) == Half.TOP ? TOP_AABB : BOTTOM_AABB;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == Half.BOTTOM) {
            return Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
        }
        return TOP_AABB;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide() ? null : ((pLevel1, pPos, pState1, pBlockEntity) -> ((FakeFloorBlockEntity)pBlockEntity).tick());
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return true;
    }

    @Override
    public @Nullable BlockPathTypes getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return BlockPathTypes.TRAPDOOR;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FakeFloorBlockEntity(pPos, pState);
    }
}
