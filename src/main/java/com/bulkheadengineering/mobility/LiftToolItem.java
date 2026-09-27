package com.bulkheadengineering.mobility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.chat.Component;
import java.util.*;
public final class LiftToolItem extends Item {
 public enum Kind {REFRESH,LINK,UNLINK}private final Kind kind;
 public LiftToolItem(Kind kind){super(new Properties().stacksTo(1));this.kind=kind;}
 public InteractionResult useOn(UseOnContext c){if(c.getLevel().isClientSide)return InteractionResult.SUCCESS;var player=c.getPlayer();if(player==null||!player.mayBuild())return InteractionResult.FAIL;var level=(ServerLevel)c.getLevel();var p=c.getClickedPos();
  try {
   if(kind==Kind.REFRESH){var route=LiftRoute.discover(level,p);Set<UUID> ids=new HashSet<>();for(int stop:route.stops())if(level.getBlockEntity(route.blocks().get(stop)) instanceof LiftStationEntity station&&station.cabin!=null)ids.add(station.cabin);if(ids.size()>1)throw new IllegalArgumentException("Remove existing lifts before merging their tracks");
    LiftCarEntity car=null;if(!ids.isEmpty()){var entity=level.getEntity(ids.iterator().next());if(entity instanceof LiftCarEntity found)car=found;else throw new IllegalArgumentException("Load the existing cabin's chunk before refreshing");}
    if(car!=null)car.pauseForEditing();
    if(player.isShiftKeyDown()){if(car!=null){car.discard();for(var floor:route.blocks())if(level.getBlockEntity(floor) instanceof LiftStationEntity s){s.cabin=null;s.cabinId=-1;s.sync();}}player.displayClientMessage(Component.literal("Lift removed; tracks kept"),true);return InteractionResult.SUCCESS;}
    if(car!=null){LiftNetwork.open(player,p,car,"cabin");return InteractionResult.SUCCESS;}
    boolean fresh=car==null;if(fresh)car=Mobility.LIFT_CAR.get().create(level);car.initialize(route,level.getBlockState(p).getValue(LiftTrackBlock.FACING).getOpposite());if(fresh)level.addFreshEntity(car);
    for(int stop:route.stops())if(level.getBlockEntity(route.blocks().get(stop)) instanceof LiftStationEntity station){station.cabin=car.getUUID();station.cabinId=car.getId();station.sync();}LiftNetwork.open(player,p,car,"cabin");
   }else{
    var tag=c.getItemInHand().getOrCreateTag();var state=level.getBlockState(p);
    if(state.getBlock() instanceof LiftTrackBlock track&&track.kind==LiftTrackBlock.Kind.FLOOR){tag.putLong("floor",p.asLong());tag.putString("dimension",level.dimension().location().toString());player.displayClientMessage(Component.literal("Floor selected: "+p.toShortString()),true);}
    else if(state.getBlock() instanceof LiftFixtureBlock fixture){if(!tag.contains("floor")||!level.dimension().location().toString().equals(tag.getString("dimension")))throw new IllegalArgumentException("Select a Floor Track in this dimension first");var floor=BlockPos.of(tag.getLong("floor"));if(!level.hasChunkAt(floor)||!(level.getBlockEntity(floor) instanceof LiftStationEntity))throw new IllegalArgumentException("Selected floor is missing or unloaded");var root=fixture.root(p,state);
     for(int x=0;x<fixture.width();x++)for(int y=0;y<fixture.height();y++)if(level.getBlockEntity(root.relative(state.getValue(LiftFixtureBlock.FACING).getClockWise(),x).above(y)) instanceof LiftStationEntity be){if(kind==Kind.LINK){if(!be.links.contains(floor)&&be.links.size()<16)be.links.add(floor);}else if(player.isShiftKeyDown())be.links.clear();else be.links.remove(floor);be.sync();}player.displayClientMessage(Component.literal(kind==Kind.LINK?"Lift floor linked":"Lift floor unlinked"),true);
    }else throw new IllegalArgumentException("Use on Floor Track, then a lift button, panel or door");
   }return InteractionResult.SUCCESS;
  }catch(IllegalArgumentException ex){player.displayClientMessage(Component.literal(ex.getMessage()),true);return InteractionResult.FAIL;}
 }
}
