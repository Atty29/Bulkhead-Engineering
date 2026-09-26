package com.hbm_doors;
import com.google.gson.*;
import com.hbm_doors.legacy.block.entity.doors.*;
import com.hbm_doors.legacy.client.loader.dae.*;
import com.hbm_doors.legacy.interfaces.IDoorAnimator;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import java.util.*;
import java.io.*;

/** Small buffered renderer: original geometry and declaration transforms, no HBM VBO/shader stack. */
public class DoorRenderer implements BlockEntityRenderer<DoorBlockEntity> {
 private static final Map<String,Model> CACHE=new HashMap<>();
 private record Vertex(float x,float y,float z,float u,float v,float nx,float ny,float nz){}
 private record Face(String material,Vertex[] vertices){}
 private static class Model {Map<String,List<Face>> parts=new LinkedHashMap<>();Map<String,String> textures=new HashMap<>();List<String> roots=new ArrayList<>();DaeModel dae;}
 public DoorRenderer(BlockEntityRendererProvider.Context c){}
 public static void clear(){CACHE.clear();DaeModel.allModels.clear();}
 public static int verifyAllModels(){
  int count=0;for(String door:com.hbm_doors.legacy.block.entity.doors.DoorDeclRegistry.getAll().keySet())for(var v:DoorVariants.forDoor(door)){
   Model m=CACHE.computeIfAbsent(v.model(),DoorRenderer::load);
   if(m.dae==null&&m.parts.isEmpty())throw new IllegalStateException("Empty OBJ "+v.model());
   if(m.dae!=null&&m.dae.meshes.isEmpty())throw new IllegalStateException("Empty DAE "+v.model());
   count++;
  }return count;
 }
 public boolean shouldRenderOffScreen(DoorBlockEntity be){return true;}
 private static ResourceLocation id(String path){return new ResourceLocation(path);}
 private static Model load(String path){
  Model m=new Model();ResourceLocation r=id(path);
  try(Reader reader=new InputStreamReader(Minecraft.getInstance().getResourceManager().open(new ResourceLocation(r.getNamespace(),"models/"+r.getPath()+".json")),java.nio.charset.StandardCharsets.UTF_8)){
   JsonObject j=JsonParser.parseReader(reader).getAsJsonObject();
   j.getAsJsonObject("textures").entrySet().forEach(e->m.textures.put(e.getKey(),e.getValue().getAsString()));
   if(j.has("parts"))j.getAsJsonArray("parts").forEach(e->m.roots.add(e.getAsString()));
   String model=j.get("model").getAsString();
   if(j.get("loader").getAsString().endsWith(":dae")){m.dae=DaeModel.load(id(model.endsWith(".dae")?model:model+".dae"));return m;}
   List<float[]> pos=new ArrayList<>(),uv=new ArrayList<>(),norm=new ArrayList<>();String group="default",material="default";boolean hasObject=false;
   try(BufferedReader br=new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().open(id(model)),java.nio.charset.StandardCharsets.UTF_8))){
    String line;while((line=br.readLine())!=null){String[] a=line.trim().split("\\s+");if(a.length<2)continue;
     switch(a[0]){
      case "v":pos.add(new float[]{Float.parseFloat(a[1]),Float.parseFloat(a[2]),Float.parseFloat(a[3])});break;
      case "vt":uv.add(new float[]{Float.parseFloat(a[1]),Float.parseFloat(a[2])});break;
      case "vn":norm.add(new float[]{Float.parseFloat(a[1]),Float.parseFloat(a[2]),Float.parseFloat(a[3])});break;
      case "o":group=a[1];hasObject=true;break;
      case "g":if(!hasObject)group=a[1];break;
      case "usemtl":material=a[1];break;
      case "f":
       Vertex[] face=new Vertex[a.length-1];for(int i=1;i<a.length;i++){String[] index=a[i].split("/",-1);float[] p=pos.get(index(index[0],pos.size()));float[] u=index.length>1&&!index[1].isEmpty()?uv.get(index(index[1],uv.size())):new float[2];float[] n=index.length>2&&!index[2].isEmpty()?norm.get(index(index[2],norm.size())):new float[]{0,1,0};face[i-1]=new Vertex(p[0],p[1],p[2],u[0],1-u[1],n[0],n[1],n[2]);}
       // RenderType consumes quads; repeat each triangle's third vertex.
       for(int i=1;i<face.length-1;i++)m.parts.computeIfAbsent(group,k->new ArrayList<>()).add(new Face(material,new Vertex[]{face[0],face[i],face[i+1],face[i+1]}));break;
     }
    }
   }
  }catch(Exception e){throw new IllegalStateException("Cannot load door model "+path,e);}
  // A few modern JSON files omit their static frame; retain the OBJ frame too.
  for(String part:m.parts.keySet())if((part.equalsIgnoreCase("frame")||part.equalsIgnoreCase("base"))&&!m.roots.contains(part))m.roots.add(part);
  return m;
 }
 private static int index(String s,int size){int i=Integer.parseInt(s);return i>0?i-1:size+i;}
 public void render(DoorBlockEntity be,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  String door=((AnimatedDoorBlock)be.getBlockState().getBlock()).id;
  List<DoorVariants.Variant> variants=DoorVariants.forDoor(door);var variant=variants.get(Math.floorMod(be.variant,variants.size()));
  Model m=CACHE.computeIfAbsent(variant.model(),DoorRenderer::load);DoorDecl d=be.getDoorDecl();
  pose.pushPose();pose.translate(.5,0,.5);Direction f=AnimatedDoorBlock.facing(be.getBlockState());float angle=switch(f){case SOUTH->180;case WEST->90;case EAST->270;default->0;};pose.mulPose(Axis.YP.rotationDegrees(90+angle));
  d.doOffsetTransform(new IDoorAnimator(){public void translate(double x,double y,double z){pose.translate(x,y,z);}public void rotate(float a,float x,float y,float z){pose.mulPose(new org.joml.Quaternionf().rotationAxis((float)Math.toRadians(a),x,y,z));}});
  if(m.dae!=null){
   int offset=d.getBakedModelRotationOffsetY();if(offset!=0){pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(offset));pose.translate(-.5,0,.5);}
   DaeAnimation clip=m.dae.animations.get("animation");if(clip==null&&!m.dae.animations.isEmpty())clip=m.dae.animations.values().iterator().next();
   for(DaeNode node:m.dae.sceneRoots)dae(node,clip,be.animation(partial)/20,pose,buffers,light,overlay,m);
  }else{
   Set<String> children=new HashSet<>();for(String name:m.roots)children.addAll(Arrays.asList(d.getChildren(name,variant.selection())));
   for(String name:m.roots)if(!children.contains(name))part(name,false,d,variant,m,be.animation(partial),pose,buffers,light,overlay,0);
  }pose.popPose();
 }
 private static void part(String name,boolean child,DoorDecl d,DoorVariants.Variant v,Model m,float time,PoseStack p,MultiBufferSource b,int light,int overlay,int depth){
  if(depth>16||!d.doesRender(name,child))return;p.pushPose();float[] o=new float[3],r=new float[3],t=new float[3];d.getOrigin(name,o,v.selection());d.getRotation(name,time,r,v.selection());d.getTranslation(name,time,child,t,v.selection());
  p.translate(o[0],o[1],o[2]);p.mulPose(Axis.XP.rotationDegrees(r[0]));p.mulPose(Axis.YP.rotationDegrees(r[1]));p.mulPose(Axis.ZP.rotationDegrees(r[2]));p.translate(t[0]-o[0],t[1]-o[1],t[2]-o[2]);
  for(Face face:m.parts.getOrDefault(name,List.of())){VertexConsumer vc=b.getBuffer(RenderType.entityCutoutNoCull(texture(m,face.material)));for(Vertex vertex:face.vertices)emit(vertex,p,vc,light,overlay);}
  for(String sub:d.getChildren(name,v.selection()))part(sub,true,d,v,m,time,p,b,light,overlay,depth+1);p.popPose();
 }
 private static ResourceLocation texture(Model m,String material){String value=m.textures.getOrDefault(material,m.textures.getOrDefault("default",m.textures.get("particle")));if(value==null)throw new IllegalStateException("Missing texture for "+material);ResourceLocation r=id(value);return new ResourceLocation(r.getNamespace(),"textures/"+r.getPath()+".png");}
 private static void emit(Vertex v,PoseStack p,VertexConsumer c,int light,int overlay){c.vertex(p.last().pose(),v.x,v.y,v.z).color(255,255,255,255).uv(v.u,v.v).overlayCoords(overlay).uv2(light).normal(p.last().normal(),v.nx,v.ny,v.nz).endVertex();}
 private static void dae(DaeNode n,DaeAnimation clip,float time,PoseStack p,MultiBufferSource b,int light,int overlay,Model m){
  p.pushPose();p.mulPoseMatrix(n.localMatrix(time,clip));if(n.mesh!=null){DaeMesh mesh=n.mesh;VertexConsumer c=b.getBuffer(RenderType.entityCutoutNoCull(texture(m,"default")));
   for(int[] tri:mesh.tris)for(int j:new int[]{0,1,2,2}){int pi=tri[j*3]*3,ni=tri[j*3+1]*3,ui=tri[j*3+2]*2;emit(new Vertex(mesh.positions[pi],mesh.positions[pi+1],mesh.positions[pi+2],ui>=0?mesh.uvs[ui]:0,ui>=0?1-mesh.uvs[ui+1]:0,ni>=0?mesh.normals[ni]:0,ni>=0?mesh.normals[ni+1]:1,ni>=0?mesh.normals[ni+2]:0),p,c,light,overlay);}
  }for(DaeNode sub:n.children)dae(sub,clip,time,p,b,light,overlay,m);p.popPose();
 }
}
