package net.thedragonskull.trapsmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thedragonskull.trapsmod.util.CageTrapUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CageTrapTickerBlockEntity extends BlockEntity {

    private int tickCounter = 0;
    private int currentLayer = 0;

    public CageTrapTickerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAGE_TRAP_TICKER_BE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;

        if (tickCounter >= 4) {
            tickCounter = 0;
            CageTrapUtils.dropCageLayer(level, worldPosition.below(4), currentLayer);
            currentLayer++;

            if (currentLayer > 3) {
                level.removeBlock(worldPosition, false);
            }
        }
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

}
