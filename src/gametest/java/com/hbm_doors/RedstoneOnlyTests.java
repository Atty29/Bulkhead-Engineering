package com.hbm_doors;

import com.hbm_doors.legacy.block.entity.doors.DoorBlockEntity;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;

@GameTestHolder("hbm_doors") @PrefixGameTestTemplate(false)
public class RedstoneOnlyTests {
 @GameTest(template="empty",timeoutTicks=200)
 public static void globalControlAllDoors(GameTestHelper h){
  // Direct tick simulation must run on an even world tick (normal power polling cadence).
  if(h.getLevel().getGameTime()%2!=0)h.runAfterDelay(1,()->verify(h));else verify(h);
 }
 private static void verify(GameTestHelper h){
  var level=h.getLevel();var player=FakePlayerFactory.getMinecraft(level);var p=h.absolutePos(new BlockPos(10,5,10));
  boolean previous=DoorConfig.REDSTONE_ONLY.get();
  try {
   int checked=0;
   for(var entry:Doors.DOORS.entrySet()){
    var block=entry.getValue().get();var state=block.defaultBlockState();String id=entry.getKey();
    level.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(p,state,3);block.setPlacedBy(level,p,state,null,ItemStack.EMPTY);
    var hit=new BlockHitResult(Vec3.atCenterOf(p),Direction.NORTH,p,false);
    // Both modes are reversible, including the vanilla-style door/hatch subclasses.
    DoorConfig.REDSTONE_ONLY.set(false);
    state.use(level,player,InteractionHand.MAIN_HAND,hit);tick(level,p);h.assertTrue(open(level,p),id+" manual opens when disabled");
    level.getBlockState(p).use(level,player,InteractionHand.MAIN_HAND,hit);tick(level,p);h.assertTrue(!open(level,p),id+" manual closes when disabled");
    DoorConfig.REDSTONE_ONLY.set(true);
    for(var hand:InteractionHand.values())level.getBlockState(p).use(level,player,hand,hit);
    if(level.getBlockState(p.above()).getBlock() instanceof DoorPartBlock||block instanceof DoorBlock){
     var upperHit=new BlockHitResult(Vec3.atCenterOf(p.above()),Direction.NORTH,p.above(),false);
     level.getBlockState(p.above()).use(level,player,InteractionHand.MAIN_HAND,upperHit);
    }
    tick(level,p);h.assertTrue(!open(level,p),id+" blocks hand opening, including upper/frame parts");
    level.setBlock(p.below(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);tick(level,p);h.assertTrue(open(level,p),id+" still opens with power");
    for(var hand:InteractionHand.values())level.getBlockState(p).use(level,player,hand,hit);
    tick(level,p);h.assertTrue(open(level,p),id+" blocks hand closing");
    boolean modular=level.getBlockEntity(p) instanceof DoorBlockEntity be&&be.isModularBlastDoor();
    level.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);tick(level,p);
    if(modular){h.assertTrue(open(level,p),id+" falling edge retains pulse behavior");level.setBlock(p.below(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);tick(level,p);}
    h.assertTrue(!open(level,p),id+" closes with redstone");
    level.removeBlock(p,false);level.removeBlock(p.above(),false);level.removeBlock(p.below(),false);checked++;
   }
   h.assertTrue(checked==21,"all 21 registered door/hatch entries covered");h.succeed();
  }finally{DoorConfig.REDSTONE_ONLY.set(previous);}
 }
 private static void tick(net.minecraft.server.level.ServerLevel level,BlockPos p){
  if(level.getBlockEntity(p) instanceof DoorBlockEntity be)for(int t=0;t<=be.getDoorDecl().getOpenTime();t++)DoorBlockEntity.tick(level,p,level.getBlockState(p),be);
 }
 private static boolean open(net.minecraft.server.level.ServerLevel level,BlockPos p){
  return level.getBlockEntity(p) instanceof DoorBlockEntity be?be.state==1:level.getBlockState(p).getValue(BlockStateProperties.OPEN);
 }
}
