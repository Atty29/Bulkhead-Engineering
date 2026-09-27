package com.hbm_doors.mobility;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import java.util.*;

/** Adapted from MTR BlockEscalatorBase/Step/Side and ItemEscalator (MIT). */
public final class EscalatorBlock extends Block {
 public enum Side implements StringRepresentable {LEFT,RIGHT;public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}}
 public enum Orientation implements StringRepresentable {LANDING_BOTTOM,LANDING_TOP,FLAT,SLOPE,TRANSITION_BOTTOM,TRANSITION_TOP;public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}}
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final EnumProperty<Side> SIDE=EnumProperty.create("side",Side.class);
 public static final EnumProperty<Orientation> ORIENTATION=EnumProperty.create("orientation",Orientation.class);
 public static final BooleanProperty DIRECTION=BooleanProperty.create("direction"),STATUS=BooleanProperty.create("status");
 public final boolean railing;
 public EscalatorBlock(boolean railing){super(Properties.copy(Blocks.IRON_BLOCK).strength(3).noOcclusion());this.railing=railing;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(SIDE,Side.LEFT).setValue(ORIENTATION,Orientation.LANDING_BOTTOM).setValue(DIRECTION,true).setValue(STATUS,true));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,SIDE,ORIENTATION,DIRECTION,STATUS);}
 public static Direction across(BlockState s){return s.getValue(SIDE)==Side.LEFT?s.getValue(FACING).getClockWise():s.getValue(FACING).getCounterClockWise();}
 private boolean matches(BlockGetter l,BlockPos p,BlockState s){var n=l.getBlockState(p);return n.is(this)&&n.getValue(FACING)==s.getValue(FACING)&&n.getValue(SIDE)==s.getValue(SIDE);}
 private Orientation orientation(BlockGetter l,BlockPos p,BlockState s){var f=s.getValue(FACING);boolean a=matches(l,p.relative(f),s),b=matches(l,p.relative(f.getOpposite()),s),au=matches(l,p.relative(f).above(),s),bd=matches(l,p.relative(f.getOpposite()).below(),s);return a&&b?Orientation.FLAT:au&&bd?Orientation.SLOPE:au&&b?Orientation.TRANSITION_BOTTOM:a&&bd?Orientation.TRANSITION_TOP:b?Orientation.LANDING_TOP:Orientation.LANDING_BOTTOM;}
 public BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos p,BlockPos q){return s.setValue(ORIENTATION,orientation(l,p,s));}
 // A rising section is diagonally adjacent, outside vanilla's six neighbor updates.
 private static void refreshNearby(Level l,BlockPos p){if(l.isClientSide)return;for(var q:BlockPos.betweenClosed(p.offset(-1,-1,-1),p.offset(1,1,1))){var state=l.getBlockState(q);if(state.getBlock() instanceof EscalatorBlock b){var next=state.setValue(ORIENTATION,b.orientation(l,q,state));if(next!=state)l.setBlock(q,next,2);}}}
 public void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){super.onPlace(s,l,p,old,moving);refreshNearby(l,p);}
 public void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos q,boolean moving){refreshNearby(l,p);}
 public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
  var o=s.getValue(ORIENTATION);boolean slope=o==Orientation.SLOPE||o==Orientation.TRANSITION_TOP;
  VoxelShape shape=railing?Block.box(s.getValue(SIDE)==Side.RIGHT?12:0,0,o==Orientation.LANDING_TOP?8:0,s.getValue(SIDE)==Side.RIGHT?16:4,16,o==Orientation.LANDING_BOTTOM?8:16):slope?Shapes.or(Block.box(0,0,0,16,8,16),Block.box(0,8,0,16,15,8)):Block.box(0,0,0,16,15,16);
  return MobilityShapes.rotate(shape,s.getValue(FACING));
 }
 public void entityInside(BlockState s,Level l,BlockPos p,Entity e){if(!railing&&s.getValue(STATUS)&&!e.isShiftKeyDown()){var f=s.getValue(FACING);double v=s.getValue(DIRECTION)?.10:-.10;var old=e.getDeltaMovement();e.setDeltaMovement(old.x*.25+f.getStepX()*v,old.y,old.z*.25+f.getStepZ()*v);}}
 public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
  if(!player.getItemInHand(hand).is(Mobility.WRENCH.get()))return InteractionResult.PASS;
  if(!l.isClientSide){BlockPos start=railing?p.below():p;var st=l.getBlockState(start);if(st.getBlock() instanceof EscalatorBlock){boolean running=!st.getValue(STATUS)||st.getValue(DIRECTION),forward=!st.getValue(STATUS);Set<BlockPos> seen=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();queue.add(start);
   while(!queue.isEmpty()&&seen.size()<4096){var q=queue.removeFirst();if(!seen.add(q)||!l.hasChunkAt(q))continue;var n=l.getBlockState(q);if(!(n.getBlock() instanceof EscalatorBlock)||n.getValue(FACING)!=s.getValue(FACING))continue;l.setBlock(q,n.setValue(STATUS,running).setValue(DIRECTION,forward),3);for(Direction d:Direction.values()){queue.add(q.relative(d));if(d.getAxis().isHorizontal()){queue.add(q.relative(d).above());queue.add(q.relative(d).below());}}}
   player.displayClientMessage(net.minecraft.network.chat.Component.literal(running?(forward?"Escalator: forward":"Escalator: reverse"):"Escalator: stopped"),true);
  }}return InteractionResult.sidedSuccess(l.isClientSide);
 }
 public void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock())&&!l.isClientSide){var other=p.relative(across(s));var vertical=railing?p.below():p.above();if(l.getBlockState(other).is(this))l.removeBlock(other,false);if(l.getBlockState(vertical).getBlock() instanceof EscalatorBlock b&&b.railing!=railing)l.removeBlock(vertical,false);}super.onRemove(s,l,p,n,moving);}
 public ItemStack getCloneItemStack(BlockGetter l,BlockPos p,BlockState s){return new ItemStack(Mobility.ESCALATOR.get());}
 public static final class Placer extends Item {
  public Placer(){super(new Properties());}
  public InteractionResult useOn(UseOnContext c){var l=c.getLevel();var f=c.getHorizontalDirection();var p=l.getBlockState(c.getClickedPos()).canBeReplaced()?c.getClickedPos():c.getClickedPos().relative(c.getClickedFace());var q=p.relative(f.getClockWise());
   for(var pos:List.of(p,q,p.above(),q.above()))if(l.isOutsideBuildHeight(pos)||!l.getWorldBorder().isWithinBounds(pos)||!l.getBlockState(pos).canBeReplaced())return InteractionResult.FAIL;
   if(!l.isClientSide){for(int i=0;i<2;i++)for(int y=0;y<2;y++){var pos=(i==0?p:q).above(y);var b=y==0?Mobility.ESCALATOR_STEP.get():Mobility.ESCALATOR_SIDE.get();l.setBlock(pos,b.defaultBlockState().setValue(FACING,f).setValue(SIDE,i==0?Side.LEFT:Side.RIGHT),2);}for(var pos:List.of(p,q,p.above(),q.above())){var s=l.getBlockState(pos);var b=(EscalatorBlock)s.getBlock();l.setBlock(pos,s.setValue(ORIENTATION,b.orientation(l,pos,s)),3);l.updateNeighborsAt(pos,b);}if(c.getPlayer()==null||!c.getPlayer().isCreative())c.getItemInHand().shrink(1);}return InteractionResult.sidedSuccess(l.isClientSide);
  }
 }
}
