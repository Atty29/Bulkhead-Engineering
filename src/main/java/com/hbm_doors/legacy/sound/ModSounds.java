package com.hbm_doors.legacy.sound;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.*;
public class ModSounds {
public static final DeferredRegister<SoundEvent> REG=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,"hbm_doors");
public static final RegistryObject<SoundEvent> DOOR_WGH_BIG_STOP=REG.register("block.door_wgh_big_stop",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.door_wgh_big_stop")));
public static final RegistryObject<SoundEvent> WGH_START=REG.register("block.wgh_start",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.wgh_start")));
public static final RegistryObject<SoundEvent> DOOR_WGH_BIG_START=REG.register("block.door_wgh_big_start",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.door_wgh_big_start")));
public static final RegistryObject<SoundEvent> GARAGE_MOVE=REG.register("block.garage_move",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.garage_move")));
public static final RegistryObject<SoundEvent> SLIDING_DOOR_OPENING=REG.register("block.sliding_door_opening",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.sliding_door_opening")));
public static final RegistryObject<SoundEvent> VAULT_SCRAPE=REG.register("block.vault_scrape",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.vault_scrape")));
public static final RegistryObject<SoundEvent> METAL_STOP_1=REG.register("block.metal_stop_1",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.metal_stop_1")));
public static final RegistryObject<SoundEvent> ALARM_6=REG.register("block.alarm_6",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.alarm_6")));
public static final RegistryObject<SoundEvent> DOOR_MOVE_2=REG.register("block.door_move_2",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.door_move_2")));
public static final RegistryObject<SoundEvent> SLIDING_DOOR_SHUT=REG.register("block.sliding_door_shut",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.sliding_door_shut")));
public static final RegistryObject<SoundEvent> LEVER_1=REG.register("block.lever_1",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.lever_1")));
public static final RegistryObject<SoundEvent> VAULT_THUD=REG.register("block.vault_thud",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.vault_thud")));
public static final RegistryObject<SoundEvent> WGH_STOP=REG.register("block.wgh_stop",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.wgh_stop")));
public static final RegistryObject<SoundEvent> SLIDING_DOOR_OPENED=REG.register("block.sliding_door_opened",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.sliding_door_opened")));
public static final RegistryObject<SoundEvent> GARAGE_STOP=REG.register("block.garage_stop",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.garage_stop")));
public static final RegistryObject<SoundEvent> MODULAR_START=REG.register("block.modular_start",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.modular_start")));
public static final RegistryObject<SoundEvent> MODULAR_STOP=REG.register("block.modular_stop",()->SoundEvent.createVariableRangeEvent(new ResourceLocation("hbm_doors","block.modular_stop")));
}