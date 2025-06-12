package net.thedragonskull.trapsmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

    public static final RawAnimation SNAP = RawAnimation.begin().thenPlayAndHold("animation.bear_trap.snap");
    public static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("animation.bear_trap.open");
    private String bearTrapLastAnimation = null;

    private UUID owner = null;

    public BearTrapBE(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.BEAR_TRAP_BE.get(), pPos, pBlockState);
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

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (bearTrapLastAnimation != null) {
            tag.putString("trap_last_animation", bearTrapLastAnimation);
        }

        if (owner != null) tag.putUUID("owner", owner);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("trap_last_animation")) {
            bearTrapLastAnimation = tag.getString("trap_last_animation");
        }

        if (tag.hasUUID("owner")) owner = tag.getUUID("owner");
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
