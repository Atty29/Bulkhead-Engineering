package com.bulkheadengineering;
import com.bulkheadengineering.legacy.block.entity.doors.*;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
@GameTestHolder("bulkheadengineering")
@PrefixGameTestTemplate(false)
public class DoorTests {
 @GameTest(template="empty",timeoutTicks=300)
 public static void lifecycle(GameTestHelper h){
  var level=h.getLevel();BlockPos p=h.absolutePos(new BlockPos(10,5,10));int count=0;
  for(var entry:Doors.DOORS.entrySet())if(entry.getValue().get() instanceof AnimatedDoorBlock block){
   for(Direction f:Direction.Plane.HORIZONTAL){
    BlockState state=block.defaultBlockState().setValue(AnimatedDoorBlock.FACING,f);level.setBlock(p,state,3);block.setPlacedBy(level,p,state,null,ItemStack.EMPTY);
    for(BlockPos o:block.offsets())if(!o.equals(BlockPos.ZERO)){
     var entity=level.getBlockEntity(p.offset(AnimatedDoorBlock.rotate(o,f)));
     h.assertTrue(entity instanceof DoorPartEntity&&p.equals(((DoorPartEntity)entity).controller),entry.getKey()+" part placement "+f+" "+o);
    }
    DoorBlockEntity be=(DoorBlockEntity)level.getBlockEntity(p);h.assertTrue(be!=null,"controller exists");
    be.toggle();for(int t=0;t<be.getDoorDecl().getOpenTime();t++)DoorBlockEntity.tick(level,p,state,be);
    h.assertTrue(be.state==1&&be.getOpenTicks()==be.getDoorDecl().getOpenTime(),entry.getKey()+" opens");
    var tag=be.saveWithoutMetadata();DoorBlockEntity restored=new DoorBlockEntity(p,state);restored.load(tag);h.assertTrue(restored.state==1&&restored.getOpenTicks()==be.getOpenTicks(),"NBT round trip");
    be.toggle();for(int t=0;t<be.getDoorDecl().getOpenTime();t++)DoorBlockEntity.tick(level,p,state,be);h.assertTrue(be.state==0&&be.getOpenTicks()==0,entry.getKey()+" closes");
    level.removeBlock(p,false);
    for(BlockPos o:block.offsets())h.assertTrue(level.getBlockState(p.offset(AnimatedDoorBlock.rotate(o,f))).isAir(),entry.getKey()+" no orphan "+f+" "+o);
    count++;
   }
  }
  h.assertTrue(count==60,"all 15 door entries in four orientations");h.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void secureAccessFrameCollision(GameTestHelper h){
  var level=h.getLevel();BlockPos p=h.absolutePos(new BlockPos(10,5,10));AnimatedDoorBlock block=(AnimatedDoorBlock)Doors.DOORS.get("secure_access_door").get();BlockState s=block.defaultBlockState();
  level.setBlock(p,s,3);block.setPlacedBy(level,p,s,null,ItemStack.EMPTY);DoorBlockEntity be=(DoorBlockEntity)level.getBlockEntity(p);
  h.assertTrue(!AnimatedDoorBlock.shape(s,level,p,new BlockPos(2,1,0)).isEmpty(),"secure access right jamb collides closed");
  h.assertTrue(!AnimatedDoorBlock.shape(s,level,p,new BlockPos(-2,2,0)).isEmpty(),"secure access left jamb collides closed");
  be.state=1;
  h.assertTrue(!AnimatedDoorBlock.shape(s,level,p,new BlockPos(2,1,0)).isEmpty(),"secure access right jamb collides open");
  h.assertTrue(!AnimatedDoorBlock.shape(s,level,p,new BlockPos(-2,2,0)).isEmpty(),"secure access left jamb collides open");
  h.assertTrue(!AnimatedDoorBlock.shape(s,level,p,new BlockPos(0,4,0)).isEmpty(),"secure access header collides open");
  h.assertTrue(AnimatedDoorBlock.shape(s,level,p,new BlockPos(0,1,0)).isEmpty(),"secure access centre passage clears open");
  level.removeBlock(p,false);h.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void redstoneAndPartRemoval(GameTestHelper h){
  var level=h.getLevel();BlockPos p=h.absolutePos(new BlockPos(10,5,10));AnimatedDoorBlock block=(AnimatedDoorBlock)Doors.DOORS.get("sliding_seal_door").get();BlockState s=block.defaultBlockState();
  level.setBlock(p,s,3);block.setPlacedBy(level,p,s,null,ItemStack.EMPTY);DoorBlockEntity be=(DoorBlockEntity)level.getBlockEntity(p);
  level.setBlock(p.below(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
  h.runAfterDelay(30,()->{h.assertTrue(be.state==1,"redstone opens");level.removeBlock(p.below(),false);});
  h.runAfterDelay(60,()->{h.assertTrue(be.state==0,"redstone closes");BlockPos o=block.offsets().stream().filter(q->!q.equals(BlockPos.ZERO)).findFirst().orElseThrow();level.destroyBlock(p.offset(o),false);h.assertTrue(level.getBlockState(p).isAir(),"breaking part removes controller");h.succeed();});
 }
}
