package com.bulkheadengineering;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Doors.ID,value=Dist.CLIENT)
public final class DoorClientEvents {
 private DoorClientEvents(){}

 @SubscribeEvent
 public static void configure(PlayerInteractEvent.RightClickBlock event){
  if(!event.getLevel().isClientSide||event.getHand()!=InteractionHand.MAIN_HAND||!event.getEntity().isShiftKeyDown())return;
  var door=DoorTarget.resolve(event.getLevel(),event.getPos());
  if(door==null)return;
  event.setCanceled(true);
  event.setCancellationResult(InteractionResult.SUCCESS);
  Minecraft.getInstance().setScreen(new DoorConfigScreen(door));
 }
}
