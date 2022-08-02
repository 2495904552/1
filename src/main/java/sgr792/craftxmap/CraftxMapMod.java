package sgr792.craftxmap;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sgr792.craftxmap.client.ClientHandler;
import sgr792.craftxmap.client.craftxmapModelLoader;
import sgr792.craftxmap.client.mobileTEISR;
import sgr792.craftxmap.common.RegistryHandler;
import sgr792.craftxmap.common.items.ItemMap3D;
import sgr792.craftxmap.common.items.ItemRadio;
import java.io.File;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "craftxmap", name = "Craftx Map Mod", version = "1.0", acceptedMinecraftVersions = "[1.12.2]")
public class CraftxMapMod {
  public static final File MinecraftDirectory = getMinecraftDirectory();
  
  public static final String DEV_MINECRAFT_DIR = "run/";
  
  public static final String MODID = "craftxmap";
  
  public static final String NAME = "Craftx Map Mod";
  
  public static final String VERSION = "1.0";
  
  public static ItemRadio ITEM_RADIO;
  
  public static ItemMap3D ITEM_MAP;
  
  public static Config config;
  
  public static SoundEvents soundEvents;
  
  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    config = new Config();
    readConfig();
    soundEvents = new SoundEvents();
    ITEM_RADIO = (ItemRadio)(new ItemRadio()).setCreativeTab(CreativeTabs.TOOLS);
    ITEM_MAP = (ItemMap3D)(new ItemMap3D()).setCreativeTab(CreativeTabs.TOOLS);
    MinecraftForge.EVENT_BUS.register(new RegistryHandler());
    initClient();
    RegistryHandler.ITEMS.add(((Item)ITEM_RADIO.setRegistryName("radio")).setUnlocalizedName("radio"));
    RegistryHandler.ITEMS.add(((Item)ITEM_MAP.setRegistryName("map3d")).setUnlocalizedName("map3d"));
  }
  
  public class SoundEvents {
    SoundEvent lowPower = new SoundEvent(new ResourceLocation("craftxmap", "lowPower"));
    
    SoundEvent turn_off = new SoundEvent(new ResourceLocation("craftxmap", "turn_off"));
    
    SoundEvent turn_on = new SoundEvent(new ResourceLocation("craftxmap", "turn_on"));
    
    SoundEvent battery = new SoundEvent(new ResourceLocation("craftxmap", "battery"));
    
    SoundEvent send = new SoundEvent(new ResourceLocation("craftxmap", "send"));
    
    SoundEvent open = new SoundEvent(new ResourceLocation("craftxmap", "open"));
    
    SoundEvent beep = new SoundEvent(new ResourceLocation("craftxmap", "beep"));
    
    SoundEvent tuning = new SoundEvent(new ResourceLocation("craftxmap", "tuning"));
    
    public SoundEvents() {
      this.lowPower.setRegistryName(this.lowPower.getSoundName());
      this.turn_off.setRegistryName(this.turn_off.getSoundName());
      this.turn_on.setRegistryName(this.turn_on.getSoundName());
      this.battery.setRegistryName(this.battery.getSoundName());
      this.send.setRegistryName(this.send.getSoundName());
      this.open.setRegistryName(this.open.getSoundName());
      this.beep.setRegistryName(this.beep.getSoundName());
      this.tuning.setRegistryName(this.tuning.getSoundName());
    }
    
    public SoundEvent getLowPower() {
      return this.lowPower;
    }
    
    public SoundEvent getTurn_off() {
      return this.turn_off;
    }
    
    public SoundEvent getTurn_on() {
      return this.turn_on;
    }
    
    public SoundEvent getBattery() {
      return this.battery;
    }
    
    public SoundEvent getSend() {
      return this.send;
    }
    
    public SoundEvent getOpen() {
      return this.open;
    }
    
    public SoundEvent getBeep() {
      return this.beep;
    }
    
    public SoundEvent getTuning() {
      return this.tuning;
    }
  }
  
  public void initClient() {
    OBJLoader.INSTANCE.addDomain("craftxmap");
    ModelLoaderRegistry.registerLoader((ICustomModelLoader)new craftxmapModelLoader());
    ITEM_RADIO.setTileEntityItemStackRenderer((TileEntityItemStackRenderer)new mobileTEISR());
    ITEM_MAP.setTileEntityItemStackRenderer((TileEntityItemStackRenderer)new mobileTEISR());
    MinecraftForge.EVENT_BUS.register(new ClientHandler());
  }
  
  @EventHandler
  public void init(FMLServerStartingEvent event) {}
  
  @EventHandler
  public void init(FMLInitializationEvent event) {}
  
  public static File getMinecraftDirectory() {
    Minecraft minecraft = FMLClientHandler.instance().getClient();
    if (minecraft != null)
      return minecraft.mcDataDir; 
    return new File("run/");
  }
  
  public void readConfig() {
    File craftxmap = new File(new File(MinecraftDirectory, "config"), "craftxmap.json");
    try {
      if (!craftxmap.exists()) {
        craftxmap.createNewFile();
        Files.write((new Config()).toString(), craftxmap, StandardCharsets.UTF_8);
        config = new Config();
      } else {
        config = config.fromString(Files.toString(craftxmap, StandardCharsets.UTF_8));
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public class Config {
    int battery_time = 12000;
    
    int battery_warning = 2000;
    
    public int getBattery_time() {
      return this.battery_time;
    }
    
    public int getBattery_warning() {
      return this.battery_warning;
    }
    
    public String toString() {
      Gson GSON = (new GsonBuilder()).setVersion(3.0D).create();
      return GSON.toJson(this);
    }
    
    public Config fromString(String json) {
      Gson GSON = (new GsonBuilder()).setVersion(3.0D).create();
      return (Config)GSON.fromJson(json, Config.class);
    }
  }
}
