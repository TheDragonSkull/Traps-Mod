package net.thedragonskull.trapsmod.trap_variants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;
import net.thedragonskull.trapsmod.util.ModTags;

import java.util.UUID;

public class ModBearTrapVariants {

    public static void registerVariant() {

        // TNT
        BearTrapVariantRegistry.register(Blocks.TNT.asItem(), (level, pos, entity) -> {
            Vec3 explosionPos = Vec3.atCenterOf(pos);
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof BearTrapBE bearTrap) {
                level.explode(
                        null,
                        level.damageSources().explosion(null),
                        null,
                        explosionPos,
                        1.2F,
                        false,
                        Level.ExplosionInteraction.BLOCK
                );

                if (!level.isClientSide) { //todo: lo ven todos?
                    ((ServerLevel) level).sendParticles(
                            ParticleTypes.FLAME,
                            explosionPos.x,
                            explosionPos.y,
                            explosionPos.z,
                            20,
                            0.2, 0.2, 0.2,
                            0.4
                    );
                    ((ServerLevel) level).sendParticles(
                            ParticleTypes.SMOKE,
                            explosionPos.x,
                            explosionPos.y,
                            explosionPos.z,
                            20,
                            0.2, 0.2, 0.2,
                            0.4
                    );
                }

                bearTrap.getItemHandler().extractItem(0, 1, false);
                level.destroyBlock(pos, false);
            }
        });

        // FIREWORKS
        BearTrapVariantRegistry.register(Items.FIREWORK_ROCKET, (level, pos, entity) -> {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof BearTrapBE bearTrap) {
                ItemStack trapItem = bearTrap.getTrapItem();
                ItemStack firework = trapItem.copy();
                Vec3 center = Vec3.atCenterOf(pos);

                FireworkRocketEntity rocket = new FireworkRocketEntity(
                        level,
                        firework,
                        center.x, center.y + 0.2, center.z,
                        false
                );
                level.addFreshEntity(rocket);
            }
        });

        // FIRE CHARGE
        BearTrapVariantRegistry.register(Items.FIRE_CHARGE, (level, pos, entity) -> {
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            RandomSource random = level.getRandom();

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

            if (entity != null) {
                entity.setSecondsOnFire(5);
            }

            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        });

        // REDSTONE
        BearTrapVariantRegistry.register(Items.REDSTONE_BLOCK, ((level, pos, entity) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BearTrapBE bearTrap) {
                int signal = 5;
                if (entity != null) {
                    bearTrap.trapEntity(entity);
                    if (entity instanceof Player) {
                        signal = 15;
                    } else {
                        signal = 10;
                    }
                }

                bearTrap.setRedstoneSignal(signal);
                level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
            }
        }));

        // LIGHTNING ROD
        BearTrapVariantRegistry.register(Items.LIGHTNING_ROD, (level, pos, entity) -> {
            LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(level);

            if (lightningbolt != null && (level.isThundering() || level.isRaining())) {
                lightningbolt.moveTo(Vec3.atBottomCenterOf(pos));
                level.addFreshEntity(lightningbolt);
            }

        });

        // FOOD
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item.builtInRegistryHolder().is(ModTags.Items.TEMPT_ITEMS)) {
                BearTrapVariantRegistry.register(item, (level, pos, entity) -> {});
            }
        }

        // ENDER PEARL
        BearTrapVariantRegistry.register(Items.ENDER_PEARL, ((level, pos, entity) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof BearTrapBE bearTrap)) return;

            UUID ownerId = bearTrap.getOwner();
            if (!(level instanceof ServerLevel serverLevel) || ownerId == null) return;

            Entity entityOwner = serverLevel.getEntity(ownerId);
            if (!(entityOwner instanceof ServerPlayer player)) return;

            BlockPos[] offsets = {
                    pos.north(), pos.south(), pos.east(), pos.west(),
                    pos.north().east(), pos.north().west(), pos.south().east(), pos.south().west()
            };

            for (BlockPos target : offsets) {
                if (level.isEmptyBlock(target) && level.isEmptyBlock(target.above())) {
                    Vec3 spawnPos = Vec3.atCenterOf(target.above());
                    ThrownEnderpearl pearl = new ThrownEnderpearl(level, player);

                    pearl.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                    pearl.setDeltaMovement(Vec3.ZERO);

                    level.playSound(null, pos, SoundEvents.ENDER_PEARL_THROW, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.addFreshEntity(pearl);
                    break;
                }
            }
        }));

        // POTIONS
        BearTrapVariantRegistry.register(Items.SPLASH_POTION, (level, pos, entity) -> throwPotion(level, pos));
        BearTrapVariantRegistry.register(Items.LINGERING_POTION, (level, pos, entity) -> throwPotion(level, pos));
    }

    private static void throwPotion(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BearTrapBE bearTrap)) return;

        ItemStack potionStack = bearTrap.getTrapItem();
        if (potionStack.isEmpty()) return;

        Vec3 center = Vec3.atCenterOf(pos);
        double yOffset = potionStack.is(Items.LINGERING_POTION) ? -0.5 : 0.5;

        ThrownPotion potionEntity = new ThrownPotion(serverLevel, center.x, center.y + yOffset, center.z);
        potionEntity.setItem(potionStack.copy());

        potionEntity.setDeltaMovement(0, 0.5, 0);

        serverLevel.addFreshEntity(potionEntity);
    }




}
