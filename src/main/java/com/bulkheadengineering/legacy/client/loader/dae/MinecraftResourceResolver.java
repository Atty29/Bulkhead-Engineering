package com.bulkheadengineering.legacy.client.loader.dae;
import java.io.*;
import net.minecraft.resources.ResourceLocation;
final class MinecraftResourceResolver {static InputStream open(ResourceLocation id)throws IOException{return net.minecraft.client.Minecraft.getInstance().getResourceManager().open(id);}}
