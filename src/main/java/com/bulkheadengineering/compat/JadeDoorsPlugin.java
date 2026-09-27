package com.bulkheadengineering.compat;
import com.bulkheadengineering.*;
import snownee.jade.api.*;
@WailaPlugin
public final class JadeDoorsPlugin implements IWailaPlugin {
 @Override public void registerClient(IWailaClientRegistration registration){
  registration.usePickedResult(Doors.PART.get());
  Doors.DOORS.values().forEach(block->registration.usePickedResult(block.get()));
 }
}
