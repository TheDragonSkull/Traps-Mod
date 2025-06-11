package net.thedragonskull.trapsmod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.thedragonskull.trapsmod.block.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void trapsmod$replaceBambooWithSharpened(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (!level.isClientSide && state.is(Blocks.BAMBOO)) {
            int age = 0;
            if (state.hasProperty(BlockStateProperties.AGE_1)) {
                age = state.getValue(BlockStateProperties.AGE_1);
            }

            Direction randomFacing = Direction.Plane.HORIZONTAL.getRandomDirection(level.random);

            BlockState sharpened = ModBlocks.SHARPENED_BAMBOO.get()
                    .defaultBlockState()
                    .setValue(BlockStateProperties.AGE_1, age)
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, randomFacing);

            level.setBlock(pos, sharpened, 3);
            level.playSound(null, pos, SoundEvents.BAMBOO_WOOD_FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, sharpened));

            ItemStack stack = context.getItemInHand();
            if (player != null) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            }

            cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }



}
