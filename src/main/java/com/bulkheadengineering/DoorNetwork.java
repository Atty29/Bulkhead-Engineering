package com.bulkheadengineering;

import com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.Supplier;

public final class DoorNetwork {
 private static final String PROTOCOL="1";
 public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(
  new ResourceLocation(Doors.ID,"door_config"),
  ()->PROTOCOL,PROTOCOL::equals,PROTOCOL::equals);
 private static int nextId;
 private DoorNetwork(){}

 public static void init(){
  CHANNEL.registerMessage(nextId++,UpdateDoorConfig.class,
   UpdateDoorConfig::encode,UpdateDoorConfig::decode,UpdateDoorConfig::handle);
 }

 public record UpdateDoorConfig(BlockPos pos,int variant,String mainText,String subText){
  private static void encode(UpdateDoorConfig message,FriendlyByteBuf buffer){
   buffer.writeBlockPos(message.pos);
   buffer.writeVarInt(message.variant);
   buffer.writeUtf(message.mainText,DoorLabels.MAIN_MAX);
   buffer.writeUtf(message.subText,DoorLabels.SUB_MAX);
  }
  private static UpdateDoorConfig decode(FriendlyByteBuf buffer){
   return new UpdateDoorConfig(buffer.readBlockPos(),buffer.readVarInt(),
    buffer.readUtf(DoorLabels.MAIN_MAX),buffer.readUtf(DoorLabels.SUB_MAX));
  }
  private static void handle(UpdateDoorConfig message,Supplier<NetworkEvent.Context> contextSupplier){
   NetworkEvent.Context context=contextSupplier.get();
   context.enqueueWork(()->{
    ServerPlayer player=context.getSender();
    if(player==null)return;
    DoorBlockEntity door=DoorTarget.resolve(player.level(),message.pos);
    if(door==null||player.distanceToSqr(Vec3.atCenterOf(door.getBlockPos()))>64.0)return;
    door.applyConfiguration(message.variant,
     DoorLabels.clean(message.mainText,DoorLabels.MAIN_MAX),
     DoorLabels.clean(message.subText,DoorLabels.SUB_MAX));
   });
   context.setPacketHandled(true);
  }
 }
}
