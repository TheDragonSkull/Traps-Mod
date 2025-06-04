package net.thedragonskull.trapsmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.thedragonskull.trapsmod.block.custom.properties.PlankPart;
import net.thedragonskull.trapsmod.block.entity.PunjiSticksPlankBE;
import org.jetbrains.annotations.Nullable;

public class PunjiSticksPlank extends HorizontalDirectionalBlock implements EntityBlock {

    public static final EnumProperty<PlankPart> PLANK_PART = EnumProperty.create("plank_part", PlankPart.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public PunjiSticksPlank(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PLANK_PART, PlankPart.BASE));
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PLANK_PART, ACTIVE);
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

            boolean current = baseState.getValue(ACTIVE);
            BlockState newState = baseState.setValue(ACTIVE, !current);
            level.setBlock(basePos, newState, 3);

            BlockEntity be = level.getBlockEntity(basePos);
            if (be instanceof PunjiSticksPlankBE punji) {
                punji.setLastActivatedPart(part);
                punji.triggerAnim("controller", !current ? "activate" : "reset");
            }

        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockPos offsetPos = pos.relative(facing);

        BlockState baseState = this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PLANK_PART, PlankPart.BASE);

        BlockState extensionState = this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PLANK_PART, PlankPart.EXTENSION);

        level.setBlock(offsetPos, extensionState, 3); // lado secundario
        return baseState;
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
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PunjiSticksPlankBE(pPos, pState);
    }
}
