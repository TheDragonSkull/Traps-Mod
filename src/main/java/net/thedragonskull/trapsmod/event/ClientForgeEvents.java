package net.thedragonskull.trapsmod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thedragonskull.trapsmod.TrapsMod;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;

import java.util.List;

@Mod.EventBusSubscriber(modid = TrapsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onDebugTextRender(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();

        if (!(mc.hitResult instanceof BlockHitResult blockHit)) return;

        BlockPos pos = blockHit.getBlockPos();
        Level level = mc.level;

        if (level != null && level.getBlockEntity(pos) instanceof BearTrapBE trap) {
            String ownerName = trap.getOwnerName();
            List<String> right = event.getRight();

            // Search for the line that starts with "trapsmod" in the right
            for (int i = 0; i < right.size(); i++) {
                String line = right.get(i);
                if (line.startsWith("trapsmod")) {
                    right.add(i + 1, "owner: " + ownerName); // add this line
                    break;
                }
            }
        }
    }


}
