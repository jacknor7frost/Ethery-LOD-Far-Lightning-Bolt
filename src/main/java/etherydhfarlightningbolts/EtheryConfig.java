package etherydhfarlightningbolts;

import net.minecraftforge.common.ForgeConfigSpec;

public class EtheryConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.DoubleValue MAX_DISTANCE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Ethery DH Far Lightning").push("general");

        ENABLED = builder.comment("Enable the mod").define("enabled", true);
        MAX_DISTANCE = builder.comment("Max distance (blocks) to send lightning packets. 0 = unlimited.")
                .defineInRange("maxDistance", 4096.0, 0.0, 32768.0);

        builder.pop();
        SPEC = builder.build();
    }
}