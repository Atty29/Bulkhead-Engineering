package com.bulkheadengineering;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
public class DoorPartBlock extends BaseEntityBlock {
 public DoorPartBlock(){super(Properties.copy(Blocks.IRON_BLOCK).strength(10,1000).requiresCorrectToolForDrops().noOcclusion().dynamicShape().isViewBlocking((s,l,p)->false).noLootTable());}
 @Override public net.minecraft.world.item.ItemStack getCloneItemStack(BlockState state,net.minecraft.world.phys.HitResult target,BlockGetter level,BlockPos pos,Player player){return DoorTarget.pick(level,pos);}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new DoorPartEntity(p,s);}
 public RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
 public float getDestroyProgress(BlockState s,Player player,BlockGetter l,BlockPos p){
  if(l.getBlockEntity(p) instanceof DoorPartEntity part&&part.controller!=null){var ctrl=l.getBlockState(part.controller);if(ctrl.getBlock() instanceof AnimatedDoorBlock)return ctrl.getDestroyProgress(player,l,part.controller);}
  return super.getDestroyProgress(s,player,l,p);
 }
 public float getExplosionResistance(BlockState s,BlockGetter l,BlockPos p,net.minecraft.world.level.Explosion explosion){
  if(l.getBlockEntity(p) instanceof DoorPartEntity part&&part.controller!=null&&l.getBlockEntity(part.controller) instanceof com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity door&&door.isModularBlastDoor())return 18000;
  return super.getExplosionResistance(s,l,p,explosion);
 }
 public void playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){
  if(!l.isClientSide&&l.getBlockEntity(p) instanceof DoorPartEntity part&&part.controller!=null){BlockPos ctrl=part.controller;part.controller=null;if(l.getBlockState(ctrl).getBlock() instanceof AnimatedDoorBlock)l.destroyBlock(ctrl,!player.isCreative()&&player.hasCorrectToolForDrops(l.getBlockState(ctrl)));}
  super.playerWillDestroy(l,p,s,player);
 }
 private VoxelShape linkedShape(BlockGetter level,BlockPos partPos,boolean outline){
  if(level.getBlockEntity(partPos) instanceof DoorPartEntity part&&part.controller!=null){
   BlockState controllerState=level.getBlockState(part.controller);
   if(controllerState.getBlock() instanceof AnimatedDoorBlock){
    // Derive the local coordinate from the actual world position instead of
    // trusting the copied block-entity 'local' value. This keeps collision
    // correct even if a client receives the part before its BE data packet.
    BlockPos worldDelta=partPos.subtract(part.controller);
    BlockPos local=AnimatedDoorBlock.unrotate(worldDelta,AnimatedDoorBlock.facing(controllerState));
    return outline
     ?AnimatedDoorBlock.outline(controllerState,level,part.controller,local)
     :AnimatedDoorBlock.shape(controllerState,level,part.controller,local);
   }
  }
  // A linked multiblock part must never become a temporary walk-through block
  // while controller data is loading/syncing. Solid fallback is the safe state.
  return Shapes.block();
 }
 public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return linkedShape(l,p,true);}
 public VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return linkedShape(l,p,false);}
 public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
  if(l.getBlockEntity(p) instanceof DoorPartEntity be&&be.controller!=null){BlockState ctrl=l.getBlockState(be.controller);return ctrl.use(l,player,hand,new BlockHitResult(hit.getLocation(),hit.getDirection(),be.controller,hit.isInside()));}return InteractionResult.PASS;
 }
 public void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){
  if(!s.is(next.getBlock())&&!l.isClientSide&&l.getBlockEntity(p) instanceof DoorPartEntity part&&part.controller!=null){BlockPos ctrl=part.controller;part.controller=null;if(l.getBlockState(ctrl).getBlock() instanceof AnimatedDoorBlock)l.destroyBlock(ctrl,true);}
  super.onRemove(s,l,p,next,moving);
 }
}
