package com.bulkheadengineering;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
public class DoorItem extends BlockItem {
 public DoorItem(Block b){super(b,new Item.Properties());}
 @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){consumer.accept(new DoorItemRenderer.Extension());}
}
