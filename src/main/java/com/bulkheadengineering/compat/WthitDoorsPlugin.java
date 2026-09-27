package com.bulkheadengineering.compat;
import com.bulkheadengineering.*;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
public final class WthitDoorsPlugin implements IWailaPlugin,IBlockComponentProvider {
 @Override public void register(IRegistrar registrar){registrar.head(this,DoorPartBlock.class,2000);registrar.icon(this,DoorPartBlock.class);}
 @Override public void appendHead(ITooltip tooltip,IBlockAccessor accessor,IPluginConfig config){var item=DoorTarget.pick(accessor.getLevel(),accessor.getPosition());if(!item.isEmpty())tooltip.setLine(WailaConstants.OBJECT_NAME_TAG,item.getHoverName());}
 @Override public ITooltipComponent getIcon(IBlockAccessor accessor,IPluginConfig config){var item=DoorTarget.pick(accessor.getLevel(),accessor.getPosition());return item.isEmpty()?null:new ItemComponent(item);}
}
