package net.thedragonskull.trapsmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemStackHandler;
import net.thedragonskull.trapsmod.block.custom.BearTrap;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;
import net.thedragonskull.trapsmod.sound.ModSounds;

import javax.annotation.Nullable;
import java.util.Random;

import static net.thedragonskull.trapsmod.block.custom.BearTrap.BURIED;
import static net.thedragonskull.trapsmod.block.custom.BearTrap.TRAP_SET;

public class BearTrapUtils {

    public static VoxelShape rotateShapeFrom(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[] { shape, Shapes.empty() };
        int times = (from.get2DDataValue() - to.get2DDataValue() + 4) % 4;

        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                buffer[1] = Shapes.or(buffer[1],
                        Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX));
            });
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }

    public static void trapSnap(Level level, BlockPos pos, @Nullable LivingEntity trappedEntity) {
        BlockEntity be = level.getBlockEntity(pos);
        BlockState trapState = level.getBlockState(pos);

        if (be instanceof BearTrapBE bearTrap) {

            if (trappedEntity != null) {
                bearTrap.trapEntity(trappedEntity);
            }

            bearTrap.setAndTrigger("bear_trap_snap");
            level.setBlock(pos, trapState.setValue(BURIED, false).setValue(TRAP_SET, false), 3);
            ItemStack trapItem = bearTrap.getTrapItem();

            if (!trapItem.isEmpty()) {
                if (trapItem.is(Blocks.TNT.asItem())) {
                    Vec3 explosionPos = Vec3.atCenterOf(pos);
                    level.explode(
                            null,
                            level.damageSources().explosion(null),
                            null,
                            explosionPos,
                            1.2F,
                            false,
                            Level.ExplosionInteraction.BLOCK
                    );

                    bearTrap.getItemHandler().extractItem(0, 1, false);
                    level.destroyBlock(pos, false);

                } else if (trapItem.is(Items.FIREWORK_ROCKET)) {
                    ItemStack firework = trapItem.copy();
                    Vec3 center = Vec3.atCenterOf(pos);

                    FireworkRocketEntity fireworkEntity = new FireworkRocketEntity(
                            level,
                            firework,
                            center.x, center.y + 0.2, center.z,
                            false
                    );

                    level.addFreshEntity(fireworkEntity);

                } else if (trapItem.is(Items.FIRE_CHARGE)) {
                    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
                    RandomSource random = level.getRandom();

                    // 5x5 area
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            if (random.nextFloat() < 0.5f) continue;

                            mutablePos.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                            BlockState targetState = level.getBlockState(mutablePos);

                            if (targetState.isAir() && Blocks.FIRE.defaultBlockState().canSurvive(level, mutablePos)) {
                                level.setBlock(mutablePos, Blocks.FIRE.defaultBlockState(), 11);
                            }
                        }
                    }

                    if (trappedEntity != null) {
                        trappedEntity.setSecondsOnFire(5);
                    }

                    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }


                bearTrap.getItemHandler().extractItem(0, 1, false);
            }

            level.playSound(null, pos, ModSounds.BEAR_TRAP_SNAP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }

    }

    public static void trapSet(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        BlockState trapState = level.getBlockState(pos);

        if (be instanceof BearTrapBE bearTrap) {
            bearTrap.setAndTrigger("bear_trap_open");
            level.setBlock(pos, trapState.setValue(TRAP_SET, true), 3);

            level.playSound(null, pos, ModSounds.BEAR_TRAP_SET.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }

    }

    public static void interact(Player player, Level level, BlockPos pos, InteractionHand hand, ItemStackHandler itemHandler) { //todo: sonidos
        ItemStack heldItem = player.getItemInHand(hand);
        int selectedSlot = player.getInventory().selected;

        if (level == null)
            return;

        // Put item
        if (itemHandler.getStackInSlot(0).isEmpty() && isValidTrapItem(heldItem)) {
            ItemStack stack = heldItem.copy();
            stack.setCount(1);
            itemHandler.setStackInSlot(0, stack);
            heldItem.shrink(1);
            level.playSound(null, pos,
                    SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS);

            // Retrieve item
        } else if (!itemHandler.getStackInSlot(0).isEmpty() && heldItem.isEmpty()) {
            player.getInventory().setItem(selectedSlot, itemHandler.getStackInSlot(0));
            itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            level.playSound(null, pos,
                    SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.BLOCKS);

            // Replace item
        } else if (!itemHandler.getStackInSlot(0).isEmpty() && isValidTrapItem(heldItem)) {
            ItemStack oldItem = itemHandler.getStackInSlot(0).copy();
            ItemStack newItem = heldItem.copy();
            newItem.setCount(1);

            itemHandler.setStackInSlot(0, newItem);
            heldItem.shrink(1);

            if (!player.getInventory().add(oldItem)) {
                player.drop(oldItem, false);
            }

            level.playSound(null, pos,
                    SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS);
        }
    }

    public static boolean isValidTrapItem(ItemStack stack) {
        return stack.is(ModTags.Items.VALID_TRAP_ITEMS);
    }

}
