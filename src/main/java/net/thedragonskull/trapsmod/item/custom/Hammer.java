package net.thedragonskull.trapsmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thedragonskull.trapsmod.block.ModBlocks;
import net.thedragonskull.trapsmod.block.custom.CreakingFloorBlock;
import net.thedragonskull.trapsmod.block.custom.properties.CustomWoodType;

import java.util.Random;

import static net.thedragonskull.trapsmod.block.custom.CreakingFloorBlock.WOOD_TYPE;

public class Hammer extends Item {

    public Hammer(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState clickedState = level.getBlockState(pos);
        Player player = pContext.getPlayer();

        if (!level.isClientSide && player != null) {
            CustomWoodType woodType = CreakingFloorBlock.getWoodTypeFromBlock(clickedState.getBlock());
            boolean hasNuggets = (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.IRON_NUGGET) && player.getItemInHand(InteractionHand.MAIN_HAND).getCount() >= 6) ||
                    (player.getItemInHand(InteractionHand.OFF_HAND).is(Items.IRON_NUGGET) && player.getItemInHand(InteractionHand.OFF_HAND).getCount() >= 6);

            if (clickedState.is(BlockTags.PLANKS) && (hasNuggets || player.isCreative())) {

                if (woodType != null) {
                    BlockState newState = ModBlocks.CREAKING_FLOOR.get().defaultBlockState().setValue(WOOD_TYPE, woodType);
                    level.setBlockAndUpdate(pos, newState);
                    level.playSound(null, pos, ModSounds.CREAKING_1.get(), SoundSource.BLOCKS);

                    if (!player.isCreative() && hasNuggets) {
                        removeNuggets(player);
                    }

                    player.getCooldowns().addCooldown(this, 20);

                    pContext.getItemInHand().hurtAndBreak(1, player,
                            player1 -> player.broadcastBreakEvent(player.getUsedItemHand()));

                    return InteractionResult.SUCCESS;
                }

            } else if (clickedState.is(ModBlocks.CREAKING_FLOOR.get())) {
                CustomWoodType currentWoodType = clickedState.getValue(WOOD_TYPE);
                Random random = new Random();
                int nuggetCount = random.nextInt(3) + 1;

                BlockState newState = currentWoodType.getPlanks().defaultBlockState();
                level.setBlockAndUpdate(pos, newState);
                level.playSound(null, pos, SoundEvents.SCAFFOLDING_FALL, SoundSource.BLOCKS);

                if (!player.isCreative()) {
                    for (int i = 0; i < nuggetCount; i++) {
                        ItemEntity ironNuggetEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, new ItemStack(Items.IRON_NUGGET));
                        level.addFreshEntity(ironNuggetEntity);
                    }
                }

                player.getCooldowns().addCooldown(this, 20);

                pContext.getItemInHand().hurtAndBreak(1, player,
                        player1 -> player.broadcastBreakEvent(player.getUsedItemHand()));

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.SUCCESS;
    }

    private void removeNuggets(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(Items.IRON_NUGGET) && stack.getCount() >= 6) {
                stack.shrink(6);
                return;
            }
        }
    }
}
