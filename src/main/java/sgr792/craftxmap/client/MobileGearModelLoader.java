package sgr792.craftxmap.client;

import sgr792.craftxmap.client.util.ObjModelLoader;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;

public class MobileGearModelLoader implements ICustomModelLoader {
  public static ObjModelLoader loader;
  
  public void onResourceManagerReload(IResourceManager resourceManager) {
    OBJLoader.INSTANCE.onResourceManagerReload(resourceManager);
    loader = new ObjModelLoader(resourceManager);
  }
  
  public boolean accepts(ResourceLocation modelLocation) {
    if (modelLocation.getResourceDomain().equals("mobilegear"));
    if (modelLocation.getResourceDomain().equals("mobilegear") && modelLocation.getResourcePath().contains("models/item/")) {
      System.out.println("accepts: " + modelLocation.getResourcePath());
      return false;
    } 
    return false;
  }
  
  public IModel loadModel(ResourceLocation modelLocation) throws Exception {
    return null;
  }
}
