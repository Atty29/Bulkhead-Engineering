package com.bulkheadengineering;
import com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;
import net.minecraftforge.common.util.FakePlayerFactory;
@GameTestHolder(Doors.ID) @PrefixGameTestTemplate(false)
public class DoorIdentityTests {
 @GameTest(template="empty",timeoutTicks=600)
 public static void everyPartEveryStyleAfterReload(GameTestHelper h){var l=h.getLevel();var p=h.absolutePos(new BlockPos(12,8,12));var player=FakePlayerFactory.getMinecraft(l);int picks=0;
  for(var entry:Doors.DOORS.entrySet())if(entry.getValue().get() instanceof AnimatedDoorBlock block)for(Direction facing:Direction.Plane.HORIZONTAL){
   var state=block.defaultBlockState().setValue(AnimatedDoorBlock.FACING,facing);l.setBlock(p,state,3);block.setPlacedBy(l,p,state,null,ItemStack.EMPTY);var master=(DoorBlockEntity)l.getBlockEntity(p);
   for(int style=0;style<DoorVariants.forDoor(block.id).size();style++){master.variant=style;master.load(master.saveWithoutMetadata());
    for(var mode:new GameType[]{GameType.CREATIVE,GameType.SURVIVAL}){player.setGameMode(mode);
     for(var offset:block.offsets()){var q=p.offset(AnimatedDoorBlock.rotate(offset,facing));if(l.getBlockEntity(q) instanceof DoorPartEntity part)part.load(part.saveWithoutMetadata());var hit=new BlockHitResult(Vec3.atCenterOf(q),Direction.NORTH,q,false);var stack=l.getBlockState(q).getCloneItemStack(hit,l,q,player);
      h.assertTrue(stack.is(block.asItem())&&DoorTarget.style(block,stack)==style,entry.getKey()+" identity/style "+facing+" "+offset+" "+mode);
      h.assertTrue(stack.getHoverName().equals(new ItemStack(block).getHoverName()),"child has controller's translated name");h.assertTrue(stack.getCount()==1,"single placement item");h.assertTrue(stack.getTag()==null||(!stack.getTag().contains("BlockEntityTag")&&!stack.getTag().contains("controller")),"no controller/motion data copied");picks++;
     }
    }
   }
   var copied=DoorTarget.pick(l,p);int style=master.variant;l.removeBlock(p,false);l.setBlock(p,state,3);block.setPlacedBy(l,p,state,null,copied);h.assertTrue(((DoorBlockEntity)l.getBlockEntity(p)).variant==style,"placing picked item restores style");l.removeBlock(p,false);
  }
  h.assertTrue(picks>1000,"covered every section and variant in both game modes");player.setGameMode(GameType.CREATIVE);System.out.println("BULKHEAD_IDENTITY: "+picks+" controller/child style picks passed");h.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void corruptOwnershipAndSingleDrop(GameTestHelper h){var l=h.getLevel();var p=h.absolutePos(new BlockPos(12,8,12));
  for(var entry:Doors.DOORS.entrySet())if(entry.getValue().get() instanceof AnimatedDoorBlock block){var state=block.defaultBlockState();l.setBlock(p,state,3);block.setPlacedBy(l,p,state,null,ItemStack.EMPTY);var off=block.offsets().stream().filter(o->!o.equals(BlockPos.ZERO)).findFirst().orElseThrow();var q=p.offset(off);var part=(DoorPartEntity)l.getBlockEntity(q);var owner=part.controller;
   part.controller=q;h.assertTrue(DoorTarget.pick(l,q).isEmpty(),"self link safe");part.controller=p.above(40);h.assertTrue(DoorTarget.pick(l,q).isEmpty(),"missing owner safe");part.controller=owner;var savedLocal=part.local;part.local=BlockPos.ZERO;h.assertTrue(DoorTarget.pick(l,q).isEmpty(),"corrupt offset safe");part.local=savedLocal;
   var box=new AABB(p).inflate(25);l.destroyBlock(q,true);var drops=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box);h.assertTrue(drops.stream().mapToInt(e->e.getItem().getCount()).sum()==1&&drops.get(0).getItem().is(block.asItem()),"breaking child drops exactly one complete door: "+entry.getKey());drops.forEach(e->e.discard());
  }h.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void standardDoorsAndLandingDoors(GameTestHelper h){var l=h.getLevel();var p=h.absolutePos(new BlockPos(12,8,12));var player=FakePlayerFactory.getMinecraft(l);
  for(var mode:new GameType[]{GameType.CREATIVE,GameType.SURVIVAL}){player.setGameMode(mode);for(var entry:Doors.DOORS.entrySet())if(!(entry.getValue().get() instanceof AnimatedDoorBlock)){var b=entry.getValue().get();l.setBlock(p.below(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);l.setBlock(p,b.defaultBlockState(),3);b.setPlacedBy(l,p,b.defaultBlockState(),null,new ItemStack(b));int height=b instanceof net.minecraft.world.level.block.DoorBlock?2:1;for(int y=0;y<height;y++){var q=p.above(y);var hit=new BlockHitResult(Vec3.atCenterOf(q),Direction.NORTH,q,false);h.assertTrue(l.getBlockState(q).getCloneItemStack(hit,l,q,player).is(b.asItem()),"standard door/hatch pick "+entry.getKey());}l.removeBlock(p,false);}
   for(var entry:com.bulkheadengineering.mobility.Mobility.BLOCKS.entrySet())if(entry.getValue().get() instanceof com.bulkheadengineering.mobility.LiftFixtureBlock b&&b.kind==com.bulkheadengineering.mobility.LiftFixtureBlock.Kind.DOOR){l.setBlock(p,b.defaultBlockState(),3);b.setPlacedBy(l,p,b.defaultBlockState(),null,new ItemStack(b));for(int x=0;x<b.width();x++)for(int y=0;y<b.height();y++){var q=p.offset(x,y,0);var hit=new BlockHitResult(Vec3.atCenterOf(q),Direction.NORTH,q,false);h.assertTrue(l.getBlockState(q).getCloneItemStack(hit,l,q,player).is(b.asItem()),"landing door section pick");}l.removeBlock(p,false);}
  }player.setGameMode(GameType.CREATIVE);
  for(var b:Doors.DOORS.values())h.assertTrue(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(b.get()).getNamespace().equals(Doors.ID),"new block namespace");
  for(var item:Doors.ITEMS.getEntries())h.assertTrue(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item.get()).getNamespace().equals(Doors.ID),"new item namespace");
  int recipes=0;for(var recipe:l.getRecipeManager().getRecipes())if(recipe.getId().getNamespace().equals(Doors.ID))recipes++;h.assertTrue(recipes==37,"all 37 renamed recipes load, got "+recipes);h.succeed();
 }
}
