package com.bulkheadengineering;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-owned and automatically synced to joining clients by Forge. */
public final class DoorConfig {
 public static final ForgeConfigSpec SPEC;
 public static final ForgeConfigSpec.BooleanValue REDSTONE_ONLY;
 static {
  var builder=new ForgeConfigSpec.Builder();
  REDSTONE_ONLY=builder.comment("Disable hand opening/closing for every Bulkhead Engineering door and hatch.",
    "Redstone and modular blast-door pulse controls still work. Sneak-click skin selection is unchanged.")
    .define("redstoneOnly",true);
  SPEC=builder.build();
 }
 private DoorConfig(){}
}
