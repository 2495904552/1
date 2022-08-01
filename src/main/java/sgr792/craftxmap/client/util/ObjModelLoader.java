package sgr792.craftxmap.client.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

public class ObjModelLoader {
  private Map<String, abstractObjModel> models = new HashMap();
  private IResourceManager resourceManager;

  public ObjModelLoader(IResourceManager resourceManagerIn) {
    this.resourceManager = resourceManagerIn;
    this.loadModels();
  }

  public void loadModels() {
    this.readItemModel("item", "radio");
    this.readItemModel("item", "map3d");
    this.readItemModel("item", "echo");
  }

  public void readItemModel(String path, String fileName) {
    Reader reader = null;
    IResource iresource = null;

    try {
      ResourceLocation rl = new ResourceLocation("mobilegear", "models/" + path + "/" + fileName + ".obj");
      if (rl.getResourcePath().endsWith(".obj")) {
        iresource = this.resourceManager.getResource(new ResourceLocation(rl.getResourceDomain(), rl.getResourcePath()));
        reader = new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8);
        this.models.put(fileName, new ObjModel(new BufferedReader(reader)));
      }
    } catch (Exception var9) {
      var9.printStackTrace();
    } finally {
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(iresource);
    }

  }

  public int getSize() {
    return this.models.size();
  }

  public abstractObjModel getModel(String name) {
    return (abstractObjModel)this.models.get(name);
  }
}
