package indi.mobilegear.client.util;

import java.util.function.UnaryOperator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec2f;

public abstract class abstractObjModel {
  public abstract boolean renderNormal(String paramString, BufferBuilder paramBufferBuilder);
  
  public abstract boolean renderNormal(BufferBuilder paramBufferBuilder);
  
  public abstract boolean renderNormal(BufferBuilder paramBufferBuilder, int[] paramArrayOfint, int paramInt);
  
  public abstract void renderReUV(BufferBuilder paramBufferBuilder, UnaryOperator<Vec2f> paramUnaryOperator);
}
