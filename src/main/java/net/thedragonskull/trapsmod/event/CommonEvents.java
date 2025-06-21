package net.thedragonskull.trapsmod.event;

import net.minecraft.ChatFormatting;
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

        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!player.getUUID().equals(trap.trappedEntityId)) return;

        event.setCanceled(true);

        player.displayClientMessage(
                Component.literal("Every attempt to move only intensifies the pain. You can only break the trap.")
                        .withStyle(ChatFormatting.DARK_RED), true
        );

        player.hurt(level.damageSources().generic(), 1.0F);
    }

}
