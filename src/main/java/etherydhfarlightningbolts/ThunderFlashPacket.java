package etherydhfarlightningbolts;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class ThunderFlashPacket {
    public final double x, y, z;
    
    // The client-side queue for DH rendering
    public static final Queue<ThunderFlashPacket> pendingBolts = new ConcurrentLinkedQueue<>();

    public ThunderFlashPacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(ThunderFlashPacket pkt, FriendlyByteBuf buf) {
        buf.writeDouble(pkt.x);
        buf.writeDouble(pkt.y);
        buf.writeDouble(pkt.z);
    }

    public static ThunderFlashPacket decode(FriendlyByteBuf buf) {
        return new ThunderFlashPacket(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(ThunderFlashPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Add to the queue on the client thread when the packet arrives
            pendingBolts.add(pkt);
        });
        ctx.get().setPacketHandled(true);
    }
}