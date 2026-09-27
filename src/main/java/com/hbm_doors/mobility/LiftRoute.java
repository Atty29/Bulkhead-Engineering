package com.hbm_doors.mobility;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.util.*;
/** Bounded path discovery. Never generates chunks or accepts ambiguous branches. */
public record LiftRoute(List<BlockPos> blocks,List<Vec3> points,List<Integer> stops) {
 public static LiftRoute discover(ServerLevel level,BlockPos start){
  Map<BlockPos,List<BlockPos>> graph=new LinkedHashMap<>();ArrayDeque<BlockPos> todo=new ArrayDeque<>();todo.add(start);
  while(!todo.isEmpty()){
   var p=todo.removeFirst();if(graph.containsKey(p))continue;if(graph.size()>=512)throw new IllegalArgumentException("Lift route exceeds 512 track blocks");
   if(!level.hasChunkAt(p))throw new IllegalArgumentException("Load every chunk along the lift route first");
   BlockState s=level.getBlockState(p);if(!(s.getBlock() instanceof LiftTrackBlock track))throw new IllegalArgumentException("Use the refresher on lift track");
   List<BlockPos> adjacent=new ArrayList<>();
   for(var d:track.connections(s)){var q=p.relative(d);if(!level.hasChunkAt(q))continue;var n=level.getBlockState(q);if(n.getBlock() instanceof LiftTrackBlock other&&other.connections(n).contains(d.getOpposite())){adjacent.add(q);todo.add(q);}}
   if(adjacent.size()>2)throw new IllegalArgumentException("Lift tracks must form one path, without branches");graph.put(p,adjacent);
  }
  var end=graph.entrySet().stream().filter(e->e.getValue().size()==1).map(Map.Entry::getKey).min(Comparator.<BlockPos>comparingInt(p -> p.getY()).thenComparingLong(p -> p.asLong())).orElseThrow(()->new IllegalArgumentException("Connect at least two floors with a non-looping track"));
  List<BlockPos> ordered=new ArrayList<>();BlockPos prev=null,next=end;
  while(next!=null){ordered.add(next);BlockPos current=next;next=null;for(var q:graph.get(current))if(!q.equals(prev)){next=q;break;}prev=current;}
  List<Integer> stops=new ArrayList<>();List<Vec3> points=new ArrayList<>();
  for(int i=0;i<ordered.size();i++){var p=ordered.get(i);var s=level.getBlockState(p);var track=(LiftTrackBlock)s.getBlock();points.add(track.point(p,s));if(track.kind==LiftTrackBlock.Kind.FLOOR)stops.add(i);}
  if(stops.size()<2)throw new IllegalArgumentException("A lift needs at least two Floor Track blocks");return new LiftRoute(List.copyOf(ordered),List.copyOf(points),List.copyOf(stops));
 }
}
