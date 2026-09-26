package com.hbm_doors;
import com.hbm_doors.legacy.block.entity.doors.*;
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
import java.util.*;

public class AnimatedDoorBlock extends BaseEntityBlock {
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public final String id;
 public AnimatedDoorBlock(String id){super(Properties.copy(Blocks.IRON_BLOCK).strength(10,1000).requiresCorrectToolForDrops().noOcclusion().dynamicShape().isViewBlocking((s,l,p)->false));this.id=id;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
 public DoorDecl decl(){return DoorDeclRegistry.getById(id);}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING);}
 public RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new DoorBlockEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return createTickerHelper(t,Doors.DOOR_ENTITY.get(),DoorBlockEntity::tick);}
 public static BlockPos rotate(BlockPos p,Direction f){return switch(f){case SOUTH->new BlockPos(-p.getX(),p.getY(),-p.getZ());case WEST->new BlockPos(p.getZ(),p.getY(),-p.getX());case EAST->new BlockPos(-p.getZ(),p.getY(),p.getX());default->p;};}
 public static Direction facing(BlockState s){return s.getValue(FACING);}
 public Set<BlockPos> offsets(){return decl().getStructureDefinition().getClosedShapes().keySet();}
 public BlockState getStateForPlacement(BlockPlaceContext c){
  Direction f=c.getHorizontalDirection().getOpposite();
  for(BlockPos o:offsets()){
   BlockPos p=c.getClickedPos().offset(rotate(o,f));
   if(c.getLevel().isOutsideBuildHeight(p)||!c.getLevel().getWorldBorder().isWithinBounds(p)||!c.getLevel().getBlockState(p).canBeReplaced(c))return null;
  }return defaultBlockState().setValue(FACING,f);
 }
 public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){
  if(l.isClientSide)return;
  for(BlockPos o:offsets())if(!o.equals(BlockPos.ZERO)){
   BlockPos q=p.offset(rotate(o,facing(s)));l.setBlock(q,Doors.PART.get().defaultBlockState(),3);
   if(l.getBlockEntity(q) instanceof DoorPartEntity part){part.controller=p;part.local=o;part.setChanged();l.sendBlockUpdated(q,l.getBlockState(q),l.getBlockState(q),3);}
  }
 }
 public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
  if(l.getBlockEntity(p) instanceof DoorBlockEntity be){if(!l.isClientSide){if(player.isShiftKeyDown())be.cycleVariant(player);else be.toggle();}return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;
 }
 public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s,l,p,BlockPos.ZERO);}
 public static VoxelShape shape(BlockState s,BlockGetter l,BlockPos p,BlockPos offset){
  // Neighbor shape queries can occur after the controller state was removed,
  // before its old block entity and linked parts finish being detached.
  if(!(s.getBlock() instanceof AnimatedDoorBlock))return Shapes.empty();
  if(!(l.getBlockEntity(p) instanceof DoorBlockEntity be))return Shapes.block();
  DoorDecl d=be.getDoorDecl();boolean open=be.state==1;
  VoxelShape shape=(open?d.getStructureDefinition().getOpenShapes():d.getStructureDefinition().getClosedShapes()).getOrDefault(offset,Shapes.empty());
  final VoxelShape[] result={Shapes.empty()};Direction f=facing(s);
  shape.forAllBoxes((x,y,z,X,Y,Z)->{double a=x-.5,b=z-.5,A=X-.5,B=Z-.5;double loX=a,hiX=A,loZ=b,hiZ=B;
   switch(f){case SOUTH:loX=-A;hiX=-a;loZ=-B;hiZ=-b;break;case WEST:loX=b;hiX=B;loZ=-A;hiZ=-a;break;case EAST:loX=-B;hiX=-b;loZ=a;hiZ=A;break;default:break;}
   result[0]=Shapes.or(result[0],Shapes.box(loX+.5,y,loZ+.5,hiX+.5,Y,hiZ+.5));});return result[0];
 }
 public void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){
  if(!s.is(next.getBlock())&&!l.isClientSide){
   // Only remove parts that still belong to this controller; never adjacent builds.
   for(BlockPos o:offsets())if(!o.equals(BlockPos.ZERO)){
    BlockPos q=p.offset(rotate(o,facing(s)));
    if(l.getBlockEntity(q) instanceof DoorPartEntity part&&p.equals(part.controller)){part.controller=null;l.removeBlock(q,false);}
   }
  }super.onRemove(s,l,p,next,moving);
 }
}
