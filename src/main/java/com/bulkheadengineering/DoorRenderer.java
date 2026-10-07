package com.bulkheadengineering;
import com.google.gson.*;
import com.bulkheadengineering.legacy.block.entity.doors.*;
import com.bulkheadengineering.legacy.client.loader.dae.*;
import com.bulkheadengineering.legacy.interfaces.IDoorAnimator;
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
 private static class Bounds {
  float minX=Float.POSITIVE_INFINITY,minY=Float.POSITIVE_INFINITY,minZ=Float.POSITIVE_INFINITY;
  float maxX=Float.NEGATIVE_INFINITY,maxY=Float.NEGATIVE_INFINITY,maxZ=Float.NEGATIVE_INFINITY;
  void include(Vector3f p){minX=Math.min(minX,p.x);minY=Math.min(minY,p.y);minZ=Math.min(minZ,p.z);maxX=Math.max(maxX,p.x);maxY=Math.max(maxY,p.y);maxZ=Math.max(maxZ,p.z);}
  void include(Bounds b){if(b==null||!Float.isFinite(b.minX))return;minX=Math.min(minX,b.minX);minY=Math.min(minY,b.minY);minZ=Math.min(minZ,b.minZ);maxX=Math.max(maxX,b.maxX);maxY=Math.max(maxY,b.maxY);maxZ=Math.max(maxZ,b.maxZ);}
 }
 private static class Model {
  Map<String,List<Face>> parts=new LinkedHashMap<>();
  Map<String,String> textures=new HashMap<>();
  Map<String,Bounds> bounds=new HashMap<>();
  List<String> roots=new ArrayList<>();
  DaeModel dae;
  void include(String part,Vector3f p){bounds.computeIfAbsent(part,k->new Bounds()).include(p);}
  Bounds doorBounds(){
   Bounds result=new Bounds();
   for(var e:bounds.entrySet()){
    String n=e.getKey().toLowerCase(java.util.Locale.ROOT);
    if(n.contains("door")&&!n.contains("frame"))result.include(e.getValue());
   }
   if(!Float.isFinite(result.minX))for(var e:bounds.entrySet()){
    String n=e.getKey().toLowerCase(java.util.Locale.ROOT);
    if(!n.contains("frame")&&!n.equals("base")&&!n.contains("lock")&&!n.equals("label"))result.include(e.getValue());
   }
   return result;
  }
 }
 public DoorRenderer(BlockEntityRendererProvider.Context c){}
 public static void clear(){CACHE.clear();DaeModel.allModels.clear();SgcSkinTextures.clear();}
 public static int verifyAllModels(){
  int count=0;for(String door:com.bulkheadengineering.legacy.block.entity.doors.DoorDeclRegistry.getAll().keySet())for(var v:DoorVariants.forDoor(door)){
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
   // Mesh coordinates are baked before hinge/slide transforms. Normalize the
   // obsolete upstream root offsets to our controller-centered coordinate system.
   org.joml.Matrix4f meshTransform=new org.joml.Matrix4f();
   if(j.has("transform")){
    JsonObject tr=j.getAsJsonObject("transform");float[] t={0,0,0},rot={0,0,0};
    if(tr.has("translation"))for(int i=0;i<3;i++)t[i]=tr.getAsJsonArray("translation").get(i).getAsFloat();
    if(tr.has("rotation"))for(int i=0;i<3;i++)rot[i]=tr.getAsJsonArray("rotation").get(i).getAsFloat();
    if(path.contains("fire_door"))t[2]=.5f;
    if(path.contains("large_vehicle_door")||path.contains("water_door")||path.contains("secure_access_door"))t[2]=0;
    // Secure-access sill is sunk below the placement surface, as in the mesh.
    if(path.contains("secure_access_door"))t[1]=path.contains("legacy")?-1:0;
    meshTransform.translate(t[0],t[1],t[2]).rotateXYZ((float)Math.toRadians(rot[0]),(float)Math.toRadians(rot[1]),(float)Math.toRadians(rot[2]));
    if(tr.has("scale"))meshTransform.scale(tr.get("scale").getAsFloat());
   }
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
       Vertex[] face=new Vertex[a.length-1];for(int i=1;i<a.length;i++){String[] index=a[i].split("/",-1);float[] p=pos.get(index(index[0],pos.size()));float[] u=index.length>1&&!index[1].isEmpty()?uv.get(index(index[1],uv.size())):new float[2];float[] n=index.length>2&&!index[2].isEmpty()?norm.get(index(index[2],norm.size())):new float[]{0,1,0};Vector3f point=meshTransform.transformPosition(new Vector3f(p[0],p[1],p[2]));m.include(group,point);Vector3f normal=meshTransform.transformDirection(new Vector3f(n[0],n[1],n[2])).normalize();face[i-1]=new Vertex(point.x,point.y,point.z,u[0],1-u[1],normal.x,normal.y,normal.z);}
       // RenderType consumes quads; repeat each triangle's third vertex.
       for(int i=1;i<face.length-1;i++)m.parts.computeIfAbsent(group,k->new ArrayList<>()).add(new Face(group.equalsIgnoreCase("Label")&&m.textures.containsKey("label")?"label":material,new Vertex[]{face[0],face[i],face[i+1],face[i+1]}));break;
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
   for(String name:m.roots)if(!children.contains(name))part(name,false,d,variant,m,be.animation(partial),pose,new PoseStack(),buffers,light,overlay,0);
  }
  if(variant.configurableText()&&be.getOpenTicks()==0&&m.dae==null)renderSgcFacade(be,m,pose,buffers,light);
  pose.popPose();
 }
 private static void part(String name,boolean child,DoorDecl d,DoorVariants.Variant v,Model m,float time,PoseStack world,PoseStack p,MultiBufferSource b,int light,int overlay,int depth){
  if(d instanceof ModularBlastDoorDecl&&!ModularBlastDoorDecl.visible(name,time))return;
  if(depth>16||!d.doesRender(name,child))return;p.pushPose();float[] o=new float[3],r=new float[3],t=new float[3];d.getOrigin(name,o,v.selection());d.getRotation(name,time,r,v.selection());d.getTranslation(name,time,child,t,v.selection());
  p.translate(o[0],o[1],o[2]);p.mulPose(Axis.XP.rotationDegrees(r[0]));p.mulPose(Axis.YP.rotationDegrees(r[1]));p.mulPose(Axis.ZP.rotationDegrees(r[2]));p.translate(t[0]-o[0],t[1]-o[1],t[2]-o[2]);
  if(!(v.configurableText()&&name.equalsIgnoreCase("Label")))for(Face face:m.parts.getOrDefault(name,List.of())){
   List<Vertex> polygon=new ArrayList<>();for(int i=0;i<3;i++)polygon.add(transformed(face.vertices[i],p));
   String id=d.getBlockId().getPath();
   if(id.equals("large_vehicle_door")){polygon=clip(polygon,2,1,3.5f);polygon=clip(polygon,2,-1,3.5f);}
   if(id.equals("fire_door"))polygon=clip(polygon,1,-1,3.0001f);
   if(id.equals("sliding_seal_door"))polygon=clip(polygon,2,-1,.5001f);
   if(id.equals("secure_access_door"))polygon=clip(polygon,1,-1,4.0001f);
   VertexConsumer vc=b.getBuffer(RenderType.entityCutoutNoCull(texture(m,face.material,v)));
   int[] tint=v.configurableText()?sgcTint(name):new int[]{255,255,255};
   for(int i=1;i<polygon.size()-1;i++)for(Vertex vertex:new Vertex[]{polygon.get(0),polygon.get(i),polygon.get(i+1),polygon.get(i+1)})emit(vertex,world,vc,light,overlay,tint[0],tint[1],tint[2]);
  }
  for(String sub:d.getChildren(name,v.selection()))part(sub,true,d,v,m,time,world,p,b,light,overlay,depth+1);p.popPose();
 }
 private static Vertex transformed(Vertex v,PoseStack p){
  Vector3f a=p.last().pose().transformPosition(new Vector3f(v.x,v.y,v.z));Vector3f n=p.last().normal().transform(new Vector3f(v.nx,v.ny,v.nz));
  return new Vertex(a.x,a.y,a.z,v.u,v.v,n.x,n.y,n.z);
 }
 private static float distance(Vertex v,int axis,float sign,float limit){return sign*(axis==0?v.x:axis==1?v.y:v.z)+limit;}
 private static Vertex mix(Vertex a,Vertex b,float t){float u=1-t;return new Vertex(a.x*u+b.x*t,a.y*u+b.y*t,a.z*u+b.z*t,a.u*u+b.u*t,a.v*u+b.v*t,a.nx*u+b.nx*t,a.ny*u+b.ny*t,a.nz*u+b.nz*t);}
 /** Sutherland-Hodgman clipping in animated model space, before facing rotation. */
 private static List<Vertex> clip(List<Vertex> polygon,int axis,float sign,float limit){
  List<Vertex> out=new ArrayList<>();if(polygon.isEmpty())return out;
  Vertex prev=polygon.get(polygon.size()-1);float pd=distance(prev,axis,sign,limit);
  for(Vertex next:polygon){float nd=distance(next,axis,sign,limit);if((pd>=0)!=(nd>=0))out.add(mix(prev,next,pd/(pd-nd)));if(nd>=0)out.add(next);prev=next;pd=nd;}return out;
 }
 private static ResourceLocation texture(Model m,String material){
  String value=m.textures.getOrDefault(material,m.textures.getOrDefault("default",m.textures.get("particle")));
  if(value==null)throw new IllegalStateException("Missing texture for "+material);
  ResourceLocation r=id(value);
  return new ResourceLocation(r.getNamespace(),"textures/"+r.getPath()+".png");
 }
 private static ResourceLocation texture(Model m,String material,DoorVariants.Variant variant){
  if(variant.configurableText())return new ResourceLocation("minecraft","textures/block/white_concrete.png");
  return texture(m,material);
 }
 private static int[] sgcTint(String part){
  String n=part.toLowerCase(java.util.Locale.ROOT);
  if(n.contains("frame")||n.equals("base"))return new int[]{210,210,192};
  if(n.contains("lock")||n.contains("bolt")||n.contains("spinny"))return new int[]{42,48,54};
  return new int[]{91,132,168};
 }
 private static void emit(Vertex v,PoseStack p,VertexConsumer c,int light,int overlay){emit(v,p,c,light,overlay,255,255,255);}
 private static void emit(Vertex v,PoseStack p,VertexConsumer c,int light,int overlay,int red,int green,int blue){
  c.vertex(p.last().pose(),v.x,v.y,v.z).color(red,green,blue,255).uv(v.u,v.v).overlayCoords(overlay).uv2(light).normal(p.last().normal(),v.nx,v.ny,v.nz).endVertex();
 }

 /**
  * Draws the SGC face as a flush skin on the actual moving door surface.
  * The old source texture is completely hidden: cream structural frame,
  * blue-grey panel face, recessed panel lines, hazard threshold and the
  * two editable text fields. Nothing is positioned from the multiblock
  * footprint, so the sign no longer floats in front of thick frames.
  */
 private static void renderSgcFacade(DoorBlockEntity be,Model m,PoseStack pose,MultiBufferSource buffers,int light){
  Bounds b=m.doorBounds();
  if(!Float.isFinite(b.minX)||!Float.isFinite(b.minY)||!Float.isFinite(b.minZ))return;
  float minY=Math.max(0,b.minY),maxY=b.maxY;
  float spanX=b.maxX-b.minX,spanY=maxY-minY,spanZ=b.maxZ-b.minZ;
  if(spanY<.75f)return;
  boolean normalX=spanX<spanZ;
  float width=normalX?spanZ:spanX;
  float centerH=normalX?(b.minZ+b.maxZ)*.5f:(b.minX+b.maxX)*.5f;
  float centerY=(minY+maxY)*.5f;
  float face=normalX?b.maxX:b.maxZ;
  float back=normalX?b.minX:b.minZ;
  float gap=.0015f;

  if(normalX){
   renderFacadeFace(pose,buffers,be.labelMain,be.labelSub,face+gap,centerY,centerH,90,width,spanY,light);
   renderFacadeFace(pose,buffers,be.labelMain,be.labelSub,back-gap,centerY,centerH,-90,width,spanY,light);
  }else{
   renderFacadeFace(pose,buffers,be.labelMain,be.labelSub,centerH,centerY,face+gap,0,width,spanY,light);
   renderFacadeFace(pose,buffers,be.labelMain,be.labelSub,centerH,centerY,back-gap,180,width,spanY,light);
  }
 }

 private static void renderFacadeFace(PoseStack pose,MultiBufferSource buffers,String main,String sub,float x,float y,float z,float yaw,float width,float height,int light){
  pose.pushPose();
  pose.translate(x,y,z);
  pose.mulPose(Axis.YP.rotationDegrees(yaw));
  ResourceLocation white=new ResourceLocation("minecraft","textures/block/white_concrete.png");
  VertexConsumer panel=buffers.getBuffer(RenderType.entityCutoutNoCull(white));

  float hw=width*.5f,hh=height*.5f;
  float edge=Math.max(.018f,Math.min(width,height)*.018f);
  float line=Math.max(.012f,Math.min(width,height)*.012f);

  // SGC blue panel with a dark recessed perimeter.
  panelQuad(pose,panel,-hw,-hh,hw,hh,0,28,35,42,light);
  panelQuad(pose,panel,-hw+edge,-hh+edge,hw-edge,hh-edge,.0002f,99,143,180,light);

  // Reference-image panel grid: narrow side columns and broad centre panels.
  float left=-hw+width*.185f,right=hw-width*.185f;
  panelQuad(pose,panel,left-line*.5f,-hh+edge,left+line*.5f,hh-edge,.0004f,38,55,70,light);
  panelQuad(pose,panel,right-line*.5f,-hh+edge,right+line*.5f,hh-edge,.0004f,38,55,70,light);
  for(float fy:new float[]{-.30f,.02f,.33f}){
   float yy=fy*height;
   panelQuad(pose,panel,-hw+edge,yy-line*.5f,hw-edge,yy+line*.5f,.0004f,38,55,70,light);
  }

  // Bottom black/yellow threshold stripe.
  float stripeY=-hh+edge;
  float stripeH=Math.max(.055f,height*.055f);
  panelQuad(pose,panel,-hw+edge,stripeY,hw-edge,stripeY+stripeH,.0007f,24,24,22,light);
  int stripes=14;
  float sw=(width-2*edge)/stripes;
  for(int i=0;i<stripes;i+=2){
   float x0=-hw+edge+i*sw;
   slantedQuad(pose,panel,x0,stripeY,x0+sw,stripeY+stripeH,.0009f,220,171,18,light,sw*.32f);
  }

  // Flush label areas inspired by the supplied SGC reference.
  float signW=Math.min(width*.50f,3.7f);
  float mainH=Math.min(height*.245f,1.15f);
  float subH=Math.min(height*.095f,.42f);
  float signY=height*.035f;
  panelQuad(pose,panel,-signW*.5f,signY-mainH*.10f,signW*.5f,signY+mainH*.90f,.0010f,116,157,191,light);
  panelQuad(pose,panel,-signW*.43f,signY-mainH*.34f-subH,signW*.43f,signY-mainH*.34f,.0011f,101,143,177,light);

  drawLabelLine(pose,buffers,main,signY+mainH*.39f,signW*.90f,mainH*.68f,true,light,.0020f);
  drawLabelLine(pose,buffers,sub,signY-mainH*.34f-subH*.50f,signW*.72f,subH*.76f,false,light,.0021f);
  pose.popPose();
 }

 private static void panelQuad(PoseStack pose,VertexConsumer c,float x0,float y0,float x1,float y1,float z,int r,int g,int b,int light){
  var last=pose.last();
  for(float[] v:new float[][]{{x0,y0,0,1},{x1,y0,1,1},{x1,y1,1,0},{x0,y1,0,0}})
   c.vertex(last.pose(),v[0],v[1],z).color(r,g,b,255).uv(v[2],v[3]).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(last.normal(),0,0,1).endVertex();
 }
 private static void slantedQuad(PoseStack pose,VertexConsumer c,float x0,float y0,float x1,float y1,float z,int r,int g,int b,int light,float slant){
  var last=pose.last();
  for(float[] v:new float[][]{{x0,y0,0,1},{x1,y0,1,1},{x1+slant,y1,1,0},{x0+slant,y1,0,0}})
   c.vertex(last.pose(),v[0],v[1],z).color(r,g,b,255).uv(v[2],v[3]).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light).normal(last.normal(),0,0,1).endVertex();
 }

 private static void drawLabelLine(PoseStack pose,MultiBufferSource buffers,String text,float y,float maxWidth,float maxHeight,boolean main,int light,float z){
  if(text==null||text.isEmpty())return;
  var font=Minecraft.getInstance().font;
  int pixels=Math.max(1,font.width(text));
  float scale=Math.min(maxWidth/pixels,maxHeight/font.lineHeight);
  scale=Math.min(scale,main?.115f:.066f);
  pose.pushPose();
  pose.translate(0,y,z);
  pose.scale(scale,-scale,scale);
  font.drawInBatch(text,-pixels/2f,-font.lineHeight/2f,0xFFF6F7F3,false,pose.last().pose(),buffers,net.minecraft.client.gui.Font.DisplayMode.POLYGON_OFFSET,0,light);
  pose.popPose();
 }
 private static void dae(DaeNode n,DaeAnimation clip,float time,PoseStack p,MultiBufferSource b,int light,int overlay,Model m){
  p.pushPose();p.mulPoseMatrix(n.localMatrix(time,clip));if(n.mesh!=null){DaeMesh mesh=n.mesh;VertexConsumer c=b.getBuffer(RenderType.entityCutoutNoCull(texture(m,"default")));
   for(int[] tri:mesh.tris)for(int j:new int[]{0,1,2,2}){int pi=tri[j*3]*3,ni=tri[j*3+1]*3,ui=tri[j*3+2]*2;emit(new Vertex(mesh.positions[pi],mesh.positions[pi+1],mesh.positions[pi+2],ui>=0?mesh.uvs[ui]:0,ui>=0?1-mesh.uvs[ui+1]:0,ni>=0?mesh.normals[ni]:0,ni>=0?mesh.normals[ni+1]:1,ni>=0?mesh.normals[ni+2]:0),p,c,light,overlay);}
  }for(DaeNode sub:n.children)dae(sub,clip,time,p,b,light,overlay,m);p.popPose();
 }
}
