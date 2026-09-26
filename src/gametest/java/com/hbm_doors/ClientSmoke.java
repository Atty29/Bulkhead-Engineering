package com.hbm_doors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Screenshot;

/** Optional client-only smoke run. Test code is excluded from the shipped JAR. */
@Mod.EventBusSubscriber(modid=Doors.ID,value=Dist.CLIENT)
public class ClientSmoke {
 private static boolean started;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("hbmdoors.smoke"))return;
  Minecraft mc=Minecraft.getInstance();
  if(!started&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){
   started=true;int count=DoorRenderer.verifyAllModels();System.out.println("HBM_DOORS_SMOKE: loaded "+count+" model/skin variants");mc.setScreen(new Gallery());
  }
 }
 static class Gallery extends Screen {
  int frames=0;
  Gallery(){super(Component.literal("Door model verification"));}
  public void render(GuiGraphics g,int mx,int my,float partial){
   g.fill(0,0,width,height,0xff26323c);g.drawString(font,"HBM Doors — model verification",12,10,0xffffff);
   int i=0;for(var entry:Doors.DOORS.entrySet())if(entry.getValue().get() instanceof AnimatedDoorBlock){int x=20+(i%5)*(width/5),y=40+(i/5)*(height/3-8);g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(3,3,3);g.renderItem(new ItemStack(entry.getValue().get()),0,0);g.pose().popPose();g.drawString(font,entry.getKey().replace("_door",""),x,y+50,0xffffff,false);i++;}
   super.render(g,mx,my,partial);
   if(++frames==40){g.flush();try(NativeImage image=Screenshot.takeScreenshot(minecraft.getMainRenderTarget())){image.writeToFile(java.nio.file.Path.of("door-gallery.png"));}catch(Exception ex){throw new RuntimeException(ex);}System.out.println("HBM_DOORS_SMOKE: rendered all 13 inventory models");minecraft.stop();}
  }
 }
}
