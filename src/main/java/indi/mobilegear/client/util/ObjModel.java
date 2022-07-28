package indi.mobilegear.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import javax.vecmath.Matrix4f;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ObjModel extends abstractObjModel {
  private Map<String, List<Face>> faces;
  
  public ObjModel(BufferedReader bufferedReader) throws IOException {
    List<Vec3d> vec = new ArrayList<>();
    List<Vec3d> vns = new ArrayList<>();
    List<Vec2f> tex = new ArrayList<>();
    this.faces = new HashMap<>();
    List<Face> curr = null;
    String ln;
    while ((ln = bufferedReader.readLine()) != null) {
      String g, v1[];
      int i;
      String[] v2, v3;
      Face f;
      String[] sp = ln.split(" ");
      switch (sp[0]) {
        case "v":
          try {
            vec.add(new Vec3d(Double.parseDouble(sp[1]), Double.parseDouble(sp[2]), Double.parseDouble(sp[3])));
          } catch (NumberFormatException|ArrayIndexOutOfBoundsException e) {
            throw new IOException("", e);
          } 
        case "vt":
          try {
            tex.add(new Vec2f(Float.parseFloat(sp[1]), Float.parseFloat(sp[2])));
          } catch (NumberFormatException|ArrayIndexOutOfBoundsException e) {
            throw new IOException("", e);
          } 
        case "vn":
          try {
            vns.add(new Vec3d(Double.parseDouble(sp[1]), Double.parseDouble(sp[2]), Double.parseDouble(sp[3])));
          } catch (NumberFormatException|ArrayIndexOutOfBoundsException e) {
            throw new IOException("", e);
          } 
        case "o":
        case "g":
          g = sp[1];
          i = g.indexOf(':');
          if (i > 0)
            g = g.substring(0, i); 
          curr = this.faces.computeIfAbsent(g, e -> new ArrayList());
        case "f":
          v1 = sp[1].split("/");
          v2 = sp[2].split("/");
          v3 = sp[3].split("/");
          f = new Face();
          try {
            f.vecs[0] = getParsedValue(vec, v1[0]);
            f.vecs[1] = getParsedValue(vec, v2[0]);
            f.vecs[2] = getParsedValue(vec, v3[0]);
            f.uvs[0] = getParsedValue(tex, v1[1]);
            f.uvs[1] = getParsedValue(tex, v2[1]);
            f.uvs[2] = getParsedValue(tex, v3[1]);
            f.vns[0] = getParsedValue(vns, v1[2]);
            f.vns[1] = getParsedValue(vns, v2[2]);
            f.vns[2] = getParsedValue(vns, v3[2]);
          } catch (NumberFormatException|IndexOutOfBoundsException e) {
            throw new IOException("", e);
          } 
          if (curr == null) {
            curr = new ArrayList<>();
            this.faces.put("default", curr);
          } 
          curr.add(f);
      } 
    } 
  }
  
  private static <T> T getParsedValue(List<T> list, String toParse) throws NumberFormatException, IndexOutOfBoundsException {
    if (toParse.isEmpty())
      return null; 
    return list.get(Integer.parseInt(toParse) - 1);
  }
  
  private static class Face {
    private Face() {}
    
    Vec3d[] vecs = new Vec3d[3];
    
    Vec3d[] vns = new Vec3d[3];
    
    Vec2f[] uvs = new Vec2f[3];
  }
  
  public boolean renderNormal(String name, BufferBuilder bb) {
    return renderNormal(name, bb, (Matrix4f)null);
  }
  
  public boolean renderNormal(BufferBuilder bb, int[] singleGroup, int signleDraw) {
    return renderNormal(bb, null, singleGroup, signleDraw);
  }
  
  public boolean renderNormal(BufferBuilder bb) {
    return renderNormal(bb, null, null, -1);
  }
  
  public boolean renderNormal(String name, BufferBuilder bb, Matrix4f m4f) {
    List<Face> l = this.faces.get(name);
    if (l == null)
      return false; 
    bb.begin(4, DefaultVertexFormats.POSITION_TEX_NORMAL);
    UnaryOperator<Vec2f> remap = uv -> new Vec2f(uv.x, 1.0F - uv.y);
    l.forEach(f -> {
          addVert(bb, f, 0, remap);
          addVert(bb, f, 1, remap);
          addVert(bb, f, 2, remap);
        });
    Tessellator.getInstance().draw();
    return true;
  }
  
  public boolean renderNormal(BufferBuilder bb, Matrix4f m4f, int[] singleGroup, int singleDraw) {
    int index = 0;
    for (Map.Entry<String, List<Face>> entry : this.faces.entrySet()) {
      List<Face> l = entry.getValue();
      if (l == null)
        return false; 
      if (singleGroup != null) {
        boolean jump = false;
        for (int jumpIndex : singleGroup) {
          if (index == jumpIndex)
            jump = true; 
        } 
        if (jump) {
          index++;
          continue;
        } 
      } 
      if (singleDraw != -1 && index != singleDraw) {
        index++;
        continue;
      } 
      index++;
      bb.begin(4, DefaultVertexFormats.POSITION_TEX_NORMAL);
      UnaryOperator<Vec2f> remap = uv -> new Vec2f(uv.x, 1.0F - uv.y);
      l.forEach(f -> {
            addVert(bb, f, 0, remap);
            addVert(bb, f, 1, remap);
            addVert(bb, f, 2, remap);
          });
      Tessellator.getInstance().draw();
    } 
    return true;
  }
  
  public void renderReUV(BufferBuilder bb, UnaryOperator<Vec2f> remap) {
    bb.begin(4, DefaultVertexFormats.POSITION_TEX);
    this.faces.values().stream().flatMap(Collection::stream).forEach(f -> {
          addVert(bb, f, 0, remap);
          addVert(bb, f, 1, remap);
          addVert(bb, f, 2, remap);
        });
    Tessellator.getInstance().draw();
  }
  
  private static void addVert(BufferBuilder bb, Face f, int ind, UnaryOperator<Vec2f> remap) {
    Vec2f uv = (remap != null) ? remap.apply(f.uvs[ind]) : f.uvs[ind];
    Vec3d v = new Vec3d((f.vecs[ind]).x, (f.vecs[ind]).y, (f.vecs[ind]).z);
    Vec3d n = new Vec3d((f.vns[ind]).x, (f.vns[ind]).y, 1.0D - (f.vns[ind]).z);
    bb.pos(v.x, v.y, v.z).tex(uv.x, uv.y)
      .normal((float)n.x, (float)n.y, (float)n.z)
      .endVertex();
  }
}
