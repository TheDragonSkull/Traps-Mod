package net.thedragonskull.trapsmod.trap_variants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;

public class TrapTemptGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;
    private final Ingredient temptItems;
    private BlockPos targetTrap;

    public TrapTemptGoal(PathfinderMob mob, double speed, Ingredient temptItems) {
        this.mob = mob;
        this.speed = speed;
        this.temptItems = temptItems;
    }

    @Override
    public boolean canUse() {
        targetTrap = findNearbyTrap();
        return targetTrap != null;
    }

    @Override
    public void start() {
        if (targetTrap != null) {
            mob.getNavigation().moveTo(targetTrap.getX() + 0.5, targetTrap.getY(), targetTrap.getZ() + 0.5, speed);
        }
    }

    private BlockPos findNearbyTrap() {
        BlockPos pos = mob.blockPosition();
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-8, -2, -8), pos.offset(8, 2, 8))) {
            BlockEntity be = mob.level().getBlockEntity(nearby);
            if (be instanceof BearTrapBE trap && temptItems.test(trap.getTrapItem())) {
                return nearby.immutable();
            }
        }
        return null;
    }
}

