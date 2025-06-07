package net.thedragonskull.trapsmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thedragonskull.trapsmod.util.StructureUtils;

import static net.thedragonskull.trapsmod.util.StructureUtils.triggerFallingCage;

public class StrongChainBlock extends ChainBlock {

    public StrongChainBlock(Properties props) {
        super(props);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            BlockPos base = pos.below(4); // Centro de la estructura

            triggerFallingCage(level, base);

            if (StructureUtils.isCageTrapStructure(level, base)) { //TODO: al quitar el chain ya no puede detectar el lvl 4
                triggerFallingCage(level, base); //TODO: El bloque de copper del centro acaba cayendo al fondo, cambiar layout y que el copper block no sea falling block?
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
                    player.displayClientMessage(Component.literal("❌ No hay soporte sólido encima."), false);
                }
                return;
            }

            BlockPos below = pos.below(4); // Centro de la estructura esperada
            if (StructureUtils.isCageTrapStructure(level, below)) {
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
