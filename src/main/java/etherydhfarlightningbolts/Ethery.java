package etherydhfarlightningbolts;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod("ethery_dh_farlightning_bolt")
public class Ethery {
    public static final String MODID = "ethery_dh_farlightning_bolt";
    private static final String PROTOCOL_VERSION = "1";
    private static int messageID = 0;

    // This will be initialized safely during common setup
    public static SimpleChannel PACKET_HANDLER;

    public Ethery(FMLJavaModLoadingContext context) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EtheryConfig.SPEC);

        // Tell Forge to run commonSetup when the mod loads
        context.getModEventBus().addListener(this::commonSetup);
        
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Initialize the network channel here
        PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, MODID), 
                () -> PROTOCOL_VERSION, 
                PROTOCOL_VERSION::equals, 
                PROTOCOL_VERSION::equals);

        // Register the ThunderFlashPacket
        PACKET_HANDLER.registerMessage(messageID++, ThunderFlashPacket.class, 
                ThunderFlashPacket::encode, 
                ThunderFlashPacket::decode, 
                ThunderFlashPacket::handle);
                
        System.out.println("DH MOD: Network channel registered successfully!");
    }
}