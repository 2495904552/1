package sgr792.craftxmap.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
        List<Vec3d> vec = new ArrayList();
        List<Vec3d> vns = new ArrayList();
        List<Vec2f> tex = new ArrayList();
        this.faces = new HashMap();
        List<Face> curr = null;

        String ln;
        while((ln = bufferedReader.readLine()) != null) {
            String[] sp = ln.split(" ");
            switch (sp[0]) {
                case "v":
                    try {
                        vec.add(new Vec3d(Double.parseDouble(sp[1]), Double.parseDouble(sp[2]), Double.parseDouble(sp[3])));
                        break;
                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException var18) {
                        throw new IOException("", var18);
                    }
                case "vt":
                    try {
                        tex.add(new Vec2f(Float.parseFloat(sp[1]), Float.parseFloat(sp[2])));
                        break;
                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException var17) {
                        throw new IOException("", var17);
                    }
                case "vn":
                    try {
                        vns.add(new Vec3d(Double.parseDouble(sp[1]), Double.parseDouble(sp[2]), Double.parseDouble(sp[3])));
                        break;
                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException var16) {
                        throw new IOException("", var16);
                    }
                case "o":
                case "g":
                    String g = sp[1];
                    int i = g.indexOf(58);
                    if (i > 0) {
                        g = g.substring(0, i);
                    }

                    curr = (List)this.faces.computeIfAbsent(g, (e) -> {
                        return new ArrayList();
                    });
                    break;
                case "f":
                    String[] v1 = sp[1].split("/");
                    String[] v2 = sp[2].split("/");
                    String[] v3 = sp[3].split("/");
                    Face f = new Face();

                    try {
                        f.vecs[0] = (Vec3d)getParsedValue(vec, v1[0]);
                        f.vecs[1] = (Vec3d)getParsedValue(vec, v2[0]);
                        f.vecs[2] = (Vec3d)getParsedValue(vec, v3[0]);
                        f.uvs[0] = (Vec2f)getParsedValue(tex, v1[1]);
                        f.uvs[1] = (Vec2f)getParsedValue(tex, v2[1]);
                        f.uvs[2] = (Vec2f)getParsedValue(tex, v3[1]);
                        f.vns[0] = (Vec3d)getParsedValue(vns, v1[2]);
                        f.vns[1] = (Vec3d)getParsedValue(vns, v2[2]);
                        f.vns[2] = (Vec3d)getParsedValue(vns, v3[2]);
                    } catch (IndexOutOfBoundsException | NumberFormatException var15) {
                        throw new IOException("", var15);
                    }

                    if (curr == null) {
                        curr = new ArrayList();
                        this.faces.put("default", curr);
                    }

                    ((List)curr).add(f);
            }
        }

    }

    private static <T> T getParsedValue(List<T> list, String toParse) throws NumberFormatException, IndexOutOfBoundsException {
        return toParse.isEmpty() ? null : list.get(Integer.parseInt(toParse) - 1);
    }

    public boolean renderNormal(String name, BufferBuilder bb) {
        return this.renderNormal(name, bb, (Matrix4f)null);
    }

    public boolean renderNormal(BufferBuilder bb, int[] singleGroup, int signleDraw) {
        return this.renderNormal(bb, (Matrix4f)null, singleGroup, signleDraw);
    }

    public boolean renderNormal(BufferBuilder bb) {
        return this.renderNormal(bb, (Matrix4f)null, (int[])null, -1);
    }

    public boolean renderNormal(String name, BufferBuilder bb, Matrix4f m4f) {
        List<Face> l = (List)this.faces.get(name);
        if (l == null) {
            return false;
        } else {
            bb.begin(4, DefaultVertexFormats.POSITION_TEX_NORMAL);
            UnaryOperator<Vec2f> remap = (uv) -> {
                return new Vec2f(uv.x, 1.0F - uv.y);
            };
            l.forEach((f) -> {
                addVert(bb, f, 0, remap);
                addVert(bb, f, 1, remap);
                addVert(bb, f, 2, remap);
            });
            Tessellator.getInstance().draw();
            return true;
        }
    }

    public boolean renderNormal(BufferBuilder bb, Matrix4f m4f, int[] singleGroup, int singleDraw) {
        int index = 0;
        Iterator var6 = this.faces.entrySet().iterator();

        while(true) {
            while(var6.hasNext()) {
                Map.Entry<String, List<Face>> entry = (Map.Entry)var6.next();
                List<Face> l = (List)entry.getValue();
                if (l == null) {
                    return false;
                }

                if (singleGroup != null) {
                    boolean jump = false;
                    int[] var10 = singleGroup;
                    int var11 = singleGroup.length;

                    for(int var12 = 0; var12 < var11; ++var12) {
                        int jumpIndex = var10[var12];
                        if (index == jumpIndex) {
                            jump = true;
                        }
                    }

                    if (jump) {
                        ++index;
                        continue;
                    }
                }

                if (singleDraw != -1 && index != singleDraw) {
                    ++index;
                } else {
                    ++index;
                    bb.begin(4, DefaultVertexFormats.POSITION_TEX_NORMAL);
                    UnaryOperator<Vec2f> remap = (uv) -> {
                        return new Vec2f(uv.x, 1.0F - uv.y);
                    };
                    l.forEach((f) -> {
                        addVert(bb, f, 0, remap);
                        addVert(bb, f, 1, remap);
                        addVert(bb, f, 2, remap);
                    });
                    Tessellator.getInstance().draw();
                }
            }

            return true;
        }
    }

    public void renderReUV(BufferBuilder bb, UnaryOperator<Vec2f> remap) {
        bb.begin(4, DefaultVertexFormats.POSITION_TEX);
        this.faces.values().stream().flatMap(Collection::stream).forEach((f) -> {
            addVert(bb, f, 0, remap);
            addVert(bb, f, 1, remap);
            addVert(bb, f, 2, remap);
        });
        Tessellator.getInstance().draw();
    }

    private static void addVert(BufferBuilder bb, Face f, int ind, UnaryOperator<Vec2f> remap) {
        Vec2f uv = remap != null ? (Vec2f)remap.apply(f.uvs[ind]) : f.uvs[ind];
        Vec3d v = new Vec3d(f.vecs[ind].x, f.vecs[ind].y, f.vecs[ind].z);
        Vec3d n = new Vec3d(f.vns[ind].x, f.vns[ind].y, 1.0 - f.vns[ind].z);
        bb.pos(v.x, v.y, v.z).tex((double)uv.x, (double)uv.y).normal((float)n.x, (float)n.y, (float)n.z).endVertex();
    }

    private static class Face {
        Vec3d[] vecs;
        Vec3d[] vns;
        Vec2f[] uvs;

        private Face() {
            this.vecs = new Vec3d[3];
            this.vns = new Vec3d[3];
            this.uvs = new Vec2f[3];
        }
    }
}
