package com.bulkheadengineering;
import com.bulkheadengineering.mobility.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.Screenshot;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.block.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Difficulty;
@Mod.EventBusSubscriber(modid=Doors.ID,value=Dist.CLIENT)
public class MobilityWorldSmoke {
 static boolean started,ready;static int ticks;static volatile int carId;static volatile boolean placed;
 static final BlockPos POS=new BlockPos(0,200,0);
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("bulkheadengineering.mobilitySmoke"))return;var mc=Minecraft.getInstance();
  if(!started&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){started=true;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(5);mc.options.hideGui=true;mc.options.pauseOnLostFocus=false;mc.createWorldOpenFlows().createFreshLevel("mobility-regression-"+System.currentTimeMillis(),new LevelSettings("Lift and escalator regression",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(1L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());return;}
  if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!ready){ready=true;mc.getSingleplayerServer().execute(()->{var l=mc.getSingleplayerServer().overworld();l.setDayTime(6000);l.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,mc.getSingleplayerServer());for(int x=-16;x<=16;x++)for(int z=-16;z<=16;z++)l.setBlock(new BlockPos(x,199,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
   for(int y=0;y<=6;y++)l.setBlock(POS.above(y),Mobility.BLOCKS.get(y==0||y==6?"lift_track_floor_1":"lift_track_1").get().defaultBlockState(),3);
   var car=Mobility.LIFT_CAR.get().create(l);car.initialize(LiftRoute.discover(l,POS),Direction.SOUTH);l.addFreshEntity(car);carId=car.getId();for(var p:car.floors()){var be=(LiftStationEntity)l.getBlockEntity(p);be.cabin=car.getUUID();be.cabinId=car.getId();be.sync();}
   for(int row=0;row<6;row++)for(int x=0;x<2;x++)for(int y=0;y<2;y++){var p=new BlockPos(6+x,199+Math.max(0,Math.min(row-1,3))+y,-row);var b=y==0?Mobility.ESCALATOR_STEP.get():Mobility.ESCALATOR_SIDE.get();l.setBlock(p,b.defaultBlockState().setValue(EscalatorBlock.SIDE,x==0?EscalatorBlock.Side.LEFT:EscalatorBlock.Side.RIGHT),3);}
   for(int floor=0;floor<2;floor++){var p=new BlockPos(-1,200+6*floor,4);var b=(LiftFixtureBlock)Mobility.BLOCKS.get("lift_door_odd_1").get();var state=b.defaultBlockState();l.setBlock(p,state,3);b.setPlacedBy(l,p,state,null,ItemStack.EMPTY);for(int x=0;x<3;x++)for(int y=0;y<2;y++){var be=(LiftStationEntity)l.getBlockEntity(p.offset(x,y,0));be.links.add(POS.above(6*floor));be.sync();}}
   for(String id:new String[]{"lift_buttons_1","lift_panel_odd_1"}){var p=id.contains("buttons")?new BlockPos(2,201,4):new BlockPos(-1,202,4);var b=(LiftFixtureBlock)Mobility.BLOCKS.get(id).get();l.setBlock(p,b.defaultBlockState(),3);b.setPlacedBy(l,p,b.defaultBlockState(),null,ItemStack.EMPTY);for(int x=0;x<b.width();x++){var be=(LiftStationEntity)l.getBlockEntity(p.east(x));be.links.add(POS);be.sync();}}
   var player=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,12000,0,false,false));player.getAbilities().flying=true;player.onUpdateAbilities();player.connection.teleport(9,203,11,140,12);placed=true;
  });return;}if(!placed)return;ticks++;
  if(ticks==70){checkModelCache();shot(mc,"01-lift-ground");}
  if(ticks==80)mc.getSingleplayerServer().execute(()->((LiftCarEntity)mc.getSingleplayerServer().overworld().getEntity(carId)).request(1));
  if(ticks==125)shot(mc,"02-lift-moving");
  if(ticks==210){shot(mc,"03-lift-upper");mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().getPlayers().get(0).connection.teleport(10,203,5,150,17));}
  if(ticks==250){shot(mc,"04-escalator");mc.options.hideGui=false;var car=(LiftCarEntity)mc.level.getEntity(carId);LiftScreens.open(new LiftNetwork.Open(POS,carId,"cabin",car.config().copy()));}
  if(ticks==280){shot(mc,"05-lift-setup");mc.setScreen(null);mc.options.hideGui=true;}
  if(ticks==300)mc.getSingleplayerServer().execute(()->{var car=(LiftCarEntity)mc.getSingleplayerServer().overworld().getEntity(carId);var player=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);player.getAbilities().flying=false;player.onUpdateAbilities();player.connection.teleport(car.getX(),car.getY()+.01,car.getZ(),0,0);car.request(0);});
  if(ticks==345){if(!(mc.player.getVehicle() instanceof LiftCarEntity))throw new IllegalStateException("Client player was not picked up by lift");shot(mc,"06-riding-lift");System.out.println("BULKHEAD_MOBILITY_WORLD_SMOKE: client passenger is riding moving cabin at "+mc.player.getY());}
  if(ticks==455){if(mc.player.isPassenger()||Math.abs(mc.player.getY()-200)>.3)throw new IllegalStateException("Client passenger did not arrive safely: "+mc.player.position());shot(mc,"07-arrived-inside");System.out.println("BULKHEAD_MOBILITY_WORLD_SMOKE: client passenger arrived and dismounted at ground floor");mc.options.hideGui=false;mc.setScreen(new ItemsGallery());}
  if(ticks==480){shot(mc,"08-mobility-inventory");System.out.println("BULKHEAD_MOBILITY_WORLD_SMOKE: rendered cabin, doors, escalator, setup and all mobility items");mc.setScreen(null);}
  if(ticks==490)mc.getSingleplayerServer().execute(()->{var player=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);player.connection.teleport(7,199.94,.5,180,0);});
  if(ticks==555){if(mc.player.getY()<202||mc.player.getZ()>-2)throw new IllegalStateException("Escalator did not carry player: "+mc.player.position());shot(mc,"09-escalator-ride");System.out.println("BULKHEAD_MOBILITY_WORLD_SMOKE: escalator carried client player upstairs to "+mc.player.position());mc.stop();}
 }
 static void checkModelCache(){try{
  var root=new com.bulkheadengineering.mobility.client.ModelPartExtension();root.addCuboid(0,0,0,1,1,1,0,false);root.addChild().addCuboid(0,0,0,1,1,1,0,false);
  var bake=root.getClass().getDeclaredMethod("bake");bake.setAccessible(true);var part=bake.invoke(root);
  try{var data=Class.forName("me.jellysquid.mods.sodium.client.render.immediate.model.ModelPartData");var cuboids=data.getMethod("getCuboids");var children=data.getMethod("getChildren");Object[] childParts=(Object[])children.invoke(part);if(java.lang.reflect.Array.getLength(cuboids.invoke(part))!=1||childParts.length!=1||java.lang.reflect.Array.getLength(cuboids.invoke(childParts[0]))!=1)throw new IllegalStateException("Optimized renderer cached incomplete lift geometry");System.out.println("BULKHEAD_MODEL_CACHE: complete parent and child geometry cached by optimized renderer");}catch(ClassNotFoundException vanilla){System.out.println("BULKHEAD_MODEL_CACHE: vanilla model bake passed");}
 }catch(ReflectiveOperationException ex){throw new RuntimeException(ex);}}
 static class ItemsGallery extends net.minecraft.client.gui.screens.Screen {
  ItemsGallery(){super(net.minecraft.network.chat.Component.literal("Lifts and escalators"));}
  public boolean isPauseScreen(){return false;}
  public void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float partial){g.fill(0,0,width,height,0xff25323d);g.drawCenteredString(font,title,width/2,12,0xffffff);int i=0;for(var entry:Mobility.ITEMS.entrySet()){int px=20+(i%4)*(width/4),py=35+(i/4)*45;g.pose().pushPose();g.pose().translate(px,py,0);g.pose().scale(2,2,2);g.renderItem(new ItemStack(entry.getValue().get()),0,0);g.pose().popPose();g.drawString(font,entry.getKey().replace("lift_",""),px,py+32,0xffffff);i++;}}
 }
 static void shot(Minecraft mc,String name){try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){java.nio.file.Files.createDirectories(java.nio.file.Path.of("mobility-preview"));image.writeToFile(java.nio.file.Path.of("mobility-preview",name+".png"));}catch(Exception ex){throw new RuntimeException(ex);}}
}
