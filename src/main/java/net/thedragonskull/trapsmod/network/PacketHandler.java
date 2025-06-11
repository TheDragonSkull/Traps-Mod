package net.thedragonskull.trapsmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.thedragonskull.trapsmod.TrapsMod;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int id = 0;

    public static void register() {

        INSTANCE.messageBuilder(C2SCreakingFloorSoundAndSignalPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SCreakingFloorSoundAndSignalPacket::encode)
                .decoder(C2SCreakingFloorSoundAndSignalPacket::new)
                .consumerMainThread(C2SCreakingFloorSoundAndSignalPacket::handle)
                .add();
    }


    public static void sendToServer(Object msg) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToAllPlayer(Object msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}
