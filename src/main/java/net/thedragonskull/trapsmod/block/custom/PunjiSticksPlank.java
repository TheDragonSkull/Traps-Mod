package net.thedragonskull.trapsmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thedragonskull.trapsmod.block.custom.properties.PlankPart;
import net.thedragonskull.trapsmod.block.entity.PunjiSticksPlankBE;
import org.jetbrains.annotations.Nullable;

public class PunjiSticksPlank extends HorizontalDirectionalBlock implements EntityBlock {

    public static final EnumProperty<PlankPart> PLANK_PART = EnumProperty.create("plank_part", PlankPart.class);
    public static final BooleanProperty BASE_ACTIVE = BooleanProperty.create("base_active");
    public static final BooleanProperty EXTENSION_ACTIVE = BooleanProperty.create("extension_active");
    private static final VoxelShape SHAPE_CLOSED = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_OPEN_Z = Block.box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape SHAPE_OPEN_X = Block.box(14, 0, 0, 16, 16, 16);


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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Direction facing = state.getValue(FACING);
        PlankPart part = state.getValue(PLANK_PART);

        BlockPos basePos = (part == PlankPart.BASE) ? pos : pos.relative(facing.getOpposite());
        BlockState baseState = level.getBlockState(basePos);

        if (baseState.getBlock() instanceof PunjiSticksPlank &&
                baseState.getValue(PLANK_PART) == PlankPart.BASE) {

            boolean isBaseActive = baseState.getValue(PunjiSticksPlank.BASE_ACTIVE);
            boolean isExtActive = baseState.getValue(PunjiSticksPlank.EXTENSION_ACTIVE);

            String anim;

            // 👇 Decidir qué animación reproducir en base al estado actual
            if (isBaseActive) {
                // reset base
                baseState = baseState.setValue(PunjiSticksPlank.BASE_ACTIVE, false);
                anim = "base_reset";
            } else if (isExtActive) {
                // reset extension
                baseState = baseState.setValue(PunjiSticksPlank.EXTENSION_ACTIVE, false);
                anim = "extension_reset";
            } else {
                // Ninguna está activa, activar según el lado usado
                if (part == PlankPart.BASE) {
                    baseState = baseState.setValue(PunjiSticksPlank.BASE_ACTIVE, true);
                    anim = "base_activate";
                } else {
                    baseState = baseState.setValue(PunjiSticksPlank.EXTENSION_ACTIVE, true);
                    anim = "extension_activate";
                }
            }

            // Actualizar estado y animación
            level.setBlock(basePos, baseState, 3);

            BlockEntity be = level.getBlockEntity(basePos);
            if (be instanceof PunjiSticksPlankBE punji) {
                punji.triggerAnim("plank_controller", anim);
                System.out.println("Triggering animation: " + anim);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);

        boolean isOpen = switch (state.getValue(PLANK_PART)) {
            case BASE -> state.getValue(BASE_ACTIVE);
            case EXTENSION -> state.getValue(EXTENSION_ACTIVE);
        };

        if (isOpen) {
            return switch (facing.getAxis()) {
                case X -> SHAPE_OPEN_X;
                case Z -> SHAPE_OPEN_Z;
                default -> SHAPE_CLOSED;
            };
        }

        return SHAPE_CLOSED;
    }

    private static VoxelShape rotateShape(VoxelShape shape, Direction direction) {
        VoxelShape[] buffer = new VoxelShape[] { shape, Shapes.empty() };

        // Rotar en pasos de 90 grados
        int times = switch (direction) {
            case NORTH -> 0;
            case EAST  -> 1;
            case SOUTH -> 2;
            case WEST  -> 3;
            default    -> 0;
        };

        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                buffer[1] = Shapes.or(
                        buffer[1],
                        Shapes.box(
                                1 - maxZ, minY, minX,
                                1 - minZ, maxY, maxX
                        )
                );
            });
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
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

        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            Direction facing = state.getValue(FACING);
            PlankPart part = state.getValue(PLANK_PART);
            BlockPos otherPart = (part == PlankPart.BASE)
                    ? pos.relative(facing)
                    : pos.relative(facing.getOpposite());

            if (level.getBlockState(otherPart).is(this)) {
                level.removeBlock(otherPart, false);
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

        // ¿La dirección en la que debería estar el otro bloque?
        Direction expectedOtherPartDir = getNeighbourDirection(part, state.getValue(FACING));

        if (facing == expectedOtherPartDir) {
            boolean isValidPair = facingState.is(this)
                    && facingState.getValue(PLANK_PART) != part
                    && facingState.getValue(FACING) == state.getValue(FACING);

            return isValidPair ? state : Blocks.AIR.defaultBlockState(); // se rompe si el otro bloque no es válido
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
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PunjiSticksPlankBE(pPos, pState);
    }
}
