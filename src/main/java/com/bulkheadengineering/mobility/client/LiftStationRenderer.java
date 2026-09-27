package com.bulkheadengineering.mobility.client;
import com.bulkheadengineering.mobility.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
public final class LiftStationRenderer implements BlockEntityRenderer<LiftStationEntity> {
 // Original MTR lift leaf: 12 x 16 x 2 cuboid using a 28 x 18 texture layout.
 private static final net.minecraft.client.model.geom.ModelPart LEAF=new net.minecraft.client.model.geom.ModelPart(java.util.List.of(new net.minecraft.client.model.geom.ModelPart.Cube(0,0,0,0,0,12,16,2,0,0,0,false,28,18,java.util.EnumSet.allOf(net.minecraft.core.Direction.class))),java.util.Map.of());
 public LiftStationRenderer(BlockEntityRendererProvider.Context c){}
 public boolean shouldRenderOffScreen(LiftStationEntity be){return be.getBlockState().getBlock() instanceof LiftFixtureBlock f&&f.kind==LiftFixtureBlock.Kind.DOOR;}
 public void render(LiftStationEntity be,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  var s=be.getBlockState();if(!(s.getBlock() instanceof LiftFixtureBlock b))return;
  pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(180-s.getValue(LiftFixtureBlock.FACING).toYRot()));pose.translate(-.5,0,-.5);
  if(!s.getValue(LiftFixtureBlock.ODD)&&s.getValue(LiftFixtureBlock.SIDE)==EscalatorBlock.Side.LEFT){
   if(b.kind==LiftFixtureBlock.Kind.DOOR){
    String half=s.getValue(LiftFixtureBlock.HALF)==DoubleBlockHalf.UPPER?"top":"bottom";
    for(int side=0;side<2;side++){
     var texture=new ResourceLocation("bulkheadengineering","textures/mtr/block/lift_door_"+half+"_"+(side==0?"left":"right")+"_1.png");
     pose.pushPose();pose.translate(b.width()/2d+(side==0?-.75:0)+(side==0?-1:1)*be.doorProgress*.75,1,.125);pose.scale(1,-1,1);
     LEAF.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,overlay);pose.popPose();
    }
   }else{
    var car=be.car();String value=car==null?"--":car.floorName(car.floor());if(be.locked)value="LOCKED";
    double z=b.kind==LiftFixtureBlock.Kind.BUTTON||b.style==2?.068:.256;
    if(b.kind==LiftFixtureBlock.Kind.PANEL){String label="--";if(be.getLevel()!=null&&!be.links.isEmpty()&&be.getLevel().getBlockEntity(be.links.get(0)) instanceof LiftStationEntity floor)label=floor.floorName;
     text(pose,buffers,label,b.width()/2d,.555,z,.22,0xff111111);
     text(pose,buffers,value+(car!=null&&car.moving()?" *":""),b.width()/2d,.30,z,1.05,0xffffbb33);
    }else{text(pose,buffers,value,.5,.78,z,.45,0xffff3333);text(pose,buffers,"\u25b2",.5,.39,z,.2,be.locked?0xff555555:0xff111111);text(pose,buffers,"\u25bc",.5,.19,z,.2,be.locked?0xff555555:0xff111111);}
   }
  }
  pose.popPose();
 }
 private static void text(PoseStack pose,MultiBufferSource buffers,String value,double x,double y,double z,double maxWidth,int color){var font=Minecraft.getInstance().font;float scale=(float)Math.min(.015,maxWidth/Math.max(1,font.width(value)));pose.pushPose();pose.translate(x,y,z);pose.scale(scale,-scale,scale);font.drawInBatch(value,-font.width(value)/2f,0,color,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,15728880);pose.popPose();}
}
