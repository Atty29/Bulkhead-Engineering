package com.bulkheadengineering.mobility;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;
import java.util.*;

/** Standalone authoritative lift simulation; no railway simulator or web server. */
public final class LiftCarEntity extends Entity {
 private static final EntityDataAccessor<CompoundTag> CONFIG=SynchedEntityData.defineId(LiftCarEntity.class,EntityDataSerializers.COMPOUND_TAG);
 private static final EntityDataAccessor<Float> DOOR=SynchedEntityData.defineId(LiftCarEntity.class,EntityDataSerializers.FLOAT);
 private static final EntityDataAccessor<Integer> FLOOR=SynchedEntityData.defineId(LiftCarEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> MOVING=SynchedEntityData.defineId(LiftCarEntity.class,EntityDataSerializers.BOOLEAN);
 public final List<BlockPos> tracks=new ArrayList<>();public final List<Vec3> points=new ArrayList<>();public final List<Integer> stops=new ArrayList<>();
 private final ArrayDeque<Integer> requests=new ArrayDeque<>();private final Map<UUID,Vec3> riderOffsets=new HashMap<>();
 private final List<LiftBarrierEntity> barriers=new ArrayList<>();
 public double cursor;private int target=-1,dwell=40;public String fault="";private boolean maintenance;
 public LiftCarEntity(EntityType<? extends LiftCarEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
 protected void defineSynchedData(){entityData.define(CONFIG,new CompoundTag());entityData.define(DOOR,1f);entityData.define(FLOOR,0);entityData.define(MOVING,false);}
 public CompoundTag config(){return entityData.get(CONFIG);}
 public int width(){return config().contains("width")?config().getInt("width"):3;}public int depth(){return config().contains("depth")?config().getInt("depth"):3;}public double height(){return config().contains("height")?config().getDouble("height"):3;}
 public boolean doubleSided(){return config().getBoolean("doubleSided");}public Direction front(){return Direction.from2DDataValue(config().getInt("facing"));}
 public float door(){return entityData.get(DOOR);}public int floor(){return entityData.get(FLOOR);}public boolean moving(){return entityData.get(MOVING);}
 public List<BlockPos> floors(){if(!stops.isEmpty())return stops.stream().map(tracks::get).toList();return Arrays.stream(config().getLongArray("floors")).mapToObj(BlockPos::of).toList();}
 public String floorName(int i){var names=config().getList("names",Tag.TAG_STRING);return i>=0&&i<names.size()?names.getString(i):Integer.toString(i+1);}
 public void configure(CompoundTag supplied){
  for(String key:List.of("height","offsetX","offsetY","offsetZ"))if(!Double.isFinite(supplied.getDouble(key)))return;
  CompoundTag n=config().copy();n.putInt("width",net.minecraft.util.Mth.clamp(supplied.getInt("width"),2,16));n.putInt("depth",net.minecraft.util.Mth.clamp(supplied.getInt("depth"),2,16));n.putDouble("height",Math.round(net.minecraft.util.Mth.clamp(supplied.getDouble("height"),2,16)*2)/2d);n.putInt("facing",Math.floorMod(supplied.getInt("facing"),4));n.putBoolean("doubleSided",supplied.getBoolean("doubleSided"));
  for(String key:List.of("offsetX","offsetY","offsetZ"))n.putDouble(key,net.minecraft.util.Mth.clamp(supplied.getDouble(key),-16,16));entityData.set(CONFIG,n);if(!points.isEmpty()){int a=Math.min(points.size()-1,(int)Math.floor(cursor)),b=Math.min(points.size()-1,a+1);setPos(carPosition(points.get(a).lerp(points.get(b),cursor-a)));}refreshBox();
 }
 public void initialize(LiftRoute route,Direction front){maintenance=false;tracks.clear();tracks.addAll(route.blocks());points.clear();points.addAll(route.points());stops.clear();stops.addAll(route.stops());cursor=stops.get(0);target=-1;requests.clear();entityData.set(FLOOR,0);entityData.set(DOOR,1f);entityData.set(MOVING,false);
  var n=config().copy();if(!n.contains("width")){n.putInt("width",3);n.putInt("depth",3);n.putDouble("height",3);n.putInt("facing",front.get2DDataValue());}n.putLongArray("floors",floors().stream().mapToLong(BlockPos::asLong).toArray());ListTag names=new ListTag();for(var p:floors())names.add(StringTag.valueOf(level().getBlockEntity(p) instanceof LiftStationEntity s?s.floorName:Integer.toString(p.getY())));n.put("names",names);entityData.set(CONFIG,n);refreshNames();setPos(carPosition(points.get(stops.get(0))));
 }
 private Vec3 carPosition(Vec3 rail){var f=front();return rail.add(f.getStepX()*depth()/2d+config().getDouble("offsetX"),config().getDouble("offsetY"),f.getStepZ()*depth()/2d+config().getDouble("offsetZ"));}
 public boolean request(int floor){if(floor<0||floor>=floors().size())return false;maintenance=false;if(!requests.contains(floor)&&target!=floor)requests.add(floor);return true;}
 public boolean at(BlockPos floor){var f=floors();return !moving()&&door()>.9&&floor()>=0&&floor()<stops.size()&&Math.abs(cursor-stops.get(floor()))<.001&&f.get(floor()).equals(floor);}
 public void tick(){super.tick();refreshBox();if(level().isClientSide||points.isEmpty())return;updateBarriers();if(maintenance)return;
  if(tickCount%20==0){fault="";for(var p:tracks)if(level().hasChunkAt(p)&&!(level().getBlockState(p).getBlock() instanceof LiftTrackBlock)){fault="Track missing; repair and refresh the lift";break;}}
  if(!fault.isEmpty()){entityData.set(MOVING,false);return;}
  if(target<0){entityData.set(MOVING,false);entityData.set(DOOR,Math.min(1,door()+.05f));if(dwell>0){dwell--;return;}if(!requests.isEmpty()){int next=requests.removeFirst();if(next!=floor()||Math.abs(cursor-stops.get(next))>.001)target=next;else dwell=40;}return;}
  if(door()>0){entityData.set(DOOR,Math.max(0,door()-.05f));return;}
  double goal=stops.get(target),sign=Math.signum(goal-cursor);int segment=sign>0?(int)Math.floor(cursor):(int)Math.ceil(cursor)-1;segment=Math.max(0,Math.min(points.size()-2,segment));double length=Math.max(.01,points.get(segment).distanceTo(points.get(segment+1)));double next=cursor+sign*.08/length;if(sign>0)next=Math.min(next,goal);else next=Math.max(next,goal);int a=(int)Math.floor(next),b=Math.min(points.size()-1,a+1);Vec3 point=carPosition(points.get(a).lerp(points.get(b),next-a));
  if(!level().hasChunkAt(BlockPos.containing(point))){entityData.set(MOVING,false);return;}
  // Solid blocks inside the cabin stop motion; the floor/track and doorway edges are excluded.
  double wx=(front().getAxis()==Direction.Axis.X?depth():width())/2d-.2,wz=(front().getAxis()==Direction.Axis.X?width():depth())/2d-.2;
  var body=new AABB(point.x-wx,point.y+.15,point.z-wz,point.x+wx,point.y+height()-.15,point.z+wz);if(level().getBlockCollisions(this,body).iterator().hasNext()){entityData.set(MOVING,false);return;}
  if(!moving()){
   double cx=(front().getAxis()==Direction.Axis.X?depth():width())/2d,cz=(front().getAxis()==Direction.Axis.X?width():depth())/2d;
   AABB cabin=new AABB(getX()-cx+.2,getY()-.05,getZ()-cz+.2,getX()+cx-.2,getY()+height()-.2,getZ()+cz-.2);
   for(var e:level().getEntitiesOfClass(LivingEntity.class,cabin,e->!e.isSpectator()&&!e.isPassenger()&&e.getY()>=getY()-.1&&e.getY()<getY()+.6)){riderOffsets.put(e.getUUID(),e.position().subtract(position()));e.startRiding(this,true);}syncRiders();entityData.set(MOVING,true);
  }

  cursor=next;setPos(point);
  if(Math.abs(cursor-goal)<.0001){entityData.set(FLOOR,target);target=-1;entityData.set(MOVING,false);dwell=40;ejectPassengers();var stop=floors().get(floor());if(level().getBlockEntity(stop) instanceof LiftStationEntity station&&station.ding)level().playSound(null,blockPosition(),net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BELL.value(),net.minecraft.sounds.SoundSource.BLOCKS,.8f,1f);}
 }
 private void refreshBox(){double x=(front().getAxis()==Direction.Axis.X?depth():width())/2d,z=(front().getAxis()==Direction.Axis.X?width():depth())/2d;setBoundingBox(new AABB(getX()-x,getY()-.125,getZ()-z,getX()+x,getY(),getZ()+z));}
 public void setPos(double x,double y,double z){super.setPos(x,y,z);if(entityData!=null&&entityData.hasItem(CONFIG))refreshBox();}
 public AABB getBoundingBoxForCulling(){return getBoundingBox().expandTowards(0,height(),0).inflate(.25);}
 public void pauseForEditing(){if(level().isClientSide)return;maintenance=true;target=-1;requests.clear();entityData.set(MOVING,false);ejectPassengers();riderOffsets.clear();syncRiders();if(floor()>=0&&floor()<stops.size()&&Math.abs(cursor-stops.get(floor()))<.001)entityData.set(DOOR,1f);updateBarriers();}
 public boolean canBeCollidedWith(){return true;}public boolean isPickable(){return true;}public boolean isPushable(){return false;}protected boolean canAddPassenger(Entity p){return getPassengers().size()<32;}
 protected void positionRider(Entity rider,MoveFunction move){var o=riderOffset(rider.getUUID());move.accept(rider,getX()+o.x,getY()+Math.max(0,o.y),getZ()+o.z);rider.fallDistance=0;}
 public Vec3 getDismountLocationForPassenger(LivingEntity p){return position().add(riderOffset(p.getUUID()));}
 public InteractionResult interact(Player p,InteractionHand hand){if(!level().isClientSide){boolean setup=p.mayBuild()&&(p.getItemInHand(hand).is(Mobility.WRENCH.get())||p.getItemInHand(hand).is(Mobility.REFRESHER.get()));if(setup)pauseForEditing();LiftNetwork.open(p,blockPosition(),this,setup?"cabin":"select");}return InteractionResult.sidedSuccess(level().isClientSide);}

 protected void addAdditionalSaveData(CompoundTag t){t.put("config",config().copy());t.putLongArray("tracks",tracks.stream().mapToLong(BlockPos::asLong).toArray());t.putIntArray("stops",stops);ListTag list=new ListTag();for(var v:points){CompoundTag n=new CompoundTag();n.putDouble("x",v.x);n.putDouble("y",v.y);n.putDouble("z",v.z);list.add(n);}t.put("points",list);t.putDouble("cursor",cursor);t.putInt("target",target);t.putInt("floor",floor());t.putFloat("door",door());t.putIntArray("queue",new ArrayList<>(requests));t.putInt("dwell",dwell);t.putBoolean("moving",moving());t.putBoolean("maintenance",maintenance);}
 protected void readAdditionalSaveData(CompoundTag t){entityData.set(CONFIG,t.getCompound("config"));tracks.clear();for(long v:t.getLongArray("tracks"))tracks.add(BlockPos.of(v));points.clear();for(var tag:t.getList("points",Tag.TAG_COMPOUND)){var n=(CompoundTag)tag;points.add(new Vec3(n.getDouble("x"),n.getDouble("y"),n.getDouble("z")));}stops.clear();for(int i:t.getIntArray("stops"))stops.add(i);cursor=t.getDouble("cursor");target=t.contains("target")?t.getInt("target"):-1;entityData.set(FLOOR,t.getInt("floor"));entityData.set(DOOR,t.getFloat("door"));requests.clear();for(int i:t.getIntArray("queue"))requests.add(i);dwell=t.getInt("dwell");entityData.set(MOVING,t.getBoolean("moving"));maintenance=t.getBoolean("maintenance");}
 private Vec3 riderOffset(UUID id){var n=config().getCompound("riders").getCompound(id.toString());return new Vec3(n.getDouble("x"),n.getDouble("y"),n.getDouble("z"));}
 private void syncRiders(){var data=config().copy();var riders=new CompoundTag();riderOffsets.forEach((id,v)->{var n=new CompoundTag();n.putDouble("x",v.x);n.putDouble("y",v.y);n.putDouble("z",v.z);riders.put(id.toString(),n);});data.put("riders",riders);entityData.set(CONFIG,data);}
 public void refreshNames(){var n=config().copy();ListTag names=new ListTag(),descriptions=new ListTag();for(var p:floors()){var station=level().getBlockEntity(p) instanceof LiftStationEntity be?be:null;names.add(StringTag.valueOf(station!=null?station.floorName:Integer.toString(p.getY())));descriptions.add(StringTag.valueOf(station!=null?station.description:""));}n.put("names",names);n.put("descriptions",descriptions);entityData.set(CONFIG,n);}

 private void updateBarriers(){
  while(barriers.size()<7){var b=Mobility.LIFT_BARRIER.get().create(level());barriers.add(b);b.bounds(this,new AABB(position(),position()));level().addFreshEntity(b);}
  double w=width()/2d-.125,d=depth()/2d-.125,gap=door()*.75,t=.0625,h=height();
  double[][] boxes={{-w,0,-d,-w+t,h,d},{w-t,0,-d,w,h,d},{-w,0,-d,-gap,h,-d+t},{gap,0,-d,w,h,-d+t},{-w,0,d-t,doubleSided()?-gap:0,h,d},{doubleSided()?gap:0,0,d-t,w,h,d},{-w,h-.0625,-d,w,h,d}};
  var f=front();var right=f.getClockWise();
  for(int i=0;i<boxes.length;i++){var b=boxes[i];double x1=right.getStepX()*b[0]-f.getStepX()*b[2],z1=right.getStepZ()*b[0]-f.getStepZ()*b[2],x2=right.getStepX()*b[3]-f.getStepX()*b[5],z2=right.getStepZ()*b[3]-f.getStepZ()*b[5];barriers.get(i).bounds(this,new AABB(getX()+Math.min(x1,x2),getY()+b[1],getZ()+Math.min(z1,z2),getX()+Math.max(x1,x2),getY()+b[4],getZ()+Math.max(z1,z2)));}
 }
 public void remove(RemovalReason reason){for(var b:barriers)b.discard();barriers.clear();super.remove(reason);}
 public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
