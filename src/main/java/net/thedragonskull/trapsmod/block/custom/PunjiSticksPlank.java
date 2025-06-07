package net.thedragonskull.trapsmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thedragonskull.trapsmod.block.custom.properties.PlankPart;
import net.thedragonskull.trapsmod.block.entity.PunjiSticksPlankBE;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import static net.thedragonskull.trapsmod.util.PunjiStickPlankUtils.*;

public class PunjiSticksPlank extends HorizontalDirectionalBlock implements EntityBlock {

    public static final EnumProperty<PlankPart> PLANK_PART = EnumProperty.create("plank_part", PlankPart.class);
    public static final BooleanProperty BASE_ACTIVE = BooleanProperty.create("base_active");
    public static final BooleanProperty EXTENSION_ACTIVE = BooleanProperty.create("extension_active");

    private static final VoxelShape SHAPE_CLOSED = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D); // SOUTH

    private static boolean internalRemove = false;

    public PunjiSticksPlank(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(PLANK_PART, PlankPart.BASE)
                        .setValue(BASE_ACTIVE, false)
                        .setValue(EXTENSION_ACTIVE, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PLANK_PART, BASE_ACTIVE, EXTENSION_ACTIVE);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.Entity entity) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(FACING);
        PlankPart part = state.getValue(PLANK_PART);

        BlockPos basePos = switch (part) {
            case BASE -> pos;
            case EXTENSION -> pos.relative(facing.getOpposite());
            default -> null;
        };

        if (basePos == null) return;

        BlockState baseState = level.getBlockState(basePos);
        if (!(baseState.getBlock() instanceof PunjiSticksPlank) || baseState.getValue(PLANK_PART) != PlankPart.BASE) return;

        boolean isBaseActive = baseState.getValue(BASE_ACTIVE);
        boolean isExtActive = baseState.getValue(EXTENSION_ACTIVE);

        // If active, does nothing
        if (isBaseActive || isExtActive) return;

        // Check block above
        BlockPos aboveBase = basePos.above();
        BlockPos aboveExt = basePos.relative(facing).above();

        BlockState stateAboveBase = level.getBlockState(aboveBase);
        BlockState stateAboveExt = level.getBlockState(aboveExt);

        if (!isNonBlocking(stateAboveBase, level, aboveBase) || !isNonBlocking(stateAboveExt, level, aboveExt)) return;

        // Check stepped on part
        String anim;
        if (part == PlankPart.BASE) {
            baseState = baseState.setValue(BASE_ACTIVE, true);
            anim = "base_activate";
        } else {
            baseState = baseState.setValue(EXTENSION_ACTIVE, true);
            anim = "extension_activate";
        }

        level.setBlock(basePos, baseState, 3);

        BlockEntity be = level.getBlockEntity(basePos);
        if (be instanceof PunjiSticksPlankBE punji) {
            punji.setAndTrigger(anim);

            RandomSource randomsource = level.getRandom();
            level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F,
                    (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F);
        }

        if (shouldDestroyAndDrop(stateAboveBase, level, aboveBase)) {
            level.destroyBlock(aboveBase, true);
        }
        if (shouldDestroyAndDrop(stateAboveExt, level, aboveExt)) {
            level.destroyBlock(aboveExt, true);
        }

        // Set TOP part
        if (level.isEmptyBlock(aboveBase)) {
            BlockState topBlock = this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PLANK_PART, PlankPart.TOP)
                    .setValue(BASE_ACTIVE, false)
                    .setValue(EXTENSION_ACTIVE, false);
            level.setBlock(aboveBase, topBlock, 3);
        }
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Direction facing = state.getValue(FACING);
        PlankPart part = state.getValue(PLANK_PART);

        BlockPos basePos = switch (part) {
            case BASE -> pos;
            case TOP -> pos.below();
            case EXTENSION -> pos.relative(facing.getOpposite());
        };

        BlockState baseState = level.getBlockState(basePos);

        if (baseState.getBlock() instanceof PunjiSticksPlank &&
                baseState.getValue(PLANK_PART) == PlankPart.BASE) {

            boolean isBaseActive = baseState.getValue(PunjiSticksPlank.BASE_ACTIVE);
            boolean isExtActive = baseState.getValue(PunjiSticksPlank.EXTENSION_ACTIVE);

            if (!isBaseActive && !isExtActive) {
                BlockPos aboveBase = basePos.above();
                BlockPos aboveExt = basePos.relative(facing).above();

                BlockState stateAboveBase = level.getBlockState(aboveBase);
                BlockState stateAboveExt = level.getBlockState(aboveExt);

                boolean baseBlocked = !isNonBlocking(stateAboveBase, level, aboveBase);
                boolean extBlocked = !isNonBlocking(stateAboveExt, level, aboveExt);

                if (baseBlocked || extBlocked) return InteractionResult.FAIL;
            }

            String anim;

            // Play animation
            if (isBaseActive) {
                // reset base
                baseState = baseState.setValue(PunjiSticksPlank.BASE_ACTIVE, false);
                anim = "base_reset";
            } else if (isExtActive) {
                // reset extension
                baseState = baseState.setValue(PunjiSticksPlank.EXTENSION_ACTIVE, false);
                anim = "extension_reset";
            } else {
                // If none active, play based on side used
                if (part == PlankPart.BASE) {
                    baseState = baseState.setValue(PunjiSticksPlank.BASE_ACTIVE, true);
                    anim = "base_activate";
                } else {
                    baseState = baseState.setValue(PunjiSticksPlank.EXTENSION_ACTIVE, true);
                    anim = "extension_activate";
                }
            }

            level.setBlock(basePos, baseState, 3);

            BlockEntity be = level.getBlockEntity(basePos);
            if (be instanceof PunjiSticksPlankBE punji) {
                punji.setAndTrigger(anim);

                RandomSource randomsource = level.getRandom();
                level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F,
                        (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F);
            }

            if (baseState.getValue(BASE_ACTIVE) || baseState.getValue(EXTENSION_ACTIVE)) {
                BlockPos topPos = basePos.above();

                BlockPos aboveBase = basePos.above();
                BlockPos aboveExt = basePos.relative(facing).above();

                BlockState aboveBaseState = level.getBlockState(aboveBase);
                BlockState aboveExtState = level.getBlockState(aboveExt);

                if (shouldDestroyAndDrop(aboveBaseState, level, aboveBase)) {
                    level.destroyBlock(aboveBase, true);
                }
                if (shouldDestroyAndDrop(aboveExtState, level, aboveExt)) {
                    level.destroyBlock(aboveExt, true);
                }

                if (level.isEmptyBlock(topPos)) {
                    BlockState newBlock = this.defaultBlockState()
                            .setValue(FACING, facing)
                            .setValue(PLANK_PART, PlankPart.TOP)
                            .setValue(BASE_ACTIVE, false)
                            .setValue(EXTENSION_ACTIVE, false);

                    // Set TOP part
                    level.setBlock(topPos, newBlock, 3);
                }
            } else {
                BlockPos topPos = basePos.above();
                BlockState topState = level.getBlockState(topPos);
                if (topState.getBlock() instanceof PunjiSticksPlank) {
                    internalRemove = true;
                    level.removeBlock(topPos, false);
                    internalRemove = false;
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        if (state.getValue(BASE_ACTIVE) || state.getValue(EXTENSION_ACTIVE)) {
            Direction facing = state.getValue(FACING);

            return OPEN_SHAPES.getOrDefault(facing, Shapes.empty());
        }

        return getShape(state, level, pos, pContext);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        PlankPart part = state.getValue(PLANK_PART);

        boolean active = state.getValue(BASE_ACTIVE) || state.getValue(EXTENSION_ACTIVE);

        if (part == PlankPart.TOP || (part == PlankPart.BASE && active)) {
            return OPEN_SHAPES.getOrDefault(facing, Shapes.empty());
        }

        if (part == PlankPart.BASE) {
            return SHAPE_CLOSED;
        }

        // EXTENSION
        BlockPos basePos = pos.relative(facing.getOpposite());
        BlockState baseState = level.getBlockState(basePos);

        if (baseState.getBlock() instanceof PunjiSticksPlank) {
            boolean baseActive = baseState.getValue(BASE_ACTIVE);
            boolean extActive = baseState.getValue(EXTENSION_ACTIVE);

            if (baseActive || extActive) {
                return Shapes.empty();
            }
        }

        return SHAPE_CLOSED;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        BlockPos secondPos = pos.relative(facing);
        Level level = context.getLevel();

        BlockState secondState = level.getBlockState(secondPos);

        if (!secondState.canBeReplaced(context)) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PLANK_PART, PlankPart.BASE)
                .setValue(BASE_ACTIVE, false)
                .setValue(EXTENSION_ACTIVE, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        BlockPos extensionPos = pos.relative(facing);

        if (!level.isClientSide) {
            BlockState extensionState = this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PLANK_PART, PlankPart.EXTENSION)
                    .setValue(BASE_ACTIVE, false)
                    .setValue(EXTENSION_ACTIVE, false);

            level.setBlock(extensionPos, extensionState, 3);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (internalRemove) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }

        if (state.getBlock() != newState.getBlock()) {
            Direction facing = state.getValue(PunjiSticksPlank.FACING);
            PlankPart part = state.getValue(PunjiSticksPlank.PLANK_PART);

            BlockPos basePos = switch (part) {
                case BASE -> pos;
                case TOP -> pos.below();
                case EXTENSION -> pos.relative(facing.getOpposite());
            };

            BlockPos extPos = basePos.relative(facing);
            BlockPos topPos = basePos.above();

            internalRemove = true;

            // BASE
            if (level.getBlockState(basePos).getBlock() instanceof PunjiSticksPlank) {
                level.setBlock(basePos, Blocks.AIR.defaultBlockState(), 35);
            }

            // EXTENSION
            if (level.getBlockState(extPos).getBlock() instanceof PunjiSticksPlank) {
                level.setBlock(extPos, Blocks.AIR.defaultBlockState(), 35);
            }

            // TOP
            if (level.getBlockState(topPos).getBlock() instanceof PunjiSticksPlank) {
                level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 35);
            }

            internalRemove = false;

            // Only drop one time when any part broken
            if (!level.isClientSide) {
                Player nearest = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5.0D, false);
                boolean isCreative = nearest != null && nearest.isCreative();

                if (!isCreative) {
                    popResource(level, basePos, new ItemStack(this.asItem()));
                }
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
                                  BlockPos currentPos, BlockPos facingPos) {
        PlankPart part = state.getValue(PLANK_PART);
        Direction expectedOtherPartDir = getNeighbourDirection(part, state.getValue(FACING));

        if (facing == expectedOtherPartDir) {
            boolean isValidPair = facingState.is(this)
                    && facingState.getValue(PLANK_PART) != part
                    && facingState.getValue(FACING) == state.getValue(FACING);

            return isValidPair ? state : Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    private static Direction getNeighbourDirection(PlankPart part, Direction facing) {
        return part == PlankPart.BASE ? facing : facing.getOpposite();
    }

    public static Direction getConnectedDirection(BlockState pState) {
        Direction direction = pState.getValue(FACING);
        return pState.getValue(PLANK_PART) == PlankPart.BASE ? direction.getOpposite() : direction;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PunjiSticksPlankBE(pPos, pState);
    }
}
