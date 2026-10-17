import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.PathElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;

/** 图工具：创建 UAF 视图、网格布局放置元素、连线。 */
public class D {
    final Diagram diagram;
    final DiagramPresentationElement dpe;
    final Map<Element, ShapeElement> shapes = new LinkedHashMap<>();
    int cursorX = 30, cursorY = 30, rowH = 0;
    final int maxW;

    D(String uafViewType, Package owner, String name, int maxWidth) {
        this.maxW = maxWidth;
        try {
            this.diagram = U.M.createDiagram(uafViewType, owner);
            this.diagram.setName(name);
        } catch (Exception e) {
            throw new RuntimeException("创建视图失败 " + uafViewType + ": " + e, e);
        }
        this.dpe = U.P.getDiagram(diagram);
        if (dpe == null) throw new IllegalStateException("无法获取视图展示元素: " + name);
        dpe.open();
        if (uafViewType.endsWith("Mapping Matrix") || uafViewType.endsWith("Connectivity Matrix"))
            U.matrixWholeModel(diagram);
    }

    /** 换行式放置元素，返回形状。 */
    ShapeElement add(Element e) {
        return add(e, 190, 70);
    }

    ShapeElement add(Element e, int w, int h) {
        if (shapes.containsKey(e)) return shapes.get(e);
        if (cursorX + w > maxW) {
            cursorX = 30;
            cursorY += rowH + 50;
            rowH = 0;
        }
        try {
            ShapeElement s = U.V.createShapeElement(e, dpe, false, new Point(cursorX, cursorY));
            if (s == null) {
                U.log("警告: 无法在图中放置 " + e.getHumanName());
                return null;
            }
            U.V.reshapeShapeElement(s, new Rectangle(cursorX, cursorY, w, h));
            shapes.put(e, s);
            cursorX += w + 40;
            rowH = Math.max(rowH, h);
            return s;
        } catch (Exception ex) {
            U.log("警告: 放置元素失败 " + e.getHumanName() + " : " + ex);
            return null;
        }
    }

    /** 在指定坐标放置元素。 */
    ShapeElement at(Element e, int x, int y, int w, int h) {
        if (shapes.containsKey(e)) return shapes.get(e);
        try {
            ShapeElement s = U.V.createShapeElement(e, dpe, false, new Point(x, y));
            if (s == null) {
                U.log("警告: 无法在图中放置 " + e.getHumanName());
                return null;
            }
            U.V.reshapeShapeElement(s, new Rectangle(x, y, w, h));
            shapes.put(e, s);
            return s;
        } catch (Exception ex) {
            U.log("警告: 放置元素失败 " + e.getHumanName() + " : " + ex);
            return null;
        }
    }

    /** 为已在图中的两个元素画关系线（关系元素已存在于模型中）。 */
    PathElement link(Element rel, Element from, Element to) {
        ShapeElement a = shapes.get(from), b = shapes.get(to);
        if (a == null || b == null) return null;
        try {
            return U.V.createPathElement(rel, a, b);
        } catch (Exception ex) {
            U.log("警告: 连线失败 " + rel.getHumanName() + " : " + ex);
            return null;
        }
    }

    void nextRow() {
        cursorX = 30;
        cursorY += rowH + 50;
        rowH = 0;
    }

    void text(String s, int x, int y, int w, int h) {
        try {
            ShapeElement t = U.V.createTextBox(dpe, new Point(x, y));
            if (t != null) {
                U.V.setText(t, s);
                U.V.reshapeShapeElement(t, new Rectangle(x, y, w, h));
            }
        } catch (Exception ex) {
            U.log("警告: 文本框失败 : " + ex);
        }
    }
}
