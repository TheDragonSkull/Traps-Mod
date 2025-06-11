package net.thedragonskull.trapsmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.thedragonskull.trapsmod.block.custom.properties.CustomWoodType;

import java.util.HashMap;
import java.util.Map;

public class CreakingFloorBlock extends Block {
    public static final EnumProperty<CustomWoodType> WOOD_TYPE = EnumProperty.create("wood_type", CustomWoodType.class);
    public static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final Map<BlockPos, Long> lastStepSoundTime = new HashMap<>();

    private boolean isPowered = false;

    public CreakingFloorBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WOOD_TYPE, CustomWoodType.OAK)
                .setValue(OUTPUT_POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(WOOD_TYPE, OUTPUT_POWER, POWERED);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (fallDistance > 0.1f) {
            playCreakingSound(level, pos, entity, true);
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {

        boolean entityMoving = entity.xOld != entity.getX() || entity.zOld != entity.getZ();

        if (entityMoving) {
            playCreakingSound(level, pos, entity, false);
        }

        super.stepOn(level, pos, state, entity);
    }

    private void playCreakingSound(Level level, BlockPos pos, Entity entity, boolean forcePlay) {
        long gameTime = level.getGameTime();
        long lastStepTime = lastStepSoundTime.getOrDefault(pos, 0L);

        if (forcePlay || gameTime - lastStepTime > 40) {
            RandomSource random = RandomSource.create();
            float pitch = 0.8f + random.nextFloat() * 0.4f;
            SoundEvent creakSound = random.nextBoolean() ? ModSounds.CREAKING_1.get() : ModSounds.CREAKING_2.get();

            if (entity instanceof Player) {
                PacketHandler.sendToServer(new C2SCreakingFloorSoundAndSignalPacket(pos));
            } else if (entity instanceof ItemEntity) {
                level.playSound(null, pos, creakSound, SoundSource.BLOCKS, 0.5f, pitch);
                tryEmitRedstone(level, pos, entity);
            } else {
                level.playSound(null, pos, creakSound, SoundSource.BLOCKS, 1.0f, pitch);
                tryEmitRedstone(level, pos, entity);
            }


            lastStepSoundTime.put(pos, gameTime);

        }
    }

    private void tryEmitRedstone(Level level, BlockPos pos, Entity entity) {
        int power = (entity instanceof ItemEntity) ? 7 : 15;
        level.setBlock(pos, level.getBlockState(pos).setValue(OUTPUT_POWER, power), 3);
        level.updateNeighborsAt(pos, this);
        level.scheduleTick(pos, this, 8);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pState.getValue(OUTPUT_POWER) != 0) {
            pLevel.setBlock(pPos, pState.setValue(OUTPUT_POWER, 0), 3);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            boolean isCurrentlyPowered = state.getValue(POWERED);

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighbor);

                if (level.getSignal(neighbor, direction.getOpposite()) > 0) {
                    if (neighborState.getBlock() instanceof CreakingFloorBlock) {
                        return;
                    }
                }
            }

            if (hasSignal && !isCurrentlyPowered) {
                RandomSource random = RandomSource.create();
                float pitch = 0.8f + random.nextFloat() * 0.4f;
                SoundEvent creakSound = random.nextBoolean() ? ModSounds.CREAKING_1.get() : ModSounds.CREAKING_2.get();
                level.playSound(null, pos, creakSound, SoundSource.BLOCKS, 1.0f, pitch);

                level.setBlock(pos, state.setValue(POWERED, true), 3);

            } else if (!hasSignal && isCurrentlyPowered) {
                level.setBlock(pos, state.setValue(POWERED, false), 3);
            }
        }
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return pState.getValue(OUTPUT_POWER);
    }

    public static CustomWoodType getWoodTypeFromBlock(Block block) {
        if (block == Blocks.OAK_PLANKS) return CustomWoodType.OAK;
        if (block == Blocks.SPRUCE_PLANKS) return CustomWoodType.SPRUCE;
        if (block == Blocks.BIRCH_PLANKS) return CustomWoodType.BIRCH;
        if (block == Blocks.JUNGLE_PLANKS) return CustomWoodType.JUNGLE;
        if (block == Blocks.ACACIA_PLANKS) return CustomWoodType.ACACIA;
        if (block == Blocks.DARK_OAK_PLANKS) return CustomWoodType.DARK_OAK;
        if (block == Blocks.BAMBOO_PLANKS) return CustomWoodType.BAMBOO;
        if (block == Blocks.CHERRY_PLANKS) return CustomWoodType.CHERRY;
        if (block == Blocks.MANGROVE_PLANKS) return CustomWoodType.MANGROVE;
        if (block == Blocks.CRIMSON_PLANKS) return CustomWoodType.CRIMSON;
        if (block == Blocks.WARPED_PLANKS) return CustomWoodType.WARPED;

        return CustomWoodType.OAK;
    }

}
