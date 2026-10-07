package com.bulkheadengineering;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds the SGC Blue skin from each door's own source texture at runtime.
 * This keeps every original panel/rivet/UV layout while remapping the metal
 * into the blue-grey SGC palette.
 */
public final class SgcSkinTextures {
 private static final Map<ResourceLocation,ResourceLocation> CACHE=new HashMap<>();

 private SgcSkinTextures(){}

 public static ResourceLocation get(ResourceLocation source){
  return CACHE.computeIfAbsent(source,SgcSkinTextures::create);
 }

 public static void clear(){CACHE.clear();}

 private static ResourceLocation create(ResourceLocation source){
  try(InputStream stream=Minecraft.getInstance().getResourceManager().open(source)){
   NativeImage image=NativeImage.read(stream);
   for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){
    int pixel=image.getPixelRGBA(x,y);
    int r=pixel&255;
    int g=(pixel>>>8)&255;
    int b=(pixel>>>16)&255;
    int a=(pixel>>>24)&255;
    if(a==0)continue;

    // Keep the yellow/black hazard markings that already exist on several doors.
    boolean hazardYellow=r>115&&g>80&&b<85&&r>b*1.45f&&g>b*1.25f;
    if(!hazardYellow){
     float lum=.2126f*r+.7152f*g+.0722f*b;
     float shade=(lum-128f)*1.06f+128f;
     r=clamp(Math.round(shade*.48f+8));
     g=clamp(Math.round(shade*.68f+14));
     b=clamp(Math.round(shade*.90f+24));
    }

    image.setPixelRGBA(x,y,(a<<24)|(b<<16)|(g<<8)|r);
   }

   String safe=source.getNamespace()+"_"+source.getPath().replace('/','_').replace('.','_');
   ResourceLocation generated=new ResourceLocation(Doors.ID,"dynamic/sgc/"+safe);
   Minecraft.getInstance().getTextureManager().register(generated,new DynamicTexture(image));
   return generated;
  }catch(Exception exception){
   return source;
  }
 }

 private static int clamp(int value){return Math.max(0,Math.min(255,value));}
}
