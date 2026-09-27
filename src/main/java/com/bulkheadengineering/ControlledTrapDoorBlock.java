package com.bulkheadengineering;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

/** Preserve vanilla placement, redstone and waterlogging; gate player toggling. */
public final class ControlledTrapDoorBlock extends TrapDoorBlock {
 public ControlledTrapDoorBlock(Properties properties,BlockSetType type){super(properties,type);}
 @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
  if(DoorConfig.REDSTONE_ONLY.get())return InteractionResult.sidedSuccess(level.isClientSide);
  return super.use(state,level,pos,player,hand,hit);
 }
}
