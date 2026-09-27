package com.bulkheadengineering.mobility.client;
import com.mojang.blaze3d.vertex.*;
public final class GraphicsHolder {
 public final PoseStack pose;public final VertexConsumer vertices;
 public GraphicsHolder(PoseStack pose,VertexConsumer vertices){this.pose=pose;this.vertices=vertices;}
 public void translate(double x,double y,double z){pose.translate(x,y,z);}
}
