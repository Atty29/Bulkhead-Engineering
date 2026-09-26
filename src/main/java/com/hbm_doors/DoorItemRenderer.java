package com.hbm_doors;
import com.hbm_doors.legacy.block.entity.doors.DoorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.item.*;
import net.minecraft.core.BlockPos;
import com.mojang.blaze3d.vertex.PoseStack;
public class DoorItemRenderer extends BlockEntityWithoutLevelRenderer {
 public DoorItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 @Override public void renderByItem(ItemStack stack,ItemDisplayContext ctx,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  if(stack.getItem() instanceof BlockItem item&&item.getBlock() instanceof AnimatedDoorBlock block){DoorBlockEntity be=new DoorBlockEntity(BlockPos.ZERO,block.defaultBlockState());new DoorRenderer(null).render(be,0,pose,buffers,light,overlay);}
 }
 public static class Extension implements net.minecraftforge.client.extensions.common.IClientItemExtensions {
  private DoorItemRenderer renderer;
  public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new DoorItemRenderer();return renderer;}
 }
}
