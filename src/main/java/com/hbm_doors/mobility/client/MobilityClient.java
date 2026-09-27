package com.hbm_doors.mobility.client;
import com.hbm_doors.mobility.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
@Mod.EventBusSubscriber(modid="hbm_doors",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class MobilityClient {
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(Mobility.LIFT_CAR.get(),LiftCarRenderer::new);e.registerEntityRenderer(Mobility.LIFT_BARRIER.get(),net.minecraft.client.renderer.entity.NoopRenderer::new);e.registerBlockEntityRenderer(Mobility.STATION_ENTITY.get(),LiftStationRenderer::new);}
 @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->{for(var item:java.util.List.of(Mobility.CONNECTOR,Mobility.REMOVER))net.minecraft.client.renderer.item.ItemProperties.register(item.get(),new net.minecraft.resources.ResourceLocation("hbm_doors","selected"),(stack,level,entity,seed)->stack.hasTag()&&stack.getTag().contains("floor")?1:0);Mobility.BLOCKS.values().forEach(b->net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(b.get(),net.minecraft.client.renderer.RenderType.cutout()));});}
}
