package com.bulkheadengineering;
import com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity;
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
/** Isolated in-world visual regression run; never shipped in the mod. */
@Mod.EventBusSubscriber(modid=Doors.ID,value=Dist.CLIENT)
public class WorldSmoke {
 static boolean started,ready;static int ticks,shot;static volatile boolean placed;
 static final String[] IDS={"fire_door","large_vehicle_door","qe_sliding_door","secure_access_door","sliding_seal_door","vault_door","water_door","blast_door","fusion_hatch","seal_hatch","trapdoor_steel"};
 static final BlockPos POS=new BlockPos(0,200,0);
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("bulkheadengineering.worldSmoke"))return;
  var mc=Minecraft.getInstance();
  if(!started&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){
   started=true;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(5);mc.options.hideGui=true;
   mc.createWorldOpenFlows().createFreshLevel("door-regression-"+System.currentTimeMillis(),new LevelSettings("Door visual regression",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(1L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());return;
  }
  if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!ready){ready=true;mc.getSingleplayerServer().execute(()->{
   var l=mc.getSingleplayerServer().overworld();l.setDayTime(6000);l.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,mc.getSingleplayerServer());
   for(int x=-16;x<=16;x++)for(int z=-16;z<=16;z++)l.setBlock(new BlockPos(x,199,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);
   var player=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);player.getAbilities().flying=true;player.onUpdateAbilities();player.connection.teleport(8,204,12,145,12);
  });return;}
  if(++ticks<40)return;
  int index=shot/3,stage=shot%3;if(index>=IDS.length){System.out.println("BULKHEAD_WORLD_SMOKE: captured all 11 entries closed, moving and open in a real world");mc.stop();return;}
  if(!placed){placed=true;mc.getSingleplayerServer().execute(()->{
   var l=mc.getSingleplayerServer().overworld();String id=IDS[index];
   if(stage==0){l.removeBlock(POS,false);var b=Doors.DOORS.get(id).get();var state=b.defaultBlockState();l.setBlock(POS,state,3);b.setPlacedBy(l,POS,state,null,ItemStack.EMPTY);}
   if(l.getBlockEntity(POS) instanceof DoorBlockEntity be){var tag=be.saveWithoutMetadata();int time=stage*be.getDoorDecl().getOpenTime()/2;tag.putInt("ticks",time);tag.putByte("state",(byte)(stage==2?1:0));be.load(tag);be.sync();}
   else if(stage>0)l.setBlock(POS,l.getBlockState(POS).setValue(TrapDoorBlock.OPEN,true),3);
   var player=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
   double height=id.equals("large_vehicle_door")||id.equals("blast_door")?3:1.7;
   double distance=id.equals("large_vehicle_door")||id.equals("blast_door")||id.equals("vault_door")?11:id.equals("secure_access_door")?8:5;
   double x=distance*.45,z=distance,y=200+height+1;
   if(id.equals("vault_door")){x=-3;z=-7;y=202;}
   double dx=.5-x,dy=200+height-(y+player.getEyeHeight()),dz=.5-z;
   player.connection.teleport(x,y,z,(float)Math.toDegrees(Math.atan2(-dx,dz)),(float)-Math.toDegrees(Math.atan2(dy,Math.hypot(dx,dz))));
  });ticks=0;return;}
  if(ticks>=40){mc.setScreen(null);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){java.nio.file.Files.createDirectories(java.nio.file.Path.of("world-preview"));image.writeToFile(java.nio.file.Path.of("world-preview",IDS[index]+"-"+stage+".png"));}catch(Exception ex){throw new RuntimeException(ex);}System.out.println("BULKHEAD_WORLD_SMOKE: "+IDS[index]+" stage "+stage);shot++;placed=false;ticks=40;}
 }
}
