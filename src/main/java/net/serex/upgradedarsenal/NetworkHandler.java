package net.serex.upgradedarsenal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {

    public static final String MODID = "upgradedarsenal";
    private static final String PROTOCOL_VERSION = "1";
    // Crear el canal de red con un nombre único (modid:canal)
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    public static void register() {
        // Registrar el paquete de sincronización en el canal
        CHANNEL.registerMessage(packetId++, SyncModifierPacket.class,
                SyncModifierPacket::encode,
                SyncModifierPacket::decode,
                SyncModifierPacket::handle
                // Podríamos especificar la dirección opcionalmente:
                // , Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    /** Utilidad para enviar el paquete a un jugador específico (servidor a cliente). */
    public static void sendToPlayer(ServerPlayer player, SyncModifierPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
