package com.bulkheadengineering;
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
  if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("bulkheadengineering.smoke"))return;
  Minecraft mc=Minecraft.getInstance();
  if(!started&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){
   started=true;int count=DoorRenderer.verifyAllModels();System.out.println("BULKHEAD_SMOKE: loaded "+count+" model/skin variants");mc.setScreen(new Gallery());
  }
 }
 static class Gallery extends Screen {
  int frames=0;
  Gallery(){super(Component.literal("Door model verification"));}
  public void render(GuiGraphics g,int mx,int my,float partial){
   if(frames>=40){renderModular(g);if(++frames==80){save("modular-blast-door-preview.png",g);System.out.println("BULKHEAD_SMOKE: rendered modular wall closed, half-open and open");minecraft.stop();}return;}
   g.fill(0,0,width,height,0xff26323c);g.drawString(font,"Bulkhead Engineering — model verification",12,10,0xffffff);
   int i=0;for(var entry:Doors.DOORS.entrySet())if(entry.getValue().get() instanceof AnimatedDoorBlock){int x=20+(i%5)*(width/5),y=40+(i/5)*(height/3-8);g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(3,3,3);g.renderItem(new ItemStack(entry.getValue().get()),0,0);g.pose().popPose();g.drawString(font,entry.getKey().replace("_door",""),x,y+50,0xffffff,false);i++;}
   super.render(g,mx,my,partial);
   if(++frames==40){save("door-gallery.png",g);System.out.println("BULKHEAD_SMOKE: rendered all 15 animated inventory entries");}
  }
  private void save(String name,GuiGraphics g){g.flush();try(NativeImage image=Screenshot.takeScreenshot(minecraft.getMainRenderTarget())){image.writeToFile(java.nio.file.Path.of(name));}catch(Exception ex){throw new RuntimeException(ex);}}
  private void renderModular(GuiGraphics g){
   g.fill(0,0,width,height,0xff26323c);g.drawString(font,"1.12 modular blast door - three linked sections",12,10,0xffffff);
   var block=(AnimatedDoorBlock)Doors.DOORS.get("modular_blast_door").get();var buffers=minecraft.renderBuffers().bufferSource();
   for(int stage=0;stage<3;stage++){
    float x=width*(stage+.5f)/3;float scale=Math.min(height/10f,width/20f);
    g.pose().pushPose();g.pose().translate(x,height*.80f,150);g.pose().scale(scale,-scale,scale);
    g.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(15));g.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(25));
    for(int column=-1;column<=1;column++){
     var be=new com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity(net.minecraft.core.BlockPos.ZERO,block.defaultBlockState());
     var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("ticks",stage*50);tag.putByte("state",(byte)(stage==0?0:stage==1?3:1));be.load(tag);
     g.pose().pushPose();g.pose().translate(column-.5,0,-.5);new DoorRenderer(null).render(be,0,g.pose(),buffers,15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);g.pose().popPose();
    }buffers.endBatch();g.pose().popPose();g.drawCenteredString(font,new String[]{"Closed","Opening (2.5 seconds)","Open (5 seconds)"}[stage],(int)x,height-24,0xffffff);
   }
  }
 }
}
