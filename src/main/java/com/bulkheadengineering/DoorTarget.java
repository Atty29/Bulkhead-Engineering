package com.bulkheadengineering;
import com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
/** Read-only ownership resolution shared by Pick Block and information overlays. */
public final class DoorTarget {
 public static final String STYLE="DoorVariant";
 private DoorTarget(){}
 public static DoorBlockEntity resolve(BlockGetter level,BlockPos pos){
  if(level instanceof Level world&&!world.hasChunkAt(pos))return null;
  var state=level.getBlockState(pos);var entity=level.getBlockEntity(pos);
  if(state.getBlock() instanceof AnimatedDoorBlock&&entity instanceof DoorBlockEntity door)return door;
  if(!(state.getBlock() instanceof DoorPartBlock)||!(entity instanceof DoorPartEntity part)||part.controller==null||part.controller.equals(pos))return null;
  if(level instanceof Level world&&!world.hasChunkAt(part.controller))return null;
  var masterState=level.getBlockState(part.controller);
  if(!(masterState.getBlock() instanceof AnimatedDoorBlock block)||!(level.getBlockEntity(part.controller) instanceof DoorBlockEntity door))return null;
  // A corrupt pointer must not claim an unrelated door or an arbitrary position.
  if(!block.offsets().contains(part.local)||part.local.equals(BlockPos.ZERO)||!part.controller.offset(AnimatedDoorBlock.rotate(part.local,AnimatedDoorBlock.facing(masterState))).equals(pos))return null;
  return door;
 }
 public static int style(AnimatedDoorBlock block,int value){return Math.floorMod(value,DoorVariants.forDoor(block.id).size());}
 public static int style(AnimatedDoorBlock block,ItemStack stack){return style(block,stack.hasTag()?stack.getTag().getInt(STYLE):0);}
 public static ItemStack pick(BlockGetter level,BlockPos pos){var door=resolve(level,pos);if(door==null)return ItemStack.EMPTY;var block=(AnimatedDoorBlock)door.getBlockState().getBlock();var result=new ItemStack(block);int variant=style(block,door.variant);if(variant!=0)result.getOrCreateTag().putInt(STYLE,variant);return result;}
}
