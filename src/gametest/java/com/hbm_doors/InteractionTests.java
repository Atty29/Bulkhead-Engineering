package com.hbm_doors;
import com.hbm_doors.legacy.block.entity.doors.DoorBlockEntity;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.TrapDoorBlock;
@GameTestHolder("hbm_doors") @PrefixGameTestTemplate(false)
public class InteractionTests {
 @GameTest(template="empty",timeoutTicks=300)
 public static void openDoorsRemainClickableAndPassable(GameTestHelper h){
  boolean previous=DoorConfig.REDSTONE_ONLY.get();DoorConfig.REDSTONE_ONLY.set(false);try {
  var level=h.getLevel();var player=FakePlayerFactory.getMinecraft(level);BlockPos p=h.absolutePos(new BlockPos(10,5,10));
  for(String id:new String[]{"qe_sliding_door","sliding_seal_door","secure_access_door","fire_door","large_vehicle_door","water_door"})for(Direction facing:Direction.Plane.HORIZONTAL){
   var block=(AnimatedDoorBlock)Doors.DOORS.get(id).get();var state=block.defaultBlockState().setValue(AnimatedDoorBlock.FACING,facing);
   level.setBlock(p.below(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
   player.setYRot(facing.getOpposite().toYRot());player.setPos(p.getX()+.5,p.getY(),p.getZ()+3);
   var placement=new net.minecraft.world.item.context.BlockPlaceContext(player,InteractionHand.MAIN_HAND,new ItemStack(block),new BlockHitResult(Vec3.atCenterOf(p.below()).add(0,.5,0),Direction.UP,p.below(),false));
   h.assertTrue(((net.minecraft.world.item.BlockItem)block.asItem()).place(placement).consumesAction(),id+" item placement succeeds");
   h.assertTrue(level.getBlockState(p).getValue(AnimatedDoorBlock.FACING)==facing,id+" faces the placing player");
   var be=(DoorBlockEntity)level.getBlockEntity(p);
   be.toggle();for(int i=0;i<be.getDoorDecl().getOpenTime();i++)DoorBlockEntity.tick(level,p,state,be);
   // A player's 0.6 x 1.8 box must fit through the center, including the lower row.
   double cx=p.getX()+.5,cz=p.getZ()+.5;
   h.assertTrue(level.noCollision(player,new AABB(cx-.29,p.getY()+(id.equals("fire_door")?.2:.1),cz-.29,cx+.29,p.getY()+2,cz+.29)),id+" open passage "+facing);
   if(id.equals("qe_sliding_door")||id.equals("sliding_seal_door")){
    // Ray at the visible upper frame, from both sides. This uses Minecraft's real outline raycast.
    for(int side:new int[]{-1,1}){
     Vec3 a=world(p,facing,.03,1.97,side*2),b=world(p,facing,.03,1.97,-side*2);
     var hit=level.clip(new ClipContext(a,b,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,player));
     h.assertTrue(hit.getType()==HitResult.Type.BLOCK,id+" open frame can be targeted "+facing);
     var result=level.getBlockState(hit.getBlockPos()).use(level,player,InteractionHand.MAIN_HAND,hit);
     h.assertTrue(result.consumesAction()&&be.state==2,id+" frame closes door");
     for(int i=0;i<be.getDoorDecl().getOpenTime();i++)DoorBlockEntity.tick(level,p,state,be);
     h.assertTrue(be.state==0,id+" fully closed after click");be.toggle();for(int i=0;i<be.getDoorDecl().getOpenTime();i++)DoorBlockEntity.tick(level,p,state,be);
    }
   }
   level.removeBlock(p,false);level.removeBlock(p.below(),false);
  }h.succeed();
  } finally {DoorConfig.REDSTONE_ONLY.set(previous);}
 }
 private static Vec3 world(BlockPos p,Direction f,double x,double y,double z){double a=x-.5,b=z;return switch(f){case SOUTH->new Vec3(p.getX()+.5-a,p.getY()+y,p.getZ()+.5-b);case WEST->new Vec3(p.getX()+.5+b,p.getY()+y,p.getZ()+.5-a);case EAST->new Vec3(p.getX()+.5-b,p.getY()+y,p.getZ()+.5+a);default->new Vec3(p.getX()+x,p.getY()+y,p.getZ()+.5+b);};}
 @GameTest(template="empty",timeoutTicks=100)
 public static void hatchesActuallyOpen(GameTestHelper h){
  boolean previous=DoorConfig.REDSTONE_ONLY.get();DoorConfig.REDSTONE_ONLY.set(false);try {
  var l=h.getLevel();var p=h.absolutePos(new BlockPos(10,5,10));var player=FakePlayerFactory.getMinecraft(l);
  for(String id:new String[]{"fusion_hatch","seal_hatch","trapdoor_steel"}){
   var block=Doors.DOORS.get(id).get();h.assertTrue(block instanceof TrapDoorBlock,id+" uses hatch behavior");
   l.setBlock(p,block.defaultBlockState(),3);var hit=new BlockHitResult(Vec3.atCenterOf(p),Direction.UP,p,false);
   l.getBlockState(p).use(l,player,InteractionHand.MAIN_HAND,hit);h.assertTrue(l.getBlockState(p).getValue(TrapDoorBlock.OPEN),id+" opens by hand");
   l.getBlockState(p).use(l,player,InteractionHand.MAIN_HAND,hit);h.assertTrue(!l.getBlockState(p).getValue(TrapDoorBlock.OPEN),id+" closes by hand");
   l.setBlock(p.below(),net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState(),3);h.assertTrue(l.getBlockState(p).getValue(TrapDoorBlock.OPEN),id+" redstone opens");
   l.removeBlock(p.below(),false);h.assertTrue(!l.getBlockState(p).getValue(TrapDoorBlock.OPEN),id+" redstone closes");l.removeBlock(p,false);
  }h.succeed();
  } finally {DoorConfig.REDSTONE_ONLY.set(previous);}
 }
}
