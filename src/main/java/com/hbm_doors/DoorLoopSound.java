package com.hbm_doors;
import com.hbm_doors.legacy.block.entity.doors.DoorBlockEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
class DoorLoopSound extends AbstractTickableSoundInstance {
 private final DoorBlockEntity door;private final byte direction;
 DoorLoopSound(DoorBlockEntity door,SoundEvent event){super(event,SoundSource.BLOCKS,RandomSource.create());this.door=door;direction=door.state;looping=true;delay=0;volume=door.getDoorDecl().getSoundVolume();x=door.getBlockPos().getX()+.5;y=door.getBlockPos().getY()+.5;z=door.getBlockPos().getZ()+.5;}
 public void tick(){if(door.isRemoved()||door.state!=direction||net.minecraft.client.Minecraft.getInstance().level!=door.getLevel())stop();}
}
