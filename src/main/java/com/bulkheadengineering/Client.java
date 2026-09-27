package com.bulkheadengineering;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.*;
@Mod.EventBusSubscriber(modid=Doors.ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class Client {
 private static final java.util.Map<com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity,java.util.List<DoorLoopSound>> LOOPS=new java.util.WeakHashMap<>();
 public static void tick(com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity be){
  if(be.state>1&&!LOOPS.containsKey(be)){
   java.util.List<DoorLoopSound> sounds=new java.util.ArrayList<>();var d=be.getDoorDecl();var event=be.state==3?d.getOpenSoundLoop():d.getCloseSoundLoop();
   if(event!=null)sounds.add(new DoorLoopSound(be,event));if(d.getSoundLoop2()!=null)sounds.add(new DoorLoopSound(be,d.getSoundLoop2()));
   sounds.forEach(s->net.minecraft.client.Minecraft.getInstance().getSoundManager().play(s));LOOPS.put(be,sounds);
  }else if(be.state<2){LOOPS.remove(be);}
 }
 // These descriptors belong to the block-entity renderer; vanilla needs an
 // empty baked model instead of attempting to interpret the custom meshes.
 @SubscribeEvent public static void geometry(ModelEvent.RegisterGeometryLoaders e){e.register("door",net.minecraftforge.client.model.EmptyModel.LOADER);e.register("dae",net.minecraftforge.client.model.EmptyModel.LOADER);}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(Doors.DOOR_ENTITY.get(),DoorRenderer::new);}
 @SubscribeEvent public static void reload(RegisterClientReloadListenersEvent e){e.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)r->DoorRenderer.clear());}
}
