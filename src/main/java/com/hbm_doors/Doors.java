package com.hbm_doors;

import com.hbm_doors.legacy.block.entity.doors.*;
import com.hbm_doors.legacy.sound.ModSounds;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import java.util.*;

@Mod(Doors.ID)
public class Doors {
 public static final String ID="hbm_doors";
 private static final BlockSetType STEEL_HATCH=BlockSetType.register(new BlockSetType("hbm_doors:steel",true,SoundType.METAL,
  BlockSetType.IRON.doorClose(),BlockSetType.IRON.doorOpen(),BlockSetType.IRON.trapdoorClose(),BlockSetType.IRON.trapdoorOpen(),
  BlockSetType.IRON.pressurePlateClickOff(),BlockSetType.IRON.pressurePlateClickOn(),BlockSetType.IRON.buttonClickOff(),BlockSetType.IRON.buttonClickOn()));
 public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,ID);
 public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,ID);
 public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,ID);
 public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,ID);
 public static final Map<String,RegistryObject<Block>> DOORS=new LinkedHashMap<>();
 static { DoorDeclRegistry.init(); }
 public static final RegistryObject<Block> PART=BLOCKS.register("door_part",()->new DoorPartBlock());
 static {
  DoorDeclRegistry.getAll().keySet().stream().sorted().forEach(id->register(id,()->new AnimatedDoorBlock(id)));
  register("door_bunker",()->new DoorBlock(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK).noOcclusion(),BlockSetType.STONE));
  register("door_office",()->new DoorBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD).noOcclusion(),BlockSetType.CHERRY));
  register("metal_door",()->new DoorBlock(BlockBehaviour.Properties.copy(Blocks.CHAIN).noOcclusion(),BlockSetType.BIRCH));
  register("blast_door",()->new AnimatedDoorBlock("modular_blast_door"));
  for(String id:List.of("fusion_hatch","seal_hatch","trapdoor_steel"))register(id,()->new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_TRAPDOOR).noOcclusion(),STEEL_HATCH));
 }
 public static final RegistryObject<BlockEntityType<DoorBlockEntity>> DOOR_ENTITY=ENTITIES.register("door",()->BlockEntityType.Builder.of(DoorBlockEntity::new,DOORS.values().stream().map(RegistryObject::get).filter(b->b instanceof AnimatedDoorBlock).toArray(Block[]::new)).build(null));
 public static final RegistryObject<BlockEntityType<DoorPartEntity>> PART_ENTITY=ENTITIES.register("door_part",()->BlockEntityType.Builder.of(DoorPartEntity::new,PART.get()).build(null));
 static { TABS.register("doors",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.hbm_doors")).icon(()->new ItemStack(DOORS.get("vault_door").get())).displayItems((p,o)->DOORS.values().forEach(b->o.accept(b.get()))).build()); }
 private static void register(String id,java.util.function.Supplier<Block> factory){
  RegistryObject<Block> b=BLOCKS.register(id,factory);DOORS.put(id,b);
  ITEMS.register(id,()->b.get() instanceof AnimatedDoorBlock ? new DoorItem(b.get()) : new BlockItem(b.get(),new Item.Properties()));
 }
 public Doors(){var bus=FMLJavaModLoadingContext.get().getModEventBus();BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);TABS.register(bus);ModSounds.REG.register(bus);}
}
