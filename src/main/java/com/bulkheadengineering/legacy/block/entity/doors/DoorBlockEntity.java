package com.bulkheadengineering.legacy.block.entity.doors;
import com.bulkheadengineering.*;
import com.bulkheadengineering.legacy.client.model.variant.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
public class DoorBlockEntity extends BlockEntity {
 public byte state=0;private int openTicks=0;private int previousTicks=0;private boolean powered=false;
 public int variant=0;
 public DoorBlockEntity(BlockPos p,BlockState s){super(Doors.DOOR_ENTITY.get(),p,s);}
 public DoorDecl getDoorDecl(){return ((AnimatedDoorBlock)getBlockState().getBlock()).decl();}
 public int getOpenTicks(){return openTicks;}
 public float animation(float partial){return previousTicks+(openTicks-previousTicks)*partial;}
 public boolean isModularBlastDoor(){return getDoorDecl() instanceof ModularBlastDoorDecl;}
 public void toggle(){
  if(state>1)return;
  if(isModularBlastDoor()&&level!=null&&!level.isClientSide){
   boolean opening=state==0;
   // Iterative traversal avoids the 1.12 recursive stack overflow on wide walls.
   java.util.Set<BlockPos> visited=new java.util.HashSet<>();
   java.util.ArrayDeque<BlockPos> queue=new java.util.ArrayDeque<>();queue.add(worldPosition);
   while(!queue.isEmpty()){
    BlockPos p=queue.removeFirst();if(!visited.add(p)||!level.hasChunkAt(p))continue;
    if(!(level.getBlockEntity(p) instanceof DoorBlockEntity other)||!other.isModularBlastDoor())continue;
    if(other.state==(opening?0:1))other.move(opening);
    for(Direction direction:Direction.Plane.HORIZONTAL)queue.add(p.relative(direction));
   }
  }else if(state==0)move(true);else if(state==1)move(false);
 }
 private void sound(SoundEvent s){if(s!=null&&level!=null)level.playSound(null,worldPosition,s,SoundSource.BLOCKS,getDoorDecl().getSoundVolume(),isModularBlastDoor()&&s==getDoorDecl().getOpenSoundStart()?.75f:1);}
 private void move(boolean open){state=(byte)(open?3:2);sound(open?getDoorDecl().getOpenSoundStart():getDoorDecl().getCloseSoundStart());sync();}
 public void cycleVariant(Player player){if(state>1)return;variant=(variant+1)%DoorVariants.forDoor(((AnimatedDoorBlock)getBlockState().getBlock()).id).size();sync();player.displayClientMessage(net.minecraft.network.chat.Component.literal("Door style: "+DoorVariants.forDoor(((AnimatedDoorBlock)getBlockState().getBlock()).id).get(variant).name()),true);}
 public static void tick(Level l,BlockPos p,BlockState s,DoorBlockEntity b){
  b.previousTicks=b.openTicks;
  if(l.isClientSide)Client.tick(b);
  if(!l.isClientSide&&(b.isModularBlastDoor()||l.getGameTime()%2==0)){
   boolean signal=l.hasNeighborSignal(p);AnimatedDoorBlock block=(AnimatedDoorBlock)s.getBlock();
   if(b.isModularBlastDoor())signal|=l.hasNeighborSignal(p.above(6));
   else for(BlockPos o:block.offsets()){BlockPos q=p.offset(AnimatedDoorBlock.rotate(o,AnimatedDoorBlock.facing(s)));if(l.hasNeighborSignal(q)){signal=true;break;}}
   if(signal!=b.powered){b.powered=signal;if(b.isModularBlastDoor()){if(signal)b.toggle();}else if(signal&&(b.state==0||b.state==2))b.move(true);else if(!signal&&(b.state==1||(b.state==3&&b.getDoorDecl().getOpenTime()<=25)))b.move(false);b.setChanged();}
  }
  int duration=b.getDoorDecl().getOpenTime();
  if(b.state==3)b.openTicks=Math.min(duration,b.openTicks+1);else if(b.state==2)b.openTicks=Math.max(0,b.openTicks-1);
  if(!l.isClientSide&&b.state>1){
   b.getDoorDecl().onTick(b);
   if(b.openTicks==duration&&b.state==3){b.state=1;b.sound(b.getDoorDecl().getOpenSoundEnd());b.sync();}
   else if(b.openTicks==0&&b.state==2){b.state=0;b.sound(b.getDoorDecl().getCloseSoundEnd());b.sync();}
   b.setChanged();
  }
 }
 public void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 protected void saveAdditional(CompoundTag t){super.saveAdditional(t);t.putByte("state",state);t.putInt("ticks",openTicks);t.putBoolean("powered",powered);t.putInt("variant",variant);}
 public void load(CompoundTag t){super.load(t);state=t.getByte("state");openTicks=t.getInt("ticks");previousTicks=openTicks;powered=t.getBoolean("powered");variant=t.getInt("variant");}
 public CompoundTag getUpdateTag(){return saveWithoutMetadata();}
 public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(16);}
}
