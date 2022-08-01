package sgr792.craftxmap.client.util;

import java.util.function.UnaryOperator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec2f;

public abstract class abstractObjModel {
  public abstractObjModel() {
  }

  public abstract boolean renderNormal(String var1, BufferBuilder var2);

  public abstract boolean renderNormal(BufferBuilder var1);

  public abstract boolean renderNormal(BufferBuilder var1, int[] var2, int var3);

  public abstract void renderReUV(BufferBuilder var1, UnaryOperator<Vec2f> var2);
}
