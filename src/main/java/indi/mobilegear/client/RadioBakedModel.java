package indi.mobilegear.client;

import java.util.List;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.commons.lang3.tuple.Pair;

public class RadioBakedModel implements IBakedModel {
  IBakedModel guiModel = null;
  
  public RadioBakedModel(IBakedModel guiModel) {
    this.guiModel = guiModel;
  }
  
  public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
    return null;
  }
  
  public boolean isAmbientOcclusion() {
    return false;
  }
  
  public boolean isGui3d() {
    return false;
  }
  
  public boolean isBuiltInRenderer() {
    return true;
  }
  
  public TextureAtlasSprite getParticleTexture() {
    return null;
  }
  
  public ItemOverrideList getOverrides() {
    return ItemOverrideList.NONE;
  }
  
  public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
    mobileTEISR.transformType = cameraTransformType;
    if (cameraTransformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND || cameraTransformType == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
      return ForgeHooksClient.handlePerspective(this, cameraTransformType); 
    return ForgeHooksClient.handlePerspective(this.guiModel, cameraTransformType);
  }
}
