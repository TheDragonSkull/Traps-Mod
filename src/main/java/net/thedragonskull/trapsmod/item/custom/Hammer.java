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
import net.thedragonskull.trapsmod.sound.ModSounds;

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
            ItemStack nuggets = findNuggets(player);
            boolean hasEnoughNuggets = !nuggets.isEmpty();

            if (clickedState.is(BlockTags.PLANKS) && (hasEnoughNuggets || player.isCreative())) {
                if (woodType != null) {
                    BlockState newState = ModBlocks.CREAKING_FLOOR.get().defaultBlockState().setValue(WOOD_TYPE, woodType);
                    level.setBlockAndUpdate(pos, newState);
                    level.playSound(null, pos, ModSounds.CREAKING_1.get(), SoundSource.BLOCKS);

                    if (!player.isCreative() && hasEnoughNuggets) {
                        nuggets.shrink(6);
                    }

                    player.getCooldowns().addCooldown(this, 20);

                    pContext.getItemInHand().hurtAndBreak(1, player,
                            player1 -> player.broadcastBreakEvent(player.getUsedItemHand()));

                    return InteractionResult.SUCCESS;
                }

            } else if (clickedState.is(ModBlocks.CREAKING_FLOOR.get())) {
                CustomWoodType currentWoodType = clickedState.getValue(WOOD_TYPE);
                int nuggetCount = pContext.getLevel().random.nextInt(3) + 1;

                BlockState newState = currentWoodType.getPlanks().defaultBlockState();
                level.setBlockAndUpdate(pos, newState);
                level.playSound(null, pos, SoundEvents.SCAFFOLDING_FALL, SoundSource.BLOCKS);

                if (!player.isCreative()) {
                    for (int i = 0; i < nuggetCount; i++) {
                        ItemEntity ironNuggetEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                                new ItemStack(Items.IRON_NUGGET));
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

    private ItemStack findNuggets(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(Items.IRON_NUGGET) && stack.getCount() >= 6) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
