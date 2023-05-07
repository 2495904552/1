package sgr792.craftxmap.client;

import sgr792.craftxmap.CraftxMapMod;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import sgr792.craftxmap.common.items.ItemRadio;

@EventBusSubscriber(value = {Side.CLIENT}, modid = "craftxmap")
public class ClientHandler {
  @SubscribeEvent
  public static void onModelBakeEvent(ModelBakeEvent event) {
    Iterator<ModelResourceLocation> iter = event.getModelRegistry().getKeys().iterator();
    while (iter.hasNext()) {
      ModelResourceLocation key = iter.next();
      if (key.getResourceDomain().equals("craftxmap"))
        if (key.getResourcePath().contains("radio") || key.getResourcePath().contains("map3d"))
          event.getModelRegistry().putObject(key, new RadioBakedModel((IBakedModel)event.getModelRegistry().getObject(key)));  
    } 
  }
  
  @SubscribeEvent
  public static void onChatReceivedEvent(ClientChatReceivedEvent event) {
    if (event.getType() == ChatType.CHAT) {
      EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).player;
      if (entityPlayerSP != null && ((EntityPlayer)entityPlayerSP).world != null) {
        ItemStack stack = null;
        for (ItemStack is : ((EntityPlayer)entityPlayerSP).inventory.mainInventory) {
          if (is.getItem() instanceof ItemRadio) {
            stack = is;
            break;
          } 
        } 
        if (stack != null && stack.hasTagCompound()) {
          boolean switchOn = false;
          int channel = 1;
          NBTTagCompound nbt = stack.getTagCompound();
          if (nbt.hasKey("Channel"))
            channel = nbt.getInteger("Channel"); 
          if (nbt.hasKey("Switch"))
            switchOn = nbt.getBoolean("Switch"); 
          if (switchOn)
            try {
              Pattern p = Pattern.compile(chanRegx);
              String strrr = event.getMessage().getUnformattedText();
              Matcher m = p.matcher(event.getMessage().getUnformattedText());
              if (m.find()) {
                String chanStr = m.group();
                if (!chanStr.isEmpty()) {
                  p = Pattern.compile("\\d+");
                  m = p.matcher(chanStr);
                  if (m.find()) {
                    chanStr = m.group();
                    if (!chanStr.isEmpty())
                      if (channel == Integer.parseInt(chanStr)) {
                        event.setMessage((ITextComponent)new TextComponentString(event.getMessage().getFormattedText()));
                      } else {
                        event.setCanceled(true);
                      }
                  }
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
        }
      }
    }
  }

  @SubscribeEvent
  public static void onChatEvent(ClientChatEvent event) {
    EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).player;
    if (entityPlayerSP != null && ((EntityPlayer)entityPlayerSP).world != null) {
      ItemStack stack = null;
      for (ItemStack is : ((EntityPlayer)entityPlayerSP).inventory.mainInventory) {
        if (is.getItem() instanceof ItemRadio) {
          stack = is;
          break;
        }
      }
      if (stack != null && stack.hasTagCompound()) {
        boolean switchOn = false;
        int channel = 1;
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt.hasKey("Channel"))
          channel = nbt.getInteger("Channel");
        if (nbt.hasKey("Switch"))
          switchOn = nbt.getBoolean("Switch");
        if (switchOn && event.getMessage().indexOf("/") == -1) {
          event.setCanceled(false);
          return;
        }
      }
    }
  }
  
  public static String chanRegx = "\\[ch:\\d+\\]";
  
  public static String channelString(int chan) {
    return "[ch:" + chan + "]";
  }
}
