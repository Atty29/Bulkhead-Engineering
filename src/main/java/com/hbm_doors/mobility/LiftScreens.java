package com.hbm_doors.mobility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import java.util.*;

/** Lift-only Forge UI; no MTR dashboard, railway networking or web browser. */
public final class LiftScreens {
 public static void open(LiftNetwork.Open packet){Minecraft.getInstance().setScreen(new LiftScreen(packet));}
 private static class LiftScreen extends Screen {
  final LiftNetwork.Open source;final Map<String,EditBox> fields=new LinkedHashMap<>();CompoundTag data;String error="";int page;int pageSize;
  LiftScreen(LiftNetwork.Open p){super(Component.literal(p.mode().equals("floor")?"Lift floor":p.mode().equals("cabin")?"Lift setup":"Choose floor"));source=p;data=p.data().copy();}
  protected void init(){fields.clear();pageSize=Math.max(1,(height-100)/24);int x=width/2-145,y=40;
   if(source.mode().equals("select")){var names=data.getList("names",8);for(int i=page*pageSize;i<Math.min(names.size(),page*pageSize+pageSize);i++){final int floor=i;addRenderableWidget(Button.builder(Component.literal(names.getString(i)+(data.getList("descriptions",8).getString(i).isEmpty()?"":" - "+data.getList("descriptions",8).getString(i))),b->{CompoundTag n=new CompoundTag();n.putInt("floor",floor);send("select",n);onClose();}).bounds(x,y,290,20).build());y+=24;}
    if(page>0)addRenderableWidget(Button.builder(Component.literal("Previous"),b->{page--;rebuildWidgets();}).bounds(x,y,140,20).build());if((page+1)*pageSize<names.size())addRenderableWidget(Button.builder(Component.literal("Next"),b->{page++;rebuildWidgets();}).bounds(x+150,y,140,20).build());
   }else if(source.mode().equals("floor")){field("name","Floor number",data.getString("floorName"),x,y,32);y+=28;field("description","Description",data.getString("description"),x,y,64);y+=28;toggle("ding","Arrival bell",x,y);y+=30;addRenderableWidget(Button.builder(Component.literal("Save floor"),b->{CompoundTag n=new CompoundTag();n.putString("name",fields.get("name").getValue());n.putString("description",fields.get("description").getValue());n.putBoolean("ding",data.getBoolean("ding"));send("floor",n);onClose();}).bounds(x,y,290,20).build());
   }else{
    String[] keys={"width","depth","height","offsetX","offsetY","offsetZ"};String[] labels={"Width (2–16)","Depth (2–16)","Height (2–16)","X offset","Y offset","Z offset"};
    for(int i=0;i<keys.length;i++){var box=new EditBox(font,x+(i%2)*150,40+(i/2)*38,140,20,Component.literal(labels[i]));box.setMaxLength(8);box.setValue(Double.toString(data.getDouble(keys[i])));fields.put(keys[i],box);addRenderableWidget(box);}y=154;
    addRenderableWidget(Button.builder(Component.literal("Facing: "+net.minecraft.core.Direction.from2DDataValue(data.getInt("facing")).getName()),b->{data.putInt("facing",(data.getInt("facing")+1)%4);b.setMessage(Component.literal("Facing: "+net.minecraft.core.Direction.from2DDataValue(data.getInt("facing")).getName()));}).bounds(x,y,140,20).build());toggle("doubleSided","Two entrances",x+150,y);y+=26;
    addRenderableWidget(Button.builder(Component.literal("Apply setup"),b->{try{CompoundTag n=data.copy();for(var e:fields.entrySet()){double v=Double.parseDouble(e.getValue().getValue());if(!Double.isFinite(v))throw new NumberFormatException();if(e.getKey().equals("width")||e.getKey().equals("depth"))n.putInt(e.getKey(),(int)v);else n.putDouble(e.getKey(),v);}send("cabin",n);onClose();}catch(NumberFormatException ex){error="Enter numbers in every field";}}).bounds(x,y,140,20).build());
    addRenderableWidget(Button.builder(Component.literal("Choose floor"),b->open(new LiftNetwork.Open(source.pos(),source.entity(),"select",data))).bounds(x+150,y,140,20).build());
   }
   addRenderableWidget(Button.builder(Component.literal("Close"),b->onClose()).bounds(width/2-60,height-26,120,20).build());
  }
  void field(String key,String label,String value,int x,int y,int max){var box=new EditBox(font,x+145,y,145,20,Component.literal(label));box.setMaxLength(max);box.setValue(value);fields.put(key,box);addRenderableWidget(box);}
  void toggle(String key,String label,int x,int y){addRenderableWidget(Button.builder(Component.literal(label+": "+(data.getBoolean(key)?"on":"off")),b->{data.putBoolean(key,!data.getBoolean(key));b.setMessage(Component.literal(label+": "+(data.getBoolean(key)?"on":"off")));}).bounds(x,y,140,20).build());}
  void send(String action,CompoundTag n){LiftNetwork.CHANNEL.sendToServer(new LiftNetwork.Action(source.pos(),source.entity(),action,n));}
  public boolean isPauseScreen(){return false;}
  public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g);g.drawCenteredString(font,title,width/2,15,0xffffff);int y=46;for(String key:fields.keySet()){String label=switch(key){case "name"->"Floor number";case "offsetX"->"X offset";case "offsetY"->"Y offset";case "offsetZ"->"Z offset";default->key.substring(0,1).toUpperCase()+key.substring(1);};var box=fields.get(key);g.drawString(font,label,source.mode().equals("cabin")?box.getX():width/2-145,source.mode().equals("cabin")?box.getY()-10:box.getY()+6,0xffffff);}if(!error.isEmpty())g.drawCenteredString(font,error,width/2,height-42,0xff6666);super.render(g,mx,my,partial);}
 }
}
