package com.bulkheadengineering.mobility;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
public final class LiftNetwork {
 public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("bulkheadengineering","lifts"),()->"1","1"::equals,"1"::equals);
 public record Open(BlockPos pos,int entity,String mode,CompoundTag data){}
 public record Action(BlockPos pos,int entity,String action,CompoundTag data){}
 public static void register(){
  CHANNEL.registerMessage(0,Open.class,(p,b)->{b.writeBlockPos(p.pos);b.writeInt(p.entity);b.writeUtf(p.mode,16);b.writeNbt(p.data);},b->new Open(b.readBlockPos(),b.readInt(),b.readUtf(16),b.readNbt()),(p,c)->{var context=c.get();if(context.getDirection()==NetworkDirection.PLAY_TO_CLIENT)context.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->LiftScreens.open(p)));context.setPacketHandled(true);});
  CHANNEL.registerMessage(1,Action.class,(p,b)->{b.writeBlockPos(p.pos);b.writeInt(p.entity);b.writeUtf(p.action,16);b.writeNbt(p.data);},b->new Action(b.readBlockPos(),b.readInt(),b.readUtf(16),b.readNbt()),(p,c)->{var context=c.get();if(context.getDirection()==NetworkDirection.PLAY_TO_SERVER)context.enqueueWork(()->handle(p,context.getSender()));context.setPacketHandled(true);});
 }
 public static void open(Player player,BlockPos pos,LiftCarEntity car,String mode){if(!(player instanceof ServerPlayer sp))return;CompoundTag data=car!=null?car.config().copy():new CompoundTag();if(car!=null)data.putInt("current",car.floor());else if(player.level().getBlockEntity(pos) instanceof LiftStationEntity be)data=be.getUpdateTag();CHANNEL.send(PacketDistributor.PLAYER.with(()->sp),new Open(pos,car==null?-1:car.getId(),mode,data));}
 private static void handle(Action p,ServerPlayer player){if(player==null||p.data==null)return;var level=player.serverLevel();var entity=level.getEntity(p.entity);LiftCarEntity car=entity instanceof LiftCarEntity lift?lift:null;
  boolean nearby=player.distanceToSqr(p.pos.getX()+.5,p.pos.getY()+.5,p.pos.getZ()+.5)<=64,aboard=car!=null&&(player.getVehicle()==car||car.distanceToSqr(player)<64);if(!nearby&&!aboard)return;
  var be=level.hasChunkAt(p.pos)?level.getBlockEntity(p.pos):null;
  boolean linked=car!=null&&(car.tracks.contains(p.pos)||be instanceof LiftStationEntity station&&station.car()==car||aboard);if(car!=null&&!linked)return;
  switch(p.action){
   case "select":if(car!=null&&(!(be instanceof LiftStationEntity station)||!station.locked))car.request(p.data.getInt("floor"));break;
   case "cabin":if(car!=null&&player.mayBuild()&&(player.isHolding(Mobility.WRENCH.get())||player.isHolding(Mobility.REFRESHER.get()))){car.pauseForEditing();car.configure(p.data);}break;
   case "floor":if(nearby&&be instanceof LiftStationEntity station&&level.getBlockState(p.pos).getBlock() instanceof LiftTrackBlock&&player.mayBuild()&&player.isHolding(Mobility.WRENCH.get())){station.floorName=p.data.getString("name").substring(0,Math.min(32,p.data.getString("name").length()));station.description=p.data.getString("description").substring(0,Math.min(64,p.data.getString("description").length()));station.ding=p.data.getBoolean("ding");station.sync();if(station.car()!=null)station.car().refreshNames();}break;
  }
 }
}
