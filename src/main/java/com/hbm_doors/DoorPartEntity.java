package com.hbm_doors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
public class DoorPartEntity extends BlockEntity {
 public BlockPos controller;public BlockPos local=BlockPos.ZERO;
 public DoorPartEntity(BlockPos p,BlockState s){super(Doors.PART_ENTITY.get(),p,s);}
 protected void saveAdditional(CompoundTag t){super.saveAdditional(t);if(controller!=null)t.putLong("controller",controller.asLong());t.putLong("local",local.asLong());}
 public void load(CompoundTag t){super.load(t);controller=t.contains("controller")?BlockPos.of(t.getLong("controller")):null;local=BlockPos.of(t.getLong("local"));}
 public CompoundTag getUpdateTag(){return saveWithoutMetadata();}
 public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
