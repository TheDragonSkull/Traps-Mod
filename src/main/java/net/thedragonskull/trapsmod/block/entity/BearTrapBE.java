package net.thedragonskull.trapsmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.thedragonskull.trapsmod.block.custom.BearTrap;
import net.thedragonskull.trapsmod.util.BearTrapUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class BearTrapBE extends BlockEntity implements GeoBlockEntity {
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int ticksSinceLoad = 0;
    private int redstoneSignal = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public static final RawAnimation SNAP = RawAnimation.begin().thenPlayAndHold("animation.bear_trap.snap");
    public static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("animation.bear_trap.open");
    private String bearTrapLastAnimation = null;

    private UUID owner = null;
    private String ownerName = "Unknown";

    public UUID trappedEntityId;

    public UUID ignoredEntity = null;
    private int ignoreTicks = 0;

    public BearTrapBE(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.BEAR_TRAP_BE.get(), pPos, pBlockState);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        ticksSinceLoad++;

        if (ignoreTicks > 0) ignoreTicks--;
        if (ignoreTicks <= 0) ignoredEntity = null;

        if (trappedEntityId == null) {
            if (this.getBlockState().getValue(BearTrap.TRAP_SET)) {
                ItemStack bait = getTrapItem();
                if (!bait.isEmpty()) {
                    BearTrapUtils.attractNearbyMobs(bait, level, worldPosition);
                }
            }
            return;
        }

        Entity entity = ((ServerLevel) level).getEntity(trappedEntityId);

        // Wait for the player to load
        if (ticksSinceLoad < 20 && (entity == null || entity instanceof ServerPlayer)) return;

        if (!this.getBlockState().getValue(BearTrap.TRAP_SET)) {

            if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
                releaseTrapped();
                return;
            }

            // Stop movement and tp
            Vec3 velocity = living.getDeltaMovement();
            double yMotion = velocity.y < 0 ? velocity.y : 0.0;
            living.setDeltaMovement(0.0, yMotion, 0.0);

            double centerX = worldPosition.getX() + 0.5;
            double centerY = worldPosition.getY() + 0.01;
            double centerZ = worldPosition.getZ() + 0.5;
            living.teleportTo(centerX, centerY, centerZ);
            living.makeStuckInBlock(this.getBlockState(), new Vec3(0.0D, 0.01D, 0.0D));
            living.hurtMarked = true;

        } else {
            releaseTrapped();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "bear_trap_controller", state -> {

            if (bearTrapLastAnimation != null) {
                if (bearTrapLastAnimation.equals("bear_trap_open")) {
                    state.setAnimation(OPEN);
                    return PlayState.CONTINUE;
                } else if (bearTrapLastAnimation.equals("bear_trap_snap")) {
                    state.setAnimation(SNAP);
                    return PlayState.CONTINUE;
                }
            }

            return PlayState.STOP;
        }).triggerableAnim("bear_trap_open", OPEN).triggerableAnim("bear_trap_snap", SNAP));
    }

    public void setAndTrigger(String animName) {
        this.bearTrapLastAnimation = animName;
        triggerAnim("bear_trap_controller", animName);

        if (this.level != null && !this.level.isClientSide) {
            setChanged();
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwnerName(String name) {
        this.ownerName = name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void trapEntity(LivingEntity entity) {
        this.trappedEntityId = entity.getUUID();
        this.ignoreTicks = 40;
        setChanged();
    }

    public void releaseTrapped() {
        if (trappedEntityId != null && level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(trappedEntityId);
            if (entity instanceof Mob mob) {
                CompoundTag tag = new CompoundTag();
                mob.saveWithoutId(tag);
                tag.remove("PersistenceRequired");
                mob.load(tag);
            }
        }

        this.trappedEntityId = null;
    }


    public void ignoreEntity(UUID uuid, int ticks) {
        this.ignoredEntity = uuid;
        this.ignoreTicks = ticks;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ItemStack getTrapItem() {
        return itemHandler.getStackInSlot(0);
    }

    public int getRedstoneSignal() {
        return redstoneSignal;
    }

    public void setRedstoneSignal(int signal) {
        this.redstoneSignal = signal;
        setChanged();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (bearTrapLastAnimation != null) {
            tag.putString("trap_last_animation", bearTrapLastAnimation);
        }

        if (trappedEntityId != null) {
            tag.putUUID("TrappedEntity", trappedEntityId);
        }

        tag.putInt("IgnoreTicks", ignoreTicks);

        if (owner != null) tag.putUUID("owner", owner);

        tag.putString("ownerName", ownerName);

        tag.put("trapItem", itemHandler.serializeNBT());

        tag.putInt("RedstoneSignal", redstoneSignal);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.hasUUID("TrappedEntity")) {
            this.trappedEntityId = tag.getUUID("TrappedEntity");
        }

        if (tag.contains("trap_last_animation")) {
            bearTrapLastAnimation = tag.getString("trap_last_animation");
        }

        ignoreTicks = tag.getInt("IgnoreTicks");

        if (tag.hasUUID("owner")) owner = tag.getUUID("owner");

        if (tag.contains("ownerName")) ownerName = tag.getString("ownerName");

        itemHandler.deserializeNBT(tag.getCompound("trapItem"));

        redstoneSignal = tag.getInt("RedstoneSignal");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
