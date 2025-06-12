package net.thedragonskull.trapsmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;
import net.thedragonskull.trapsmod.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

import static net.thedragonskull.trapsmod.util.BearTrapUtils.rotateShapeFrom;

public class BearTrap extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TRAP_SET = BooleanProperty.create("trap_set");

    protected static final VoxelShape SET_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D);
    protected static final VoxelShape CLOSED_SHAPE = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 9.0D, 16.0D);
    protected static final VoxelShape COLLISION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public BearTrap(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(TRAP_SET, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, TRAP_SET);
    }

    @Override
    public InteractionResult use(BlockState pState, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        BlockState trapState = level.getBlockState(pos);

        if (be instanceof BearTrapBE bearTrap) {

            if (pPlayer.isShiftKeyDown() && pPlayer.getMainHandItem().isEmpty()) {
                if (!trapState.getValue(TRAP_SET)) {
                    bearTrap.setAndTrigger("bear_trap_open");
                    level.setBlock(pos, trapState.setValue(TRAP_SET, true), 3);

                    level.playSound(null, pos, ModSounds.BEAR_TRAP_OPEN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                } else {
                    trapSnap(level, pos);
                }

                return InteractionResult.SUCCESS;
            }
        }


        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState pState, Level level, BlockPos pPos, Entity pEntity) {
        if (!level.isClientSide && pEntity instanceof LivingEntity living) {

            if (pState.getValue(TRAP_SET)) {
                trapSnap(level, pPos);

                if (living instanceof Player player) {
                    ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
                    boolean hasLeggings = !leggings.isEmpty();

                    float damage = hasLeggings ? 4.0f : 6.0f;
                    living.hurt(level.damageSources().cactus(), damage);

                } else {
                    living.hurt(level.damageSources().cactus(), 6);
                }

            }

        }
    }

    private static void trapSnap(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        BlockState trapState = level.getBlockState(pos);

        if (be instanceof BearTrapBE bearTrap) {
            bearTrap.setAndTrigger("bear_trap_snap");
            level.setBlock(pos, trapState.setValue(TRAP_SET, false), 3);

            level.playSound(null, pos, ModSounds.BEAR_TRAP_SNAP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }

    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pState.canSurvive(pLevel, pPos)) {
            pLevel.destroyBlock(pPos, true);
        }
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        return canSupportRigidBlock(pLevel, blockpos) || canSupportCenter(pLevel, blockpos, Direction.UP);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(TRAP_SET, false);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {

        if (!pState.canSurvive(pLevel, pPos)) {
            pLevel.scheduleTick(pPos, this, 1);
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        boolean isTrapSet = state.getValue(TRAP_SET);
        VoxelShape shape = isTrapSet ? SET_SHAPE : CLOSED_SHAPE;

        return rotateShapeFrom(Direction.NORTH, direction, shape);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Rotation rotation = mirror.getRotation(state.getValue(FACING));
        return this.rotate(state, null, null, rotation);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BearTrapBE(pPos, pState);
    }
}
