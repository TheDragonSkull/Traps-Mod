package net.thedragonskull.trapsmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thedragonskull.trapsmod.TrapsMod;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;

@Mod.EventBusSubscriber(modid = TrapsMod.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        BlockPos brokenPos = event.getPos();

        BlockPos possibleTrapAbove = brokenPos.above();
        BlockEntity be = level.getBlockEntity(possibleTrapAbove);

        if (!(be instanceof BearTrapBE trap)) return;
        if (!trap.isTrappingEntity()) return;

        event.setCanceled(true);

        if (level instanceof ServerLevel && event.getPlayer() instanceof ServerPlayer player) {
            player.displayClientMessage(
                    Component.literal("§cEvery attempt to move only intensifies the pain. You can only break the trap."),
                    true
            );

            // Aplica medio corazón de daño
            player.hurt(level.damageSources().generic(), 1.0F);
        }
    }

}
