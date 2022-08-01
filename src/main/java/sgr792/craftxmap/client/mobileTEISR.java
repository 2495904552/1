package sgr792.craftxmap.client;

import sgr792.craftxmap.CraftxMapMod;
import sgr792.craftxmap.client.util.abstractObjModel;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import journeymap.client.api.display.Context;
import journeymap.client.cartography.color.RGB;
import journeymap.client.io.FileHandler;
import journeymap.client.log.JMLogger;
import journeymap.client.model.ImageHolder;
import journeymap.client.model.MapState;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.properties.InGameMapProperties;
import journeymap.client.properties.MapProperties;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.map.TileDrawStep;
import journeymap.client.render.map.TileDrawStepCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.lang3.time.DateUtils;
import org.lwjgl.opengl.GL11;

public class mobileTEISR extends TileEntityItemStackRenderer {
  public static ItemCameraTransforms.TransformType transformType = ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
  
  private static abstractObjModel map3d = null;
  
  private static abstractObjModel radio = null;

  private static final GridRenderer gridRenderer = new GridRenderer(Context.UI.Minimap, 3);
  
  private static final MapState state = new MapState();
  
  private static int zoomValue = 5;
  
  private static final int height = 3;
  
  private static final int width = 3;
  
  private MiniMapProperties miniMapProperties;
  
  private static boolean regionUpdate = false;
  
  private static Method getRegionTextureHolder;
  
  private static Method getImage;
  
  private static final TimerTask tt = new TimerTask() {
      public void run() {
        mobileTEISR.regionUpdate = true;
      }
    };
  
  static {
    (new Timer()).schedule(tt, 0L, 1000L);
    try {
      getRegionTextureHolder = TileDrawStep.class.getDeclaredMethod("getRegionTextureHolder", new Class[0]);
      if (getRegionTextureHolder.isAccessible())
        getRegionTextureHolder.setAccessible(true); 
      getImage = ImageHolder.class.getDeclaredMethod("getImage", new Class[0]);
      if (getImage.isAccessible())
        getImage.setAccessible(true); 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public void checkPermission() {}
  
  public void renderByItem(ItemStack itemStackIn) {
    Minecraft mc = Minecraft.getMinecraft();
    double fontScale = 0.0015D;
    boolean isMap = true;
    if (itemStackIn.getItem().getRegistryName().toString().equals("mobilegear:radio"))
      isMap = false; 
    EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).player;
    checkModelFile();
    GL11.glPushMatrix();
    if (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
      if (isMap) {
        if (this.miniMapProperties == null)
          initGridRenderer(); 
        gridRenderer.clearGlErrors(false);
        zoomValue = 2;
        boolean moved = gridRenderer.center(state.getWorldDir(), state.getMapType(), mc.player.posX, mc.player.posZ, zoomValue);
        boolean doStateRefresh = state.shouldRefresh(mc, (MapProperties)this.miniMapProperties);
        if (doStateRefresh)
          gridRenderer.setContext(state.getWorldDir(), state.getMapType()); 
        if (moved || !doStateRefresh);
        gridRenderer.updateTiles(state.getMapType(), state.getZoom(), state.isHighQuality(), 100, 10, doStateRefresh, 0.0D, 0.0D);
        if (doStateRefresh);
        double scaleMap = 0.002D;
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.9D, 0.9D, 0.9D);
        GlStateManager.translate(0.15F, 0.45F, 0.2F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        if (map3d == null)
          return; 
        (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/map.png"));
        map3d.renderNormal(Tessellator.getInstance().getBuffer());
        double width = 0.24D, height = 0.24D;
        GlStateManager.translate(0.69D, 0.8D, -0.58D);
        (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/compass_base.png"));
        drawQuad((ResourceLocation)null, 16777215, 1.0F, 0.0D - width / 2.0D, 0.0D - height / 2.0D, width, height, 0.0D, 0.0D, 1.0D, 1.0D, 180.0D, false, true, 770, 771, true);
        GlStateManager.translate(0.0D, 0.0D, -1.0E-5D);
        GlStateManager.disableLighting();
        (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/compass_ns.png"));
        drawQuad((ResourceLocation)null, 16777215, 1.0F, 0.0D - width / 2.0D, 0.0D - height / 2.0D, width, height, 0.0D, 0.0D, 1.0D, 1.0D, (360.0F + ((EntityPlayer)entityPlayerSP).rotationYaw), false, true, 770, 771, true);
        GlStateManager.enableLighting();
        GlStateManager.translate(0.0D, 0.0D, -1.0E-5D);
        (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/compass_center.png"));
        drawQuad((ResourceLocation)null, 16777215, 1.0F, 0.0D - width / 2.0D, 0.0D - height / 2.0D, width, height, 0.0D, 0.0D, 1.0D, 1.0D, 180.0D, false, true, 770, 771, true);
        GlStateManager.translate(0.0D, 0.0D, -1.0E-5D);
        GlStateManager.popMatrix();
        GlStateManager.disableCull();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.3F, 1.2F, 0.6F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(scaleMap, scaleMap, scaleMap);
        double fixScale = 1.0D;
        drawAddtionLayers(0.0D, 0.0D);
        GlStateManager.translate(180.5D, 131.0D, -0.03999999910593033D);
        width = 6.0D;
        height = 6.0D;
        GlStateManager.disableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/tri2.png"));
        drawQuad((ResourceLocation)null, 16777215, 1.0F, 0.0D - width * fixScale / 2.0D, 0.0D - height * fixScale / 2.0D, width * fixScale, height * fixScale, 0.0D, 0.0D, 1.0D, 1.0D, (((EntityPlayer)entityPlayerSP).rotationYaw + 180.0F - 45.0F), false, true, 770, 771, true);
        GlStateManager.translate(0.0F, 0.0F, -0.01F);
        width = 36.0D;
        height = 36.0D;
        GlStateManager.enableLighting();
        setLightmap();
        GlStateManager.popMatrix();
      } else {
        int[] jumpList = { 61, 65, 63, 37 };
        GlStateManager.translate(0.1F, 0.4F, 0.4F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-20.0F, 0.0F, 1.0F, 0.0F);
        if (radio == null)
          return; 
        boolean switchOn = false;
        int charge = 0;
        int channel = 1;
        if (itemStackIn.hasTagCompound()) {
          NBTTagCompound nbt = itemStackIn.getTagCompound();
          switchOn = nbt.getBoolean("Switch");
          float f1 = nbt.getInteger("Battery");
          float f2 = CraftxMapMod.config.getBattery_time();
          if (nbt.hasKey("Battery"))
            charge = (int)(f1 / f2 * 100.0F); 
          if (nbt.hasKey("Channel"))
            channel = nbt.getInteger("Channel"); 
        } 
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/radio.png"));
        radio.renderNormal(Tessellator.getInstance().getBuffer(), jumpList, -1);
        if (switchOn) {
          if (charge >= 70) {
            radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 61);
            radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 65);
          } else if (charge >= 30 && charge <= 70) {
            radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 61);
            radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 63);
          } else {
            radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 65);
            radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 63);
          } 
          OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
          GlStateManager.disableLighting();
          if (charge >= 70) {
            radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 63);
          } else if (charge >= 30 && charge <= 70) {
            radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 65);
          } else {
            radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 61);
          } 
          GL11.glPushMatrix();
          GlStateManager.translate(-0.25F, 0.55F, -0.255F);
          GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
          fontScale = 0.0015D;
          GlStateManager.scale(fontScale, fontScale, fontScale);
          int fontColor = 924941;
          double gameTime = (mc.player.world.getWorldTime() % 24000L);
          int realTime = (int)(gameTime / 24000.0D * 1440.0D);
          realTime += 360;
          if (realTime > 1440)
            realTime -= 1440; 
          SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
          Calendar calendar1 = Calendar.getInstance();
          calendar1.setTime(new Date());
          calendar1.set(11, 0);
          calendar1.set(12, 0);
          calendar1.set(13, 0);
          Date zero = calendar1.getTime();
          Minecraft.getMinecraft().getRenderManager().getFontRenderer().drawString(sdf.format(DateUtils.addMinutes(zero, realTime)) + ((realTime > 719) ? "PM" : "AM"), 60, 5, fontColor);
          Minecraft.getMinecraft().getRenderManager().getFontRenderer().drawString((int)((EntityPlayer)entityPlayerSP).posX + "", 21, 45, fontColor);
          Minecraft.getMinecraft().getRenderManager().getFontRenderer().drawString((int)((EntityPlayer)entityPlayerSP).posY + "", 21, 60, fontColor);
          Minecraft.getMinecraft().getRenderManager().getFontRenderer().drawString((int)((EntityPlayer)entityPlayerSP).posZ + "", 21, 75, fontColor);
          Minecraft.getMinecraft().getRenderManager().getFontRenderer().drawString(channel + "", 26, 100, fontColor);
          GL11.glPopMatrix();
          (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/radio.png"));
          OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 200.0F + charge / 100.0F * 40.0F, 200.0F + charge / 100.0F * 40.0F);
          GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
          (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/bg_on.png"));
          radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 37);
          GlStateManager.scale(0.1D, 0.1D, 0.1D);
          GlStateManager.translate(-4.01D, 4.47D, -2.58D);
          double width = 0.3D, height = 0.3D;
          (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/tri4.png"));
          drawQuad((ResourceLocation)null, 16777215, 1.0F, 0.0D - width / 2.0D, 0.0D - height / 2.0D, width, height, 0.0D, 0.0D, 1.0D, 1.0D, (((EntityPlayer)entityPlayerSP).rotationYaw - 45.0F), false, true, 770, 771, true);
        } else {
          radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 61);
          radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 65);
          radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 63);
          GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
          (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/bg_off.png"));
          radio.renderNormal(Tessellator.getInstance().getBuffer(), null, 37);
        } 
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableLighting();
      } 
    } else if (isMap) {
      GlStateManager.scale(0.7D, 0.7D, 0.7D);
      GlStateManager.translate(0.4F, 0.45F, 0.3F);
      GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
      if (map3d == null)
        return; 
      (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/map.png"));
      map3d.renderNormal(Tessellator.getInstance().getBuffer());
    } else {
      double scale = 0.5D;
      GlStateManager.translate(0.35F, 0.35F, 0.45F);
      GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(-20.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.scale(scale, scale, scale);
      (Minecraft.getMinecraft()).renderEngine.bindTexture(new ResourceLocation("mobilegear:textures/items/radio.png"));
      radio.renderNormal(Tessellator.getInstance().getBuffer(), null, -1);
    } 
    GL11.glPopMatrix();
  }
  
  private void initGridRenderer() {
    if (this.miniMapProperties == null)
      this.miniMapProperties = new MiniMapProperties(0); 
    Minecraft mc = Minecraft.getMinecraft();
    gridRenderer.clear();
    state.requireRefresh();
    if (mc.player != null && !mc.player.isDead) {
      state.refresh(mc, (EntityPlayer)mc.player, (InGameMapProperties)this.miniMapProperties);
      MapType mapType = state.getMapType();
      gridRenderer.setGridSize((this.miniMapProperties.getSize() <= 768) ? 3 : 5);
      gridRenderer.setContext(state.getWorldDir(), mapType);
      gridRenderer.center(state.getWorldDir(), mapType, mc.player.posX, mc.player.posZ, zoomValue);
      gridRenderer.updateTiles(state.getMapType(), zoomValue, 
          (Journeymap.getClient().getCoreProperties()).tileHighDisplayQuality.get().booleanValue(), 100, 10, true, 0.0D, 0.0D);
    } 
  }
  
  public static HashMap<String, TextureImpl> scaleImageCache = new HashMap<>();
  
  private void drawAddtionLayers(double xOffset, double yOffset) {
    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayerSP player = mc.player;
    int intValue = zoomValue;
    int aWidth = 330;//3D地图宽度
    int aHeight = 246;//3D地图高度
    int Wx = 0, Wz = 0;
    int x1 = (int)player.posX - aWidth / 2;
    int z1 = (int)player.posZ - aHeight / 2;
    int x2 = (int)player.posX + aWidth / 2;
    int z2 = (int)player.posZ + aHeight / 2;
    ArrayList<int[]> regionlist = getAreaRegionList(x1, z1, x2, z2);
    for (int[] region : regionlist) {
      int regionMinX = region[0] * 512, regionMaxX = regionMinX + 512;
      int regionMinZ = region[1] * 512, regionMaxZ = regionMinZ + 512;
      int layerMinX = x1, layerMaxX = x2;
      int layerMinZ = z1, layerMaxZ = z2;
      if (region[0] < 0) {
        regionMinX = region[0] * 512 - 1;
        regionMaxX = regionMinX + 512;
      } 
      if (region[1] < 0) {
        regionMinZ = region[1] * 512 - 1;
        regionMaxZ = regionMinZ + 512;
      } 
      BlockPos lb = gridRenderer.getBlockAtPixel(new Point2D.Double(0.0D, 0.0D));
      BlockPos re = gridRenderer.getBlockAtPixel(new Point2D.Double(gridRenderer.getWidth(), gridRenderer.getHeight()));
      AxisAlignedBB regionRect = new AxisAlignedBB(regionMinX, 0.0D, regionMinZ, regionMaxX, 1.0D, regionMaxZ);
      AxisAlignedBB selectRect = new AxisAlignedBB(layerMinX, 0.0D, layerMinZ, layerMaxX, 1.0D, layerMaxZ);
      AxisAlignedBB sector = regionRect.intersect(selectRect);
      int imgX1 = (int)(sector.minX - regionMinX);
      int imgZ1 = (int)(sector.minZ - regionMinZ);
      int imgX2 = (int)(sector.maxX - regionMinX);
      int imgZ2 = (int)(sector.maxZ - regionMinZ);
      int imgX1S = (int)(sector.minX - regionMinX);
      int imgZ1S = (int)(sector.minZ - regionMinZ);
      int imgX2S = (int)(sector.maxX - regionMinX);
      int imgZ2S = (int)(sector.maxZ - regionMinZ);
      if (region[0] != 0 || region[1] != 0);
      if (imgX1 < 0)
        imgX1 = 512 + imgX1; 
      if (imgZ1 < 0)
        imgZ1 = 512 + imgZ1; 
      if (imgX2 < 0)
        imgX2 = 512 + imgX2; 
      if (imgZ2 < 0)
        imgZ2 = 512 + imgZ2; 
      if (imgX2 < imgX1) {
        int temp = imgX1;
        imgX1 = imgX2;
        imgX2 = temp;
      } 
      if (imgZ2 < imgZ1) {
        int temp = imgZ1;
        imgZ1 = imgZ2;
        imgZ2 = temp;
      } 
      if (region[0] != -2 || region[1] == 1);
      TextureImpl textureimpl = null;
      double scale = Math.pow(2.0D, intValue);
      try {
        if (textureimpl == null) {
          if (imgX1 + imgX2 - imgX1 > 512 || imgZ1 + imgZ2 - imgZ1 > 512 || imgX2 - imgX1 == 0 || imgZ2 - imgZ1 == 0)
            continue; 
          BufferedImage rImage = getRegionImage(region[0], region[1], player.dimension);
          if (rImage == null)
            break; 
          BufferedImage subImage = rImage.getSubimage(imgX1, imgZ1, imgX2 - imgX1, imgZ2 - imgZ1);
          textureimpl = new TextureImpl(null, subImage, true, true);
        } 
      } catch (Exception e) {
        e.printStackTrace();
      } 
      if (textureimpl == null)
        continue; 
      double width = textureimpl.getWidth();
      double height = textureimpl.getHeight();
      double fixScale = 1.0D;
      if (intValue > 3)
        fixScale = scale / 8.0D; 
      GlStateManager.bindTexture(textureimpl.getGlTextureId());
      drawQuad((ResourceLocation)null, 16777215, 1.0F, sector.minX - x1 + ((region[0] < 0) ? 0 : 1), sector.minZ - z1 + ((region[1] < 0) ? 0 : 1), width * fixScale, height * fixScale, 0.0D, 0.0D, 1.0D, 1.0D, 0.0D, false, true, 770, 771, true);
      textureimpl.deleteGlTexture();
    } 
  }
  
  public static void drawQuad(ResourceLocation textureLocation, int color, float alpha, double x, double y, double width, double height, double minU, double minV, double maxU, double maxV, double rotation, boolean flip, boolean blend, int glBlendSfactor, int glBlendDFactor, boolean clampTexture) {
    GlStateManager.pushMatrix();
    if (blend) {
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(glBlendSfactor, glBlendDFactor, 1, 0);
    } 
    GlStateManager.enableTexture2D();
    if (textureLocation != null)
      Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation); 
    if (alpha > 1.0F)
      alpha /= 255.0F; 
    if (blend) {
      float[] c = RGB.floats(color);
      GlStateManager.color(c[0], c[1], c[2], alpha);
    } else {
      GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
    } 
    GL11.glTexParameteri(3553, 10241, 9729);
    GL11.glTexParameteri(3553, 10240, 9729);
    int texEdgeBehavior = clampTexture ? 33071 : 10497;
    GL11.glTexParameteri(3553, 10242, texEdgeBehavior);
    GL11.glTexParameteri(3553, 10243, texEdgeBehavior);
    if (rotation != 0.0D) {
      double transX = x + width / 2.0D;
      double transY = y + height / 2.0D;
      GlStateManager.translate(transX, transY, 0.0D);
      GlStateManager.rotate((float)rotation, 0.0F, 0.0F, 1.0F);
      GlStateManager.translate(-transX, -transY, 0.0D);
    } 
    double direction = flip ? -maxU : maxU;
    DrawUtil.startDrawingQuads(false);
    DrawUtil.addVertexWithUV(x, height + y, 0.0D, minU, maxV);
    DrawUtil.addVertexWithUV(x + width, height + y, 0.0D, direction, maxV);
    DrawUtil.addVertexWithUV(x + width, y, 0.0D, direction, minV);
    DrawUtil.addVertexWithUV(x, y, 0.0D, minU, minV);
    DrawUtil.draw();
    if (blend) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if (glBlendSfactor != 770 || glBlendDFactor != 771) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      } 
    } 
    GlStateManager.popMatrix();
  }
  
  public static HashMap<String, BufferedImage> regionManager = new HashMap<>();
  
  public BufferedImage getRegionImage(int regionX, int regionZ, int dimension) {
    String rtId = dimension + "," + regionX + "," + regionZ;
    for (Map.Entry<String, BufferedImage> entry : regionManager.entrySet()) {
      if (((String)entry.getKey()).equals(rtId))
        if (!regionUpdate)
          return entry.getValue();  
    } 
    if (regionUpdate)
      regionUpdate = false; 
    File worldDir = null;
    worldDir = FileHandler.getJMWorldDirForWorldId(Minecraft.getMinecraft(), Journeymap.getClient().getCurrentWorldId());
    RegionCoord rc = new RegionCoord(worldDir, regionX, regionZ, dimension);
    TileDrawStep tds = TileDrawStepCache.getOrCreate(MapType.day(dimension), rc, Integer.valueOf(0), false, 0, 0, 0, 0);
    BufferedImage timage = null;
    try {
      Object imageholder = getRegionTextureHolder.invoke(tds, new Object[0]);
      timage = (BufferedImage)getImage.invoke(imageholder, new Object[0]);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    BufferedImage simage = TileDrawStepCache.getOrCreate(MapType.day(dimension), rc, Integer.valueOf(0), false, 0, 0, 0, 0).getScaledRegionArea();
    if (simage != null) {
      regionManager.put(rtId, simage);
      return simage;
    } 
    regionManager.put(rtId, timage);
    return timage;
  }
  
  public static synchronized List<TileDrawStep> getTileDrawSteps(File worldDir, ChunkPos startCoord, ChunkPos endCoord, MapType mapType, Integer zoom, boolean highQuality) {
    mapType.isUnderground();
    int rx1 = RegionCoord.getRegionPos(startCoord.x);
    int rx2 = RegionCoord.getRegionPos(endCoord.x);
    int rz1 = RegionCoord.getRegionPos(startCoord.z);
    int rz2 = RegionCoord.getRegionPos(endCoord.z);
    List<TileDrawStep> drawSteps = new ArrayList<>();
    for (int rx = rx1; rx <= rx2; rx++) {
      for (int rz = rz1; rz <= rz2; rz++) {
        RegionCoord rc = new RegionCoord(worldDir, rx, rz, mapType.dimension);
        int rminCx = Math.max(rc.getMinChunkX(), startCoord.x);
        int rminCz = Math.max(rc.getMinChunkZ(), startCoord.z);
        int rmaxCx = Math.min(rc.getMaxChunkX(), endCoord.x);
        int rmaxCz = Math.min(rc.getMaxChunkZ(), endCoord.z);
        int xoffset = rc.getMinChunkX() * 16;
        int yoffset = rc.getMinChunkZ() * 16;
        int sx1 = rminCx * 16 - xoffset;
        int sy1 = rminCz * 16 - yoffset;
        drawSteps.add(TileDrawStepCache.getOrCreate(mapType, rc, zoom, highQuality, sx1, sy1, sx1 + (rmaxCx - rminCx + 1) * 16, sy1 + (rmaxCz - rminCz + 1) * 16));
      } 
    } 
    return drawSteps;
  }
  
  public static ArrayList<int[]> getAreaRegionList(int x1, int z1, int x2, int z2) {
    ArrayList<int[]> regionList = (ArrayList)new ArrayList<>();
    int lx = x1, bx = x2;
    if (x2 < x1) {
      lx = x2;
      bx = x1;
    } 
    int lz = z1, bz = z2;
    if (z2 < z1) {
      lz = z2;
      bz = z1;
    } 
    for (int x = lx; x < bx; x++) {
      for (int z = lz; z < bz; z++) {
        int rx = x / 512;
        int rz = z / 512;
        if (x < 0) {
          rx = (Math.abs(x) - 1) / 512 + 1;
          rx = -rx;
        } 
        if (z < 0) {
          rz = (Math.abs(z) - 1) / 512 + 1;
          rz = -rz;
        } 
        boolean duplicate = false;
        for (int[] region : regionList) {
          if (region[0] == rx && region[1] == rz) {
            duplicate = true;
            break;
          } 
        } 
        if (!duplicate)
          regionList.add(new int[] { rx, rz }); 
      } 
    } 
    return regionList;
  }
  
  private void beginStencil() {
    try {
      cleanup();
      DrawUtil.zLevel = 1000.0D;
      GlStateManager.colorMask(false, false, false, false);
      double width = 2000.0D;
      double height = 2000.0D;
      DrawUtil.drawRectangle(0.0D, 0.0D, width, height, 16777215, 1.0F);
      GlStateManager.colorMask(true, true, true, true);
      DrawUtil.zLevel = 0.0D;
      GlStateManager.depthMask(false);
      GlStateManager.depthFunc(516);
    } catch (Throwable t) {
      JMLogger.logOnce("Error during MiniMap.beginStencil()", t);
    } 
  }
  
  private void endStencil() {
    try {
      GlStateManager.disableDepth();
    } catch (Throwable t) {
      JMLogger.logOnce("Error during MiniMap.endStencil()", t);
    } 
  }
  
  private void cleanup() {
    try {
      DrawUtil.zLevel = 0.0D;
      GlStateManager.depthMask(true);
      GL11.glClear(256);
      GlStateManager.enableDepth();
      GlStateManager.depthFunc(515);
      GlStateManager.enableAlpha();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
    } catch (Throwable t) {
      JMLogger.logOnce("Error during MiniMap.cleanup()", t);
    } 
  }
  
  private void setLightmap() {
    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayerSP entityPlayerSP = mc.player;
    int i = mc.world.getCombinedLight(new BlockPos(((AbstractClientPlayer)entityPlayerSP).posX, ((AbstractClientPlayer)entityPlayerSP).posY + entityPlayerSP.getEyeHeight(), ((AbstractClientPlayer)entityPlayerSP).posZ), 0);
    float f = (i & 0xFFFF);
    float f1 = (i >> 16);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
  }
  
  public void checkModelFile() {
    if (map3d == null)
      map3d = MobileGearModelLoader.loader.getModel("map3d"); 
    if (radio == null)
      radio = MobileGearModelLoader.loader.getModel("radio");
  }
}
