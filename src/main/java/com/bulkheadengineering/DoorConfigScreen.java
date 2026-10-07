package com.bulkheadengineering;

import com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public final class DoorConfigScreen extends Screen {
 private final BlockPos pos;
 private final String doorId;
 private int variant;
 private final String initialMain;
 private final String initialSub;
 private EditBox mainField;
 private EditBox subField;

 public DoorConfigScreen(DoorBlockEntity door){
  super(Component.literal("Door Configuration"));
  this.pos=door.getBlockPos();
  AnimatedDoorBlock block=(AnimatedDoorBlock)door.getBlockState().getBlock();
  this.doorId=block.id;
  this.variant=DoorTarget.style(block,door.variant);
  this.initialMain=door.labelMain;
  this.initialSub=door.labelSub;
 }

 @Override protected void init(){
  int cx=width/2;
  addRenderableWidget(Button.builder(Component.literal("<"),b->changeVariant(-1)).bounds(cx-120,50,28,20).build());
  addRenderableWidget(Button.builder(Component.literal(">"),b->changeVariant(1)).bounds(cx+92,50,28,20).build());

  mainField=new EditBox(font,cx-100,100,200,20,Component.literal("Main text"));
  mainField.setMaxLength(DoorLabels.MAIN_MAX);
  mainField.setValue(initialMain);
  addRenderableWidget(mainField);

  subField=new EditBox(font,cx-100,140,200,20,Component.literal("Secondary text"));
  subField.setMaxLength(DoorLabels.SUB_MAX);
  subField.setValue(initialSub);
  addRenderableWidget(subField);

  addRenderableWidget(Button.builder(Component.literal("Done"),b->saveAndClose()).bounds(cx-100,height-40,95,20).build());
  addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).bounds(cx+5,height-40,95,20).build());
  refreshFields();
 }

 private DoorVariants.Variant selected(){
  return DoorVariants.forDoor(doorId).get(Math.floorMod(variant,DoorVariants.forDoor(doorId).size()));
 }

 private void changeVariant(int delta){
  variant=Math.floorMod(variant+delta,DoorVariants.forDoor(doorId).size());
  refreshFields();
 }

 private void refreshFields(){
  boolean enabled=selected().configurableText();
  if(mainField!=null){mainField.visible=enabled;mainField.active=enabled;}
  if(subField!=null){subField.visible=enabled;subField.active=enabled;}
 }

 private void saveAndClose(){
  DoorNetwork.CHANNEL.sendToServer(new DoorNetwork.UpdateDoorConfig(
   pos,variant,mainField.getValue(),subField.getValue()));
  if(minecraft!=null)minecraft.setScreen(null);
 }

 @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick){
  renderBackground(graphics);
  super.render(graphics,mouseX,mouseY,partialTick);
  graphics.drawCenteredString(font,title,width/2,20,0xFFFFFF);
  graphics.drawCenteredString(font,Component.literal("Style: "+selected().name()),width/2,56,0xFFFFFF);
  if(selected().configurableText()){
   graphics.drawString(font,Component.literal("Main sign text"),width/2-100,88,0xAFC9DE,false);
   graphics.drawString(font,Component.literal("Secondary sign text"),width/2-100,128,0xAFC9DE,false);
   graphics.drawCenteredString(font,Component.literal("Shown identically on both sides"),width/2,174,0x909090);
  }else{
   graphics.drawCenteredString(font,Component.literal("This door style has no configurable signage."),width/2,112,0x909090);
  }
 }

 @Override public boolean isPauseScreen(){return false;}
}
