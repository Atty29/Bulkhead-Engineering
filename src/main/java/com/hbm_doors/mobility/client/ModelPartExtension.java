package com.hbm_doors.mobility.client;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;
import java.util.*;
/** Minimal Forge model-part adapter; retains MTR's original cuboids and UVs. */
public final class ModelPartExtension {
 private final List<ModelPart.Cube> cubes=new ArrayList<>();private final Map<String,ModelPart> children=new LinkedHashMap<>();private final ModelPart part=new ModelPart(cubes,children);private int u,v;
 public void setPivot(float x,float y,float z){part.setPos(x,y,z);}public void setRotation(float x,float y,float z){part.setRotation(x,y,z);}
 public ModelPartExtension setTextureUVOffset(int u,int v){this.u=u;this.v=v;return this;}
 public void addCuboid(float x,float y,float z,float w,float h,float d,float grow,boolean mirror){cubes.add(new ModelPart.Cube(u,v,x,y,z,w,h,d,grow,grow,grow,mirror,128,128,EnumSet.allOf(Direction.class)));}
 public ModelPartExtension addChild(){var child=new ModelPartExtension();children.put("child"+children.size(),child.part);return child;}
 public void render(GraphicsHolder g,float x,float z,float angle,int light,int overlay){render(g,x,0,z,angle,light,overlay);}
 public void render(GraphicsHolder g,float x,float y,float z,float angle,int light,int overlay){g.pose.pushPose();g.pose.translate(x/16,y/16,z/16);g.pose.mulPose(Axis.YP.rotation(angle));part.render(g.pose,g.vertices,light,overlay);g.pose.popPose();}
}
