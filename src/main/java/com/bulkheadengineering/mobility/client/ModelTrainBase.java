package com.bulkheadengineering.mobility.client;
/** Lift-only subset of MTR's model helpers (MIT). */
abstract class ModelTrainBase {
 protected ModelPartExtension createModelPart(){return new ModelPartExtension();}
 protected void buildModel(){}
 protected static void setRotationAngle(ModelPartExtension part,float x,float y,float z){part.setRotation(x,y,z);}
 protected abstract void baseTransform(GraphicsHolder graphics);
 protected abstract int getDoorMax();
 protected abstract void render(GraphicsHolder g,RenderStage stage,int light,float lx,float rx,float lz,float rz,int car,int count,boolean front,boolean details);
 public void draw(GraphicsHolder g,int light,float open){g.pose.pushPose();g.pose.scale(1,-1,1);baseTransform(g);for(var stage:RenderStage.values())render(g,stage,stage==RenderStage.LIGHT?15728880:light,0,0,open*12,open*12,0,1,true,true);g.pose.popPose();}
	protected static void renderOnce(ModelPartExtension bone, GraphicsHolder graphicsHolder, int light, float position) {
		bone.render(graphicsHolder, 0, position, 0, light, OverlayTexture.getDefaultUvMapped());
	}

	protected static void renderOnce(ModelPartExtension bone, GraphicsHolder graphicsHolder, int light, float positionX, float positionZ) {
		bone.render(graphicsHolder, positionX, positionZ, 0, light, OverlayTexture.getDefaultUvMapped());
	}

	protected static void renderOnce(ModelPartExtension bone, GraphicsHolder graphicsHolder, int light, float positionX, float positionY, float positionZ) {
		bone.render(graphicsHolder, positionX, positionY, positionZ, 0, light, OverlayTexture.getDefaultUvMapped());
	}

	protected static void renderOnceFlipped(ModelPartExtension bone, GraphicsHolder graphicsHolder, int light, float position) {
		bone.render(graphicsHolder, 0, position, (float) Math.PI, light, OverlayTexture.getDefaultUvMapped());
	}

	protected static void renderOnceFlipped(ModelPartExtension bone, GraphicsHolder graphicsHolder, int light, float positionX, float positionZ) {
		bone.render(graphicsHolder, -positionX, positionZ, (float) Math.PI, light, OverlayTexture.getDefaultUvMapped());
	}

	protected static void renderOnceFlipped(ModelPartExtension bone, GraphicsHolder graphicsHolder, int light, float positionX, float positionY, float positionZ) {
		bone.render(graphicsHolder, -positionX, positionY, positionZ, (float) Math.PI, light, OverlayTexture.getDefaultUvMapped());
	}

}
