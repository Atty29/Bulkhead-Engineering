package com.bulkheadengineering;
import com.bulkheadengineering.legacy.block.entity.doors.*;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.*;
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
 public static void secureAccessSgcFrameCollision(GameTestHelper h){
  var level=h.getLevel();BlockPos p=h.absolutePos(new BlockPos(10,5,10));
  AnimatedDoorBlock block=(AnimatedDoorBlock)Doors.DOORS.get("secure_access_door").get();
  var variants=DoorVariants.forDoor(block.id);
  int sgc=java.util.stream.IntStream.range(0,variants.size()).filter(i->variants.get(i).skin().equals("sgc_blue")).findFirst().orElseThrow();
  for(Direction facing:Direction.Plane.HORIZONTAL){
   BlockState s=block.defaultBlockState().setValue(AnimatedDoorBlock.FACING,facing);
   ItemStack stack=new ItemStack(block);stack.getOrCreateTag().putInt(DoorTarget.STYLE,sgc);
   level.setBlock(p,s,3);block.setPlacedBy(level,p,s,null,stack);
   DoorBlockEntity be=(DoorBlockEntity)level.getBlockEntity(p);
   h.assertTrue(be.variant==sgc,"SGC Blue explicitly selected through placement");
   for(boolean open:new boolean[]{false,true,false}){
    if((be.state==1)!=open){be.toggle();for(int t=0;t<be.getDoorDecl().getOpenTime();t++)DoorBlockEntity.tick(level,p,s,be);}
    h.assertTrue(be.state==(open?1:0),"animation reaches requested state");
    for(BlockPos o:block.offsets()){
     BlockPos q=p.offset(AnimatedDoorBlock.rotate(o,facing));
     // Exercise the real part-block lookup with stale copied local coordinates.
     if(level.getBlockEntity(q) instanceof DoorPartEntity part)part.local=BlockPos.ZERO;
     var actual=level.getBlockState(q).getCollisionShape(level,q);
     boolean frame=Math.abs(o.getX())==2||o.getY()==4;
     h.assertTrue(actual.isEmpty()==(open&&!frame),"SGC collision "+facing+" open="+open+" offset="+o);
     if(frame)h.assertTrue(!Shapes.joinIsNotEmpty(actual,Shapes.block(),BooleanOp.NOT_SAME),"SGC frame is fully solid "+o);
     if(level.getBlockEntity(q) instanceof DoorPartEntity part)part.local=o;
    }
   }
   // Compare every non-SGC variant, including Legacy, to the pre-hotfix profile.
   for(int v=0;v<variants.size();v++)if(v!=sgc){
    be.applyConfiguration(v,be.labelMain,be.labelSub);
    for(boolean open:new boolean[]{false,true}){
     be.state=(byte)(open?1:0);
     for(BlockPos o:block.offsets()){
      var definition=be.getDoorDecl().getStructureDefinition();
      var expected=(open?definition.getOpenShapes():definition.getClosedShapes()).getOrDefault(o,Shapes.empty());
      if(open&&o.getY()==0)expected=Shapes.empty();
      if(open&&o.getY()==3)expected=Block.box(0,6,0,16,16,16);
      if(o.getY()==4)expected=Shapes.empty();
      final var unrotated=expected;final VoxelShape[] rotated={Shapes.empty()};
      unrotated.forAllBoxes((x,y,z,X,Y,Z)->{
       var corners=new double[][]{{x,z},{x,Z},{X,z},{X,Z}};double minX=1,minZ=1,maxX=0,maxZ=0;
       for(var c:corners){double rx=c[0],rz=c[1];switch(facing){case SOUTH:rx=1-c[0];rz=1-c[1];break;case WEST:rx=c[1];rz=1-c[0];break;case EAST:rx=1-c[1];rz=c[0];break;default:break;}
        minX=Math.min(minX,rx);maxX=Math.max(maxX,rx);minZ=Math.min(minZ,rz);maxZ=Math.max(maxZ,rz);}
       rotated[0]=Shapes.or(rotated[0],Shapes.box(minX,y,minZ,maxX,Y,maxZ));
      });
      BlockPos q=p.offset(AnimatedDoorBlock.rotate(o,facing));var actual=level.getBlockState(q).getCollisionShape(level,q);
      h.assertTrue(!Shapes.joinIsNotEmpty(actual,rotated[0],BooleanOp.NOT_SAME),variants.get(v).name()+" retains original collision "+facing+" open="+open+" offset="+o);
     }
    }
   }
   level.removeBlock(p,false);
  }
  h.succeed();
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
