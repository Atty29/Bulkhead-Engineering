package com.bulkheadengineering;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.minecraft.world.Difficulty;
@Mod.EventBusSubscriber(modid=Doors.ID,value=Dist.CLIENT)
public class IdentitySmoke {
 static boolean started,ready,done;static int ticks,phase;static volatile boolean placed;public static boolean overlaySeen;
 static final BlockPos POS=new BlockPos(0,100,0);static volatile BlockPos target=POS;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
  if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("bulkheadengineering.identitySmoke"))return;var mc=Minecraft.getInstance();
  if(!started&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){started=true;String name=net.minecraftforge.fml.ModList.get().getModContainerById(Doors.ID).orElseThrow().getModInfo().getDisplayName();if(!name.equals("Bulkhead Engineering"))throw new IllegalStateException(name);System.out.println("BULKHEAD_METADATA: "+name+"; model variants="+DoorRenderer.verifyAllModels());mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);mc.options.hideGui=false;
   if(!System.getProperty("bulkheadengineering.reloadIdentity", "").isEmpty()){mc.createWorldOpenFlows().loadLevel(new TitleScreen(),System.getProperty("bulkheadengineering.reloadIdentity"));return;}
   mc.createWorldOpenFlows().createFreshLevel("bulkhead-identity-"+System.currentTimeMillis(),new LevelSettings("Bulkhead identity acceptance",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(1L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());return;}
  if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
  if(done){if(++ticks==10)MobilityWorldSmoke.shot(mc,"bulkhead-mods-menu");if(ticks>20)mc.stop();return;}
  if(!ready){ready=true;mc.getSingleplayerServer().execute(()->{var l=mc.getSingleplayerServer().overworld();l.setDayTime(6000);var block=(AnimatedDoorBlock)Doors.DOORS.get("secure_access_door").get();if(System.getProperty("bulkheadengineering.reloadIdentity", "").isEmpty()){l.setBlock(POS,block.defaultBlockState(),3);var item=new ItemStack(block);item.getOrCreateTag().putInt(DoorTarget.STYLE,2);block.setPlacedBy(l,POS,block.defaultBlockState(),null,item);}else if(!(l.getBlockEntity(POS) instanceof com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity door)||door.variant!=2)throw new IllegalStateException("Saved door/style missing after reopening");placed=true;});return;}if(!placed)return;
  if(ticks++==0){overlaySeen=false;mc.getSingleplayerServer().execute(()->{var l=mc.getSingleplayerServer().overworld();var block=(AnimatedDoorBlock)l.getBlockState(POS).getBlock();target=phase%2==0?POS:block.offsets().stream().filter(o->o.getY()>0&&!l.getBlockState(POS.offset(o)).getShape(l,POS.offset(o)).isEmpty()).map(POS::offset).findFirst().orElseThrow();var shape=l.getBlockState(target).getShape(l,target).bounds();var center=shape.getCenter().add(target.getX(),target.getY(),target.getZ());var player=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);player.setGameMode(phase<2?GameType.CREATIVE:GameType.SURVIVAL);player.getAbilities().mayfly=true;player.getAbilities().flying=true;player.onUpdateAbilities();player.connection.teleport(center.x,center.y-player.getEyeHeight(),center.z-3,0,0);});}
  if(ticks==35){var expected=DoorTarget.pick(mc.level,target);if(expected.isEmpty())throw new IllegalStateException("Client ownership missing");try{var pick=Minecraft.class.getDeclaredMethod("pickBlock");pick.setAccessible(true);mc.player.getInventory().clearContent();mc.hitResult=new BlockHitResult(Vec3.atCenterOf(target),Direction.NORTH,target,false);
    if(phase>=2){pick.invoke(mc);if(!mc.player.getInventory().isEmpty())throw new IllegalStateException("Survival pick created items");mc.player.getInventory().setItem(4,expected.copy());mc.player.getInventory().selected=0;}
    pick.invoke(mc);if(!ItemStack.matches(mc.player.getMainHandItem(),expected))throw new IllegalStateException("Pick did not select full door/style");
   }catch(ReflectiveOperationException ex){throw new RuntimeException(ex);}
   if(net.minecraftforge.fml.ModList.get().isLoaded("jade")&&!overlaySeen)throw new IllegalStateException("Jade did not report correct live tooltip for phase "+phase);
   MobilityWorldSmoke.shot(mc,"identity-"+phase);System.out.println("BULKHEAD_PICK: actual Minecraft pick passed "+(phase<2?"Creative":"Survival")+" "+(phase%2==0?"controller":"frame"));
   phase++;ticks=0;if(phase==4){done=true;mc.setScreen(new net.minecraftforge.client.gui.ModListScreen(new TitleScreen()));}
  }
 }
}
