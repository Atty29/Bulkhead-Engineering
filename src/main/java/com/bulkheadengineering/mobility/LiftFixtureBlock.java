package com.bulkheadengineering.mobility;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** MTR call buttons, two/three-wide panels and automatic landing doors. */
public final class LiftFixtureBlock extends BaseEntityBlock {
 public enum Kind {BUTTON,PANEL,DOOR}
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final EnumProperty<EscalatorBlock.Side> SIDE=EscalatorBlock.SIDE;
 public static final BooleanProperty ODD=BooleanProperty.create("odd");
 public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
 public final Kind kind;public final boolean odd;public final int style;
 public LiftFixtureBlock(Kind kind,boolean odd,int style){super(Properties.copy(Blocks.IRON_BLOCK).strength(3).noOcclusion().dynamicShape());this.kind=kind;this.odd=odd;this.style=style;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(SIDE,EscalatorBlock.Side.LEFT).setValue(ODD,false).setValue(HALF,DoubleBlockHalf.LOWER));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,SIDE,ODD,HALF);}
 public int width(){return kind==Kind.BUTTON?1:odd?3:2;}public int height(){return kind==Kind.DOOR?2:1;}
 public RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new LiftStationEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return createTickerHelper(t,Mobility.STATION_ENTITY.get(),LiftStationEntity::tick);}
 public BlockPos root(BlockPos p,BlockState s){int col=s.getValue(ODD)?1:s.getValue(SIDE)==EscalatorBlock.Side.RIGHT?width()-1:0;return p.relative(s.getValue(FACING).getClockWise(),-col).below(s.getValue(HALF)==DoubleBlockHalf.UPPER?1:0);}
 public BlockState getStateForPlacement(BlockPlaceContext c){var f=c.getHorizontalDirection();for(int x=0;x<width();x++)for(int y=0;y<height();y++){var p=c.getClickedPos().relative(f.getClockWise(),x).above(y);if(c.getLevel().isOutsideBuildHeight(p)||!c.getLevel().getWorldBorder().isWithinBounds(p)||!c.getLevel().getBlockState(p).canBeReplaced(c))return null;}return defaultBlockState().setValue(FACING,f);}
 public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity e,ItemStack item){if(l.isClientSide)return;for(int x=0;x<width();x++)for(int y=0;y<height();y++)if(x+y>0)l.setBlock(p.relative(s.getValue(FACING).getClockWise(),x).above(y),s.setValue(SIDE,x==width()-1?EscalatorBlock.Side.RIGHT:EscalatorBlock.Side.LEFT).setValue(ODD,odd&&x==1).setValue(HALF,y==1?DoubleBlockHalf.UPPER:DoubleBlockHalf.LOWER),3);}
 public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return MobilityShapes.rotate(kind==Kind.BUTTON?Block.box(4,0,0,12,16,1):Block.box(0,0,0,16,16,kind==Kind.PANEL&&style==2?1:4),s.getValue(FACING));}
 public VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){if(kind==Kind.DOOR&&l.getBlockEntity(p) instanceof LiftStationEntity be&&be.doorProgress>.95)return Shapes.empty();return getShape(s,l,p,c);}
 public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
  var held=player.getItemInHand(hand);if(Mobility.isLinkTool(held))return InteractionResult.PASS;
  if(!l.isClientSide&&l.getBlockEntity(p) instanceof LiftStationEntity be){if(held.is(Mobility.WRENCH.get())&&player.mayBuild()){boolean locked=!be.locked;var base=root(p,s);for(int x=0;x<width();x++)for(int y=0;y<height();y++)if(l.getBlockEntity(base.relative(s.getValue(FACING).getClockWise(),x).above(y)) instanceof LiftStationEntity part){part.locked=locked;part.sync();}player.displayClientMessage(net.minecraft.network.chat.Component.literal(be.locked?"Lift controls locked":"Lift controls unlocked"),true);}else if(!be.locked){if(kind==Kind.BUTTON)be.call();else if(kind==Kind.PANEL&&be.car()!=null)LiftNetwork.open(player,p,be.car(),"select");}}
  return InteractionResult.sidedSuccess(l.isClientSide);
 }
 public void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos q,boolean moving){if(!l.isClientSide&&l.getBlockEntity(p) instanceof LiftStationEntity be)be.powerChanged();}
 public void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock())&&!l.isClientSide){var root=root(p,s);for(int x=0;x<width();x++)for(int y=0;y<height();y++){var q=root.relative(s.getValue(FACING).getClockWise(),x).above(y);if(!q.equals(p)&&l.getBlockState(q).is(this))l.removeBlock(q,false);}}super.onRemove(s,l,p,n,moving);}
}
