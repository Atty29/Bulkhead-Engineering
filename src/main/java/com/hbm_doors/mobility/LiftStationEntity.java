package com.hbm_doors.mobility;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
public final class LiftStationEntity extends BlockEntity {
 public String floorName="1",description="";public boolean ding=true,locked=false,powered=false;public UUID cabin;public int cabinId=-1;public final List<BlockPos> links=new ArrayList<>();public float doorProgress;
 public LiftStationEntity(BlockPos p,BlockState s){super(Mobility.STATION_ENTITY.get(),p,s);floorName=Integer.toString(p.getY());}
 public LiftCarEntity car(){
  if(level==null)return null;if(cabin!=null){var e=level instanceof ServerLevel server?server.getEntity(cabin):level.getEntity(cabinId);if(e instanceof LiftCarEntity lift)return lift;}
  for(var p:links)if(level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof LiftStationEntity station&&station!=this&&station.cabin!=null){var e=level instanceof ServerLevel server?server.getEntity(station.cabin):level.getEntity(station.cabinId);if(e instanceof LiftCarEntity lift)return lift;}return null;
 }
 public void call(){
  if(level==null||level.isClientSide)return;
  List<BlockPos> candidates=new ArrayList<>(links);if(cabin!=null)candidates.add(worldPosition);
  LiftCarEntity best=null;int floor=-1;double score=Double.MAX_VALUE;
  for(var p:candidates)if(level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof LiftStationEntity station){var lift=station.car();if(lift==null)continue;int index=lift.floors().indexOf(p);if(index<0)continue;double distance=Math.abs(lift.cursor-lift.stops.get(index))+(lift.moving()?32:0);if(distance<score){score=distance;best=lift;floor=index;}}
  if(best!=null)best.request(floor);
 }
 public void powerChanged(){if(level==null)return;boolean next=level.hasNeighborSignal(worldPosition);if(next&&!powered)call();powered=next;setChanged();}
 public void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 public static void tick(Level l,BlockPos p,BlockState s,LiftStationEntity be){if(l.isClientSide)return;var car=be.car();if(car!=null&&be.cabin!=null&&be.cabinId!=car.getId()){be.cabinId=car.getId();be.sync();}
  if(s.getBlock() instanceof LiftFixtureBlock fixture&&fixture.kind==LiftFixtureBlock.Kind.DOOR){boolean open=false;if(car!=null){for(var link:be.links)if(car.at(link)){open=true;break;}}float next=net.minecraft.util.Mth.clamp(be.doorProgress+(open?.1f:-.1f),0,1);if(next!=be.doorProgress){be.doorProgress=next;be.sync();}}
 }
 protected void saveAdditional(CompoundTag t){super.saveAdditional(t);t.putString("floorName",floorName);t.putString("description",description);t.putBoolean("ding",ding);t.putBoolean("locked",locked);t.putBoolean("powered",powered);if(cabin!=null)t.putUUID("cabin",cabin);t.putInt("cabinId",cabinId);t.putLongArray("links",links.stream().mapToLong(BlockPos::asLong).toArray());t.putFloat("door",doorProgress);}
 public void load(CompoundTag t){super.load(t);floorName=t.getString("floorName");description=t.getString("description");ding=t.getBoolean("ding");locked=t.getBoolean("locked");powered=t.getBoolean("powered");cabin=t.hasUUID("cabin")?t.getUUID("cabin"):null;cabinId=t.getInt("cabinId");links.clear();for(long p:t.getLongArray("links"))if(links.size()<16)links.add(BlockPos.of(p));doorProgress=t.getFloat("door");}
 public CompoundTag getUpdateTag(){return saveWithoutMetadata();}public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
