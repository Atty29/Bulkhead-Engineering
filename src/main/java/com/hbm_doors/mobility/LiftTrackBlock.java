package com.hbm_doors.mobility;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import java.util.*;

/** MTR's vertical, horizontal, corner and floor track connectivity in Forge coordinates. */
public final class LiftTrackBlock extends BaseEntityBlock {
 public enum Kind {VERTICAL,HORIZONTAL,DIAGONAL,FLOOR}
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
 public static final EnumProperty<EscalatorBlock.Side> SIDE=EscalatorBlock.SIDE;
 public final Kind kind;
 public LiftTrackBlock(Kind kind){super(Properties.copy(Blocks.IRON_BLOCK).strength(3).noOcclusion());this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(HALF,DoubleBlockHalf.LOWER).setValue(SIDE,EscalatorBlock.Side.LEFT));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,HALF,SIDE);}
 public RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return kind==Kind.FLOOR?new LiftStationEntity(p,s):null;}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return kind==Kind.FLOOR?createTickerHelper(t,Mobility.STATION_ENTITY.get(),LiftStationEntity::tick):null;}
 public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection()).setValue(HALF,c.getClickLocation().y-c.getClickedPos().getY()>.5?DoubleBlockHalf.UPPER:DoubleBlockHalf.LOWER).setValue(SIDE,c.getPlayer()!=null&&c.getPlayer().isShiftKeyDown()?EscalatorBlock.Side.RIGHT:EscalatorBlock.Side.LEFT);}
 public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return MobilityShapes.rotate(kind==Kind.FLOOR?Block.box(0,0,0,16,16,1):kind==Kind.HORIZONTAL?Block.box(0,6,0,16,10,1):kind==Kind.DIAGONAL?Shapes.or(Block.box(6,0,0,10,16,1),Block.box(0,6,0,16,10,1)):Block.box(6,0,0,10,16,1),s.getValue(FACING));}
 public List<Direction> connections(BlockState s){var f=s.getValue(FACING);return switch(kind){case VERTICAL->List.of(Direction.UP,Direction.DOWN);case HORIZONTAL->List.of(f.getClockWise(),f.getCounterClockWise());case FLOOR->List.of(Direction.UP,Direction.DOWN,f.getClockWise(),f.getCounterClockWise());case DIAGONAL->List.of(s.getValue(HALF)==DoubleBlockHalf.UPPER?Direction.UP:Direction.DOWN,s.getValue(SIDE)==EscalatorBlock.Side.RIGHT?f.getClockWise():f.getCounterClockWise());};}
 public Vec3 point(BlockPos p,BlockState s){var v=new Vec3(p.getX()+.5,p.getY(),p.getZ()+.5);if(kind==Kind.DIAGONAL){var f=s.getValue(SIDE)==EscalatorBlock.Side.RIGHT?s.getValue(FACING).getClockWise():s.getValue(FACING).getCounterClockWise();v=v.add(f.getStepX()*.25,s.getValue(HALF)==DoubleBlockHalf.UPPER?.25:-.25,f.getStepZ()*.25);}return v;}
 public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(player.getItemInHand(hand).is(Mobility.REFRESHER.get())||Mobility.isLinkTool(player.getItemInHand(hand)))return InteractionResult.PASS;if(kind==Kind.FLOOR&&player.getItemInHand(hand).is(Mobility.WRENCH.get())){if(!l.isClientSide)LiftNetwork.open(player,p,null,"floor");return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;}
 public void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos q,boolean moving){if(!l.isClientSide&&kind==Kind.FLOOR&&l.getBlockEntity(p) instanceof LiftStationEntity station)station.powerChanged();}
}
