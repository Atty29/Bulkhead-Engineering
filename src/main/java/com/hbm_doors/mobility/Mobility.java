package com.hbm_doors.mobility;
import com.hbm_doors.Doors;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.*;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;
import java.util.*;

public final class Mobility {
 public static final Map<String,RegistryObject<Block>> BLOCKS=new LinkedHashMap<>();
 public static final Map<String,RegistryObject<Item>> ITEMS=new LinkedHashMap<>();
 public static final DeferredRegister<EntityType<?>> ENTITY_TYPES=DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,Doors.ID);
 public static final RegistryObject<EntityType<LiftCarEntity>> LIFT_CAR=ENTITY_TYPES.register("lift_car",()->EntityType.Builder.<LiftCarEntity>of(LiftCarEntity::new,MobCategory.MISC).sized(3,.125f).clientTrackingRange(12).updateInterval(1).build("hbm_doors:lift_car"));
 public static final RegistryObject<EntityType<LiftBarrierEntity>> LIFT_BARRIER=ENTITY_TYPES.register("lift_barrier",()->EntityType.Builder.<LiftBarrierEntity>of(LiftBarrierEntity::new,MobCategory.MISC).sized(1,1).clientTrackingRange(12).updateInterval(1).noSave().noSummon().build("hbm_doors:lift_barrier"));
 public static final RegistryObject<Block> ESCALATOR_STEP=block("escalator_step",()->new EscalatorBlock(false),false),ESCALATOR_SIDE=block("escalator_side",()->new EscalatorBlock(true),false);
 public static final RegistryObject<Item> ESCALATOR=item("escalator",EscalatorBlock.Placer::new),WRENCH=item("lift_wrench",()->new Item(new Item.Properties().stacksTo(1))),REFRESHER=item("lift_refresher",()->new LiftToolItem(LiftToolItem.Kind.REFRESH)),CONNECTOR=item("lift_buttons_link_connector",()->new LiftToolItem(LiftToolItem.Kind.LINK)),REMOVER=item("lift_buttons_link_remover",()->new LiftToolItem(LiftToolItem.Kind.UNLINK));
 static {
  block("lift_track_1",()->new LiftTrackBlock(LiftTrackBlock.Kind.VERTICAL),true);block("lift_track_horizontal_1",()->new LiftTrackBlock(LiftTrackBlock.Kind.HORIZONTAL),true);block("lift_track_diagonal_1",()->new LiftTrackBlock(LiftTrackBlock.Kind.DIAGONAL),true);block("lift_track_floor_1",()->new LiftTrackBlock(LiftTrackBlock.Kind.FLOOR),true);
  block("lift_buttons_1",()->new LiftFixtureBlock(LiftFixtureBlock.Kind.BUTTON,false,1),true);
  for(boolean odd:new boolean[]{false,true}){block(odd?"lift_door_odd_1":"lift_door_1",()->new LiftFixtureBlock(LiftFixtureBlock.Kind.DOOR,odd,1),true);for(int i=1;i<=2;i++){int style=i;block("lift_panel_"+(odd?"odd_":"even_")+i,()->new LiftFixtureBlock(LiftFixtureBlock.Kind.PANEL,odd,style),true);}}
 }
 public static final RegistryObject<BlockEntityType<LiftStationEntity>> STATION_ENTITY=Doors.ENTITIES.register("lift_station",()->BlockEntityType.Builder.of(LiftStationEntity::new,BLOCKS.values().stream().map(RegistryObject::get).filter(b->b instanceof LiftFixtureBlock||b instanceof LiftTrackBlock t&&t.kind==LiftTrackBlock.Kind.FLOOR).toArray(Block[]::new)).build(null));
 private static RegistryObject<Block> block(String id,java.util.function.Supplier<Block> f,boolean item){var b=Doors.BLOCKS.register(id,f);BLOCKS.put(id,b);if(item)item(id,()->new BlockItem(b.get(),new Item.Properties()));return b;}
 private static RegistryObject<Item> item(String id,java.util.function.Supplier<Item> f){var i=Doors.ITEMS.register(id,f);ITEMS.put(id,i);return i;}
 public static boolean isLinkTool(ItemStack stack){return stack.is(CONNECTOR.get())||stack.is(REMOVER.get());}
 public static void init(IEventBus bus){ENTITY_TYPES.register(bus);LiftNetwork.register();}
 public static void creative(CreativeModeTab.Output output){ITEMS.values().forEach(i->output.accept(i.get()));}
}
