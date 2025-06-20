package net.thedragonskull.trapsmod.block.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;
import net.thedragonskull.trapsmod.trap_variants.BearTrapExclusionRegistry;
import net.thedragonskull.trapsmod.util.BearTrapUtils;
import net.thedragonskull.trapsmod.util.ModTags;
import org.jetbrains.annotations.Nullable;

import static net.thedragonskull.trapsmod.util.BearTrapUtils.*;

public class BearTrap extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TRAP_SET = BooleanProperty.create("trap_set");
    public static final BooleanProperty BURIED = BooleanProperty.create("buried");

    protected static final VoxelShape SET_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D);
    protected static final VoxelShape CLOSED_SHAPE = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 9.0D, 16.0D);
    protected static final VoxelShape COLLISION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public BearTrap(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(TRAP_SET, false)
                        .setValue(BURIED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, TRAP_SET, BURIED);
    }

    // Cannot break if buried
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (state.getValue(BURIED)) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }


    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (!pLevel.isClientSide && pPlacer instanceof Player player) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof BearTrapBE trapBE) {
                trapBE.setOwner(player.getUUID());
                trapBE.setOwnerName(player.getName().getString());
                trapBE.releaseTrapped();
            }
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {

        if (pState.getBlock() == pNewState.getBlock()) {
            super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
            return;
        }

        BlockEntity be = pLevel.getBlockEntity(pPos);

        if (be instanceof BearTrapBE trapBE) {
            ItemStack trapItem = trapBE.getTrapItem();

            if (!trapItem.isEmpty()) {
                ItemEntity drop = new ItemEntity(pLevel, pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, trapItem);
                pLevel.addFreshEntity(drop);
            }

            trapBE.ignoreEntity(trapBE.trappedEntityId, 40);
            trapBE.releaseTrapped();
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public InteractionResult use(BlockState pState, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        if (level.isClientSide) {
            if (pPlayer.getItemInHand(pHand).getItem() instanceof BlockItem) {
                if (pPlayer.isShiftKeyDown()) {
                    return InteractionResult.PASS;
                } else {
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BearTrapBE trapBE)) return InteractionResult.PASS;

        if (!pPlayer.isShiftKeyDown() && pState.getValue(TRAP_SET)) BearTrapUtils.interact(pPlayer, level, pos, pHand, trapBE.getItemHandler());

        boolean isOwner = trapBE.getOwner() != null && trapBE.getOwner().equals(pPlayer.getUUID());

        if (pPlayer.isShiftKeyDown() && pPlayer.getMainHandItem().isEmpty() && !pState.getValue(BURIED)) {
            if (!pState.getValue(TRAP_SET)) {

                if (!isOwner) {
                    pPlayer.displayClientMessage(Component.literal("Only the owner of the trap can disarm it.")
                            .withStyle(ChatFormatting.DARK_RED), true);
                    return InteractionResult.FAIL;
                }

                trapSet(level, pos);
                trapBE.ignoreEntity(trapBE.trappedEntityId, 40);
                trapBE.releaseTrapped();

                return InteractionResult.SUCCESS;

            } else {

                trapSnap(level, pos, null);
                return InteractionResult.SUCCESS;
            }
        } else if (pPlayer.getMainHandItem().is(ItemTags.SHOVELS)) {
            boolean isBuried = pState.getValue(BearTrap.BURIED);

            if (!pState.getValue(TRAP_SET)) return InteractionResult.FAIL;

            ItemStack tool = pPlayer.getItemInHand(pHand);

            if (!isBuried) {
                // Bury if correct block below
                if (!level.getBlockState(pos.below()).is(ModTags.Blocks.BURYABLE_ON)) return InteractionResult.FAIL;

                level.setBlock(pos, pState.setValue(BearTrap.BURIED, true), 3);
                level.playSound(null, pos, SoundEvents.BRUSH_SAND, SoundSource.BLOCKS, 1.0F, 1.0F);

                tool.hurtAndBreak(1, pPlayer, (p) -> p.broadcastBreakEvent(pHand));
            } else {
                // Unbury
                level.setBlock(pos, pState.setValue(BearTrap.BURIED, false), 3);
                level.playSound(null, pos, SoundEvents.BRUSH_GRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);

                tool.hurtAndBreak(1, pPlayer, (p) -> p.broadcastBreakEvent(pHand));
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void entityInside(BlockState pState, Level level, BlockPos pPos, Entity pEntity) {
        if (!(level.getBlockEntity(pPos) instanceof BearTrapBE bearTrap)) return;

        double xInBlock = pEntity.getX() - pPos.getX();
        double zInBlock = pEntity.getZ() - pPos.getZ();
        double centerTolerance = 0.5;

        boolean isCentered = xInBlock > 0.5 - centerTolerance && xInBlock < 0.5 + centerTolerance &&
                zInBlock > 0.5 - centerTolerance && zInBlock < 0.5 + centerTolerance;

        if (!level.isClientSide && pState.getValue(BearTrap.TRAP_SET)) {

            if (pEntity instanceof LivingEntity living) {
                if (isCentered) {
                    if (bearTrap.trappedEntityId == null &&
                            (bearTrap.ignoredEntity == null || !bearTrap.ignoredEntity.equals(living.getUUID()))) {

                        trapSnap(level, pPos, living);
                    }
                } else {
                    if (living.invulnerableTime <= 0) {
                        living.hurt(level.damageSources().cactus(), 1);
                    }
                }
            }

            else if (pEntity instanceof ItemEntity) {
                trapSnap(level, pPos, null);
            }
        }
    }


    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide ? (lvl, pos, st, be) -> {
            if (be instanceof BearTrapBE trapBE) trapBE.tick();
        } : null;
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
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BearTrapBE bearTrap) {
            return bearTrap.getRedstoneSignal();
        }
        return 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
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
        return state.getValue(BURIED) ? Shapes.empty() : COLLISION_SHAPE;
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
