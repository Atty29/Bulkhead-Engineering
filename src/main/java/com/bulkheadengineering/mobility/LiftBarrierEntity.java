package com.bulkheadengineering.mobility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;
/** Ephemeral cabin wall collision, recreated by its cabin after loading. */
public final class LiftBarrierEntity extends Entity {
 private static final EntityDataAccessor<CompoundTag> BOX=SynchedEntityData.defineId(LiftBarrierEntity.class,EntityDataSerializers.COMPOUND_TAG);
 public LiftBarrierEntity(EntityType<? extends LiftBarrierEntity> t,Level l){super(t,l);noPhysics=true;}
 protected void defineSynchedData(){entityData.define(BOX,new CompoundTag());}
 public void bounds(LiftCarEntity owner,AABB box){var n=new CompoundTag();n.putInt("owner",owner.getId());n.putDouble("w",box.getXsize());n.putDouble("h",box.getYsize());n.putDouble("d",box.getZsize());entityData.set(BOX,n);setPos((box.minX+box.maxX)/2,box.minY,(box.minZ+box.maxZ)/2);setBoundingBox(box);}
 public void tick(){super.tick();var n=entityData.get(BOX);double w=n.getDouble("w")/2,d=n.getDouble("d")/2;setBoundingBox(new AABB(getX()-w,getY(),getZ()-d,getX()+w,getY()+n.getDouble("h"),getZ()+d));if(!level().isClientSide&&(!(level().getEntity(n.getInt("owner")) instanceof LiftCarEntity car)||car.isRemoved()))discard();}
 public boolean canBeCollidedWith(){return true;}public boolean isPickable(){return true;}public boolean isPushable(){return false;}
 public InteractionResult interact(Player p,InteractionHand hand){return level().getEntity(entityData.get(BOX).getInt("owner")) instanceof LiftCarEntity car?car.interact(p,hand):InteractionResult.PASS;}
 protected void readAdditionalSaveData(CompoundTag n){}protected void addAdditionalSaveData(CompoundTag n){}
 public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
