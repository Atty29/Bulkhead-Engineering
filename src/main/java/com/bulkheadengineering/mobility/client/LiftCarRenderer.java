package com.bulkheadengineering.mobility.client;
import com.bulkheadengineering.mobility.LiftCarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
public final class LiftCarRenderer extends EntityRenderer<LiftCarEntity> {
 private static final ResourceLocation TEXTURE=new ResourceLocation("bulkheadengineering","textures/mtr/vehicle/lift_1.png");
 private final Map<String,ModelLift1> models=new LinkedHashMap<>();
 public LiftCarRenderer(EntityRendererProvider.Context c){super(c);}
 public ResourceLocation getTextureLocation(LiftCarEntity e){return TEXTURE;}
 public void render(LiftCarEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){String key=e.width()+"/"+e.depth()+"/"+e.height()+"/"+e.doubleSided();if(models.size()>32)models.clear();var model=models.computeIfAbsent(key,k->new ModelLift1((int)Math.round(e.height()*2),e.width(),e.depth(),e.doubleSided()));pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180-e.front().toYRot()));model.draw(new GraphicsHolder(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE))),light,e.door());pose.popPose();super.render(e,yaw,partial,pose,buffers,light);}
 public boolean shouldRender(LiftCarEntity e,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){return e.noCulling||f.isVisible(e.getBoundingBoxForCulling());}
}
