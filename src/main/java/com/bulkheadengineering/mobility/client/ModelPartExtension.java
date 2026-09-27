package com.bulkheadengineering.mobility.client;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;
import java.util.*;
/** Builds the full part tree before constructing vanilla parts, so optimized renderers see all geometry. */
public final class ModelPartExtension {
 private final List<ModelPart.Cube> cubes=new ArrayList<>();
 private final List<ModelPartExtension> children=new ArrayList<>();
 private ModelPart part;private int u,v;private float px,py,pz,rx,ry,rz;
 public void setPivot(float x,float y,float z){px=x;py=y;pz=z;if(part!=null)part.setPos(x,y,z);}
 public void setRotation(float x,float y,float z){rx=x;ry=y;rz=z;if(part!=null)part.setRotation(x,y,z);}
 public ModelPartExtension setTextureUVOffset(int u,int v){this.u=u;this.v=v;return this;}
 private void requireUnbaked(){if(part!=null)throw new IllegalStateException("Lift geometry cannot change after baking");}
 public void addCuboid(float x,float y,float z,float w,float h,float d,float grow,boolean mirror){requireUnbaked();cubes.add(new ModelPart.Cube(u,v,x,y,z,w,h,d,grow,grow,grow,mirror,128,128,EnumSet.allOf(Direction.class)));}
 public ModelPartExtension addChild(){requireUnbaked();var child=new ModelPartExtension();children.add(child);return child;}
 private ModelPart bake(){if(part==null){Map<String,ModelPart> bakedChildren=new LinkedHashMap<>();for(int i=0;i<children.size();i++)bakedChildren.put("child"+i,children.get(i).bake());part=new ModelPart(List.copyOf(cubes),bakedChildren);part.setPos(px,py,pz);part.setRotation(rx,ry,rz);}return part;}
 public void render(GraphicsHolder g,float x,float z,float angle,int light,int overlay){render(g,x,0,z,angle,light,overlay);}
 public void render(GraphicsHolder g,float x,float y,float z,float angle,int light,int overlay){g.pose.pushPose();g.pose.translate(x/16,y/16,z/16);g.pose.mulPose(Axis.YP.rotation(angle));bake().render(g.pose,g.vertices,light,overlay);g.pose.popPose();}
}
