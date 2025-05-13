package net.serex.itemmodifiers;


import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SyncModifierPacket {
    private final int slot;
    private final String modifier;

    // Constructor del paquete (datos a enviar)
    public SyncModifierPacket(int slot, String modifier) {
        this.slot = slot;
        this.modifier = modifier;
    }

    // Decodificador: leer los datos del buffer (constructor alternativo)
    public SyncModifierPacket(FriendlyByteBuf buf) {
        this.slot = buf.readInt();
        this.modifier = buf.readUtf();  // Leer string
    }

    // Codificador: escribir los datos en el buffer para envío
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(slot);
        buf.writeUtf(modifier);
    }

    // Decodificador estático (opcional, equivalente al constructor que lee del buffer)
    public static SyncModifierPacket decode(FriendlyByteBuf buf) {
        return new SyncModifierPacket(buf);
    }

    // Manejador del paquete al recibirse (lado CLIENTE)
    public static void handle(SyncModifierPacket message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            // Encolar la tarea en el hilo de cliente (thread seguro)
            context.enqueueWork(() -> {
                // Ejecutar solo en el cliente físico
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    applyModifierClient(message.slot, message.modifier);
                });
            });
        }
        context.setPacketHandled(true);
    }

    // Aplicar el NBT del modificador al ItemStack del cliente en el slot dado

    private static void applyModifierClient(int slot, String modifier) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getInventory().getItem(slot);
        if (!stack.isEmpty()) {
            // Escribir el NBT "itemmodifiers:modifier" en el ItemStack del cliente
            stack.getOrCreateTag().putString("itemmodifiers:modifier", modifier);
            player.sendSystemMessage(Component.literal("[DEBUG] Sync → " + modifier + " en slot " + slot));

        }
    }
}
