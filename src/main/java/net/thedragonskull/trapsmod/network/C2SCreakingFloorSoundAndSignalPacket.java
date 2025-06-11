package net.thedragonskull.trapsmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.trapsmod.sound.ModSounds;

import java.util.function.Supplier;

import static net.thedragonskull.trapsmod.block.custom.CreakingFloorBlock.OUTPUT_POWER;

public class C2SCreakingFloorSoundAndSignalPacket {
    private final BlockPos pos;

    public C2SCreakingFloorSoundAndSignalPacket(BlockPos pos) {
        this.pos = pos;
    }

    public C2SCreakingFloorSoundAndSignalPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static void handle(C2SCreakingFloorSoundAndSignalPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

            if (player == null)
                return;

            ServerLevel level = player.serverLevel();
            BlockState state = level.getBlockState(msg.pos);
            RandomSource random = RandomSource.create();
            float pitch = 0.8f + random.nextFloat() * 0.4f;
            SoundEvent creakSound = random.nextBoolean() ? ModSounds.CREAKING_1.get() : ModSounds.CREAKING_2.get();

            level.setBlock(msg.pos, level.getBlockState(msg.pos).setValue(OUTPUT_POWER, 15), 3);
            level.updateNeighborsAt(msg.pos, state.getBlock());
            level.scheduleTick(msg.pos, state.getBlock(), 8);

            level.playSound(null, msg.pos, creakSound, SoundSource.BLOCKS, 0.75f, pitch);

        });
        ctx.get().setPacketHandled(true);
    }


}
