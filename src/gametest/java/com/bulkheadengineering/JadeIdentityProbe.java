package com.bulkheadengineering;
import snownee.jade.api.*;
@WailaPlugin
public class JadeIdentityProbe implements IWailaPlugin {
 public void registerClient(IWailaClientRegistration r){r.addTooltipCollectedCallback((tooltip,accessor)->{if(Boolean.getBoolean("bulkheadengineering.identitySmoke")&&accessor instanceof BlockAccessor a&&DoorTarget.resolve(a.getLevel(),a.getPosition())!=null){String text=tooltip.getMessage();if(!text.contains("Secure Access Door"))throw new IllegalStateException("Incorrect Jade identity: "+text);IdentitySmoke.overlaySeen=true;}});}
}
