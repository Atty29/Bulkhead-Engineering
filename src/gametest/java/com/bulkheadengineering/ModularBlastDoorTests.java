package com.bulkheadengineering;

import com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;

@GameTestHolder("bulkheadengineering")
@PrefixGameTestTemplate(false)
public class ModularBlastDoorTests {
    @GameTest(template="empty", timeoutTicks=400)
    public static void linkedWallAndStagedCollision(GameTestHelper h) {
        var level=h.getLevel();
        var block=(AnimatedDoorBlock)Doors.DOORS.get("modular_blast_door").get();
        for(Direction facing:Direction.Plane.HORIZONTAL) {
            BlockPos start=h.absolutePos(new BlockPos(10,5,10));
            DoorBlockEntity[] wall=new DoorBlockEntity[3];
            for(int i=0;i<3;i++) {
                BlockPos p=start.offset(AnimatedDoorBlock.rotate(new BlockPos(i,0,0),facing));
                var state=block.defaultBlockState().setValue(AnimatedDoorBlock.FACING,facing);
                level.setBlock(p,state,3);block.setPlacedBy(level,p,state,null,ItemStack.EMPTY);
                wall[i]=(DoorBlockEntity)level.getBlockEntity(p);
                h.assertTrue(block.offsets().size()==7,"seven-block footprint");
            }
            wall[0].toggle();
            for(var door:wall)h.assertTrue(door.state==3,"adjacent sections open together");
            for(int tick=1;tick<=100;tick++) {
                for(var door:wall)DoorBlockEntity.tick(level,door.getBlockPos(),door.getBlockState(),door);
                for(int y=1;y<=5;y++)h.assertTrue(ModularBlastDoorDecl.shape(wall[0],new BlockPos(0,y,0)).isEmpty()==(tick>=Math.max(1,(y-1)*20)),"opening collision at tick "+tick+", y="+y);
                if(tick==37) {
                    var restored=new DoorBlockEntity(wall[0].getBlockPos(),wall[0].getBlockState());
                    restored.load(wall[0].saveWithoutMetadata());
                    h.assertTrue(restored.state==3&&restored.getOpenTicks()==37,"mid-motion save round trip");
                }
            }
            for(var door:wall)h.assertTrue(door.state==1,"wall open after five seconds");
            wall[2].toggle();
            for(int tick=1;tick<=100;tick++) {
                for(var door:wall)DoorBlockEntity.tick(level,door.getBlockPos(),door.getBlockState(),door);
                for(int y=1;y<=5;y++)h.assertTrue(ModularBlastDoorDecl.shape(wall[0],new BlockPos(0,y,0)).isEmpty()==(tick<(6-y)*20),"closing collision at tick "+tick+", y="+y);
            }
            for(var door:wall)h.assertTrue(door.state==0,"wall closed");
            // Removing one column must not destroy its neighboring columns.
            level.destroyBlock(wall[1].getBlockPos().above(4),false);
            h.assertTrue(level.getBlockState(wall[1].getBlockPos()).isAir(),"part removes its own column");
            h.assertTrue(level.getBlockEntity(wall[0].getBlockPos())==wall[0]&&level.getBlockEntity(wall[2].getBlockPos())==wall[2],"neighbor columns survive");
            for(var door:wall)level.removeBlock(door.getBlockPos(),false);
        }
        h.succeed();
    }

    @GameTest(template="empty", timeoutTicks=300)
    public static void risingEdgeToggleAtTopAndBase(GameTestHelper h) {
        var level=h.getLevel();BlockPos p=h.absolutePos(new BlockPos(10,5,10));
        var block=(AnimatedDoorBlock)Doors.DOORS.get("modular_blast_door").get();
        level.setBlock(p,block.defaultBlockState(),3);block.setPlacedBy(level,p,block.defaultBlockState(),null,ItemStack.EMPTY);
        var door=(DoorBlockEntity)level.getBlockEntity(p);
        level.setBlock(p.above(7),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(110,()->{h.assertTrue(door.state==1,"top input opens");level.removeBlock(p.above(7),false);});
        h.runAfterDelay(120,()->{h.assertTrue(door.state==1,"falling edge leaves door open");level.setBlock(p.below(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);});
        h.runAfterDelay(230,()->{h.assertTrue(door.state==0,"second pulse at base closes");h.succeed();});
    }
}
