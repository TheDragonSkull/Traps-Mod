package net.thedragonskull.trapsmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;
import net.thedragonskull.trapsmod.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

public class BearTrap extends BaseEntityBlock {
    public static final BooleanProperty TRAP_SET = BooleanProperty.create("trap_set");

    public BearTrap(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(TRAP_SET, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(TRAP_SET);
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
                    bearTrap.setAndTrigger("bear_trap_snap");
                    level.setBlock(pos, trapState.setValue(TRAP_SET, false), 3);

                    level.playSound(null, pos, ModSounds.BEAR_TRAP_SNAP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return InteractionResult.SUCCESS;
            }
        }


        return InteractionResult.PASS;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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
