package com.hbm_doors.mobility;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.*;
public final class MobilityShapes {
 public static VoxelShape rotate(VoxelShape shape,Direction f){VoxelShape[] r={Shapes.empty()};shape.forAllBoxes((x,y,z,X,Y,Z)->{switch(f){case EAST:r[0]=Shapes.or(r[0],Shapes.box(1-Z,y,x,1-z,Y,X));break;case SOUTH:r[0]=Shapes.or(r[0],Shapes.box(1-X,y,1-Z,1-x,Y,1-z));break;case WEST:r[0]=Shapes.or(r[0],Shapes.box(z,y,1-X,Z,Y,1-x));break;default:r[0]=Shapes.or(r[0],Shapes.box(x,y,z,X,Y,Z));}});return r[0];}
}
