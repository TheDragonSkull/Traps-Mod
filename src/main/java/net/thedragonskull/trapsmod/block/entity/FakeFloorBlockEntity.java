package net.thedragonskull.trapsmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FakeFloorBlockEntity extends BlockEntity {
    private int presenceTicks = 0;
    private boolean entityPresent = false;

    public FakeFloorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.FAKE_FLOOR_BE.get(), pPos, pBlockState);
    }

    public void setEntityPresent(boolean present) {
        this.entityPresent = present;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (entityPresent) {
            presenceTicks++;

            if (presenceTicks >= 6) {
                level.destroyBlock(getBlockPos(), false);
            }
        } else {
            presenceTicks = 0;
        }

        entityPresent = false;
    }
}
