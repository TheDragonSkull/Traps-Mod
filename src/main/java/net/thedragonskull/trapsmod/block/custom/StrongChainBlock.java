package net.thedragonskull.trapsmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thedragonskull.trapsmod.block.ModBlocks;
import net.thedragonskull.trapsmod.util.CageTrapUtils;

public class StrongChainBlock extends ChainBlock {

    public StrongChainBlock(Properties props) {
        super(props);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            BlockPos base = pos.below(4);
            BlockPos above = pos.above();

            if (level.getBlockState(above).isSolidRender(level, above) &&
                    CageTrapUtils.isCageTrapStructure(level, base, false, state)) {

                BlockPos fencePos = base.above(3);
                if (level.getBlockState(fencePos).is(BlockTags.FENCES)) {
                    level.destroyBlock(fencePos, false);
                }

                level.setBlock(base.above(2), ModBlocks.CAGE_TRAP_TICKER.get().defaultBlockState(), 3);
                //TODO: comprobar moviendo chain con piston a ver si el ticker quita el piston // ponerlo en el aire central de la cage
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide) {

            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);

            if (!aboveState.isSolidRender(level, above)) {
                if (placer instanceof Player player) {
                    player.displayClientMessage(Component.literal("❌ No hay soporte sólido encima."), false); //TODO: translatables
                }
                return;
            }

            BlockPos below = pos.below(4); // Centro de la estructura esperada
            if (CageTrapUtils.isCageTrapStructure(level, below, false, null)) {
                if (placer instanceof Player player) {
                    player.displayClientMessage(Component.literal("✅ ¡Trampa de jaula detectada!"), false);
                }
            } else {
                if (placer instanceof Player player) {
                    player.displayClientMessage(Component.literal("❌ Estructura inválida."), false);
                }
            }
        }
    }

}
