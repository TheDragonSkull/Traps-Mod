package net.thedragonskull.trapsmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.thedragonskull.trapsmod.block.custom.PunjiSticksPlank;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;


public class PunjiSticksPlankBE extends BlockEntity implements GeoBlockEntity {
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final RawAnimation ROTATE_BASE_ACTIVATE = RawAnimation.begin().thenPlayAndHold("animation.punji.base_activate");
    public static final RawAnimation ROTATE_BASE_RESET = RawAnimation.begin().thenPlayAndHold("animation.punji.base_reset");
    public static final RawAnimation ROTATE_EXT_ACTIVATE = RawAnimation.begin().thenPlayAndHold("animation.punji.extension_activate");
    public static final RawAnimation ROTATE_EXT_RESET = RawAnimation.begin().thenPlayAndHold("animation.punji.extension_reset");

    public PunjiSticksPlankBE(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.PUNJI_STICKS_PLANK_BE.get(), pPos, pBlockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        Direction facing = this.getBlockState().getValue(PunjiSticksPlank.FACING);
        BlockPos pos = this.getBlockPos();
        BlockPos other = pos.relative(facing);

        return new AABB(pos).minmax(new AABB(other));
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "plank_controller", state -> PlayState.CONTINUE)
                .triggerableAnim("base_activate", ROTATE_BASE_ACTIVATE)
                .triggerableAnim("base_reset", ROTATE_BASE_RESET)
                .triggerableAnim("extension_activate", ROTATE_EXT_ACTIVATE)
                .triggerableAnim("extension_reset", ROTATE_EXT_RESET)
        );
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
