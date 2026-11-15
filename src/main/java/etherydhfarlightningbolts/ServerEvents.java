package etherydhfarlightningbolts;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LightningBolt;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = EtheryDhFarlightningBoltMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    @SubscribeEvent
public static void onEntityJoin(EntityJoinLevelEvent event) {
    if (event.getLevel().isClientSide) return;
    if (!(event.getEntity() instanceof LightningBolt)) return;
    if (!EtheryConfig.ENABLED.get()) return;

    if (EtheryDhFarlightningBoltMod.DH_CHANNEL == null) {
        System.out.println("DH MOD ERROR: DH_CHANNEL is null! Setup event didn't run.");
        return;
    }

    double x = event.getEntity().getX();
    double y = event.getEntity().getY();
    double z = event.getEntity().getZ();

    var level = (ServerLevel) event.getLevel();
    
    // 1. Calculate minimum distance based on server render distance (in chunks * 16 blocks)
    // Subtract 1 chunk (16 blocks) as a small overlap buffer so there's no "dead zone"
    int viewDistanceChunks = level.getServer().getPlayerList().getViewDistance();
    double minRadius = Math.max(64.0, (viewDistanceChunks - 1) * 16.0);
    double minDistSqr = minRadius * minRadius;

    // 2. Calculate maximum distance safely as a double (prevents int overflow)
    double maxRadius = EtheryConfig.MAX_DISTANCE.get().doubleValue();
    double maxDistSqr = maxRadius * maxRadius;

    for (ServerPlayer player : level.getPlayers(p -> true)) {
        double distSqr = player.distanceToSqr(event.getEntity());

        // Only send the DH bolt if the strike is OUTSIDE vanilla view distance AND INSIDE max DH distance
        if (distSqr >= minDistSqr && distSqr <= maxDistSqr) {
            EtheryDhFarlightningBoltMod.DH_CHANNEL.sendTo(
                new ThunderFlashPacket(x, y, z),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
            );
        }
     }
   }
}