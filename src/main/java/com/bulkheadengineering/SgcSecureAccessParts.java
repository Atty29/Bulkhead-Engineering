package com.bulkheadengineering;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/** Recovery for saved SGC Secure Access doors, without changing other variants. */
public final class SgcSecureAccessParts {
 private SgcSecureAccessParts(){}

 // A client can receive a child block before its controller link packet. Resolve
 // only a uniquely matching SGC controller whose footprint contains this block.
 static BlockPos findController(BlockGetter level,BlockPos partPos){
  BlockPos found=null;
  for(int y=0;y<=4;y++)for(int distance=-2;distance<=2;distance++)for(int axis=0;axis<2;axis++){
   if(axis==1&&distance==0)continue;
   BlockPos p=partPos.offset(axis==0?distance:0,-y,axis==1?distance:0);
   if(p.equals(partPos))continue;
   var state=level.getBlockState(p);
   if(!AnimatedDoorBlock.isSgcSecureAccess(state,level,p))continue;
   var block=(AnimatedDoorBlock)state.getBlock();
   BlockPos local=AnimatedDoorBlock.unrotate(partPos.subtract(p),AnimatedDoorBlock.facing(state));
   if(!block.offsets().contains(local))continue;
   if(found!=null&&!found.equals(p))return null;
   found=p;
  }
  return found;
 }

 public static void repair(Level level,BlockPos controller){
  if(level==null||level.isClientSide)return;
  var state=level.getBlockState(controller);
  if(!AnimatedDoorBlock.isSgcSecureAccess(state,level,controller))return;
  var block=(AnimatedDoorBlock)state.getBlock();
  for(BlockPos local:block.offsets()){
   if(local.equals(BlockPos.ZERO))continue;
   BlockPos p=controller.offset(AnimatedDoorBlock.rotate(local,AnimatedDoorBlock.facing(state)));
   if(!level.hasChunkAt(p))continue;
   // Repair holes, never replace player blocks or steal another door's parts.
   if(level.getBlockState(p).isAir())level.setBlock(p,Doors.PART.get().defaultBlockState(),3);
   if(level.getBlockEntity(p) instanceof DoorPartEntity part
      &&(part.controller==null||controller.equals(part.controller))
      &&(!controller.equals(part.controller)||!local.equals(part.local))){
    part.controller=controller;part.local=local;part.setChanged();
    level.sendBlockUpdated(p,level.getBlockState(p),level.getBlockState(p),3);
   }
  }
 }
}
