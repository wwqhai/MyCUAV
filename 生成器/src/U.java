import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.PresentationElementsManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

import java.awt.Point;
import java.util.*;

/** 公共工具：元素创建、构造型应用、图创建与布局。所有方法在同一个 Session 内调用。 */
public class U {
    static Project P;
    static ElementsFactory F;
    static ModelElementsManager M;
    static PresentationElementsManager V;
    static final Map<String, Element> REG = new LinkedHashMap<>();
    static final List<String> LOG = new ArrayList<>();

    static void init() {
        P = Application.getInstance().getProject();
        F = P.getElementsFactory();
        M = ModelElementsManager.getInstance();
        V = PresentationElementsManager.getInstance();
    }

    static void log(String s) {
        LOG.add(s);
        System.out.println("[GEN] " + s);
    }

    /** 按名称路径查找包，如 pkg("Strategy","Strategic Taxonomy")。 */
    static Package pkg(String... path) {
        Element cur = P.getPrimaryModel();
        for (String n : path) {
            Element nx = null;
            for (Element e : cur.getOwnedElement())
                if (e instanceof Package && n.equals(((Package) e).getName())) { nx = e; break; }
            if (nx == null) throw new IllegalStateException("找不到包: " + String.join("/", path));
            cur = nx;
        }
        return (Package) cur;
    }

    static Stereotype stereo(String profile, String name) {
        Profile pr = StereotypesHelper.getProfile(P, profile);
        Stereotype s = pr == null ? null : StereotypesHelper.getStereotype(P, name, pr);
        if (s == null) log("警告: 未找到构造型 " + profile + "::" + name);
        return s;
    }

    static void apply(Element e, String profile, String name) {
        Stereotype s = stereo(profile, name);
        if (s != null) StereotypesHelper.addStereotype(e, s);
    }

    static void tag(Element e, String profile, String st, String prop, Object val) {
        Stereotype s = stereo(profile, st);
        if (s == null) return;
        try {
            StereotypesHelper.setStereotypePropertyValue(e, s, prop, val);
        } catch (Exception ex) {
            log("警告: 设置标记值失败 " + st + "." + prop + " : " + ex);
        }
    }

    /** 依赖矩阵视图的范围设置：依赖矩阵插件类不在 Gen 的类路径上，这里只登记，由 Post 步骤统一处理。 */
    static final List<Diagram> MATRICES = new ArrayList<>();

    static void matrixWholeModel(Diagram d) {
        MATRICES.add(d);
    }

    /** 设置 DiagramTable 的标记值（行元素 rowElements、范围 scope 等）。 */
    static void tableTag(Diagram d, String prop, Object val) {
        Stereotype s = stereo("MagicDraw Profile", "DiagramTable");
        if (s == null) return;
        try {
            StereotypesHelper.setStereotypePropertyValue(d, s, prop, val);
        } catch (Exception ex) {
            log("警告: 设置表格属性失败 " + prop + " : " + ex);
        }
    }

    /** 表格类视图：把给定元素作为行，列保持 UAF 默认。 */
    static void tableRows(Diagram d, java.util.Collection<? extends Element> rows) {
        tableTag(d, "rowElements", new ArrayList<Element>(rows));
        tableTag(d, "additionalElements", new ArrayList<Element>(rows));
    }

    static <T extends Element> T put(T e, Element owner) {
        try {
            M.addElement(e, owner);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return e;
    }

    static void reg(String key, Element e) {
        REG.put(key, e);
    }

    @SuppressWarnings("unchecked")
    static <T extends Element> T get(String key) {
        Element e = REG.get(key);
        if (e == null) throw new IllegalStateException("未登记元素: " + key);
        return (T) e;
    }

    /** 创建类并可选应用 UAF 构造型，同时登记 key。 */
    static Class cls(Package owner, String key, String name, String profile, String st) {
        Class c = put(F.createClassInstance(), owner);
        c.setName(name);
        if (st != null) apply(c, profile, st);
        reg(key, c);
        return c;
    }

    static Class uaf(Package owner, String key, String name, String st) {
        return cls(owner, key, name, "UAF", st);
    }

    static Comment note(Element owner, Element annotated, String body) {
        Comment c = put(F.createCommentInstance(), owner);
        c.setBody(body);
        if (annotated != null) c.getAnnotatedElement().add(annotated);
        return c;
    }

    static Generalization gen(Classifier special, Classifier general) {
        Generalization g = F.createGeneralizationInstance();
        g.setGeneral(general);
        g.setSpecific(special);
        g.setOwner(special);
        return g;
    }

    /** 创建 Abstraction/Dependency 类关系并应用构造型（st 可为 null）。 */
    static Abstraction abs(Element client, Element supplier, Package owner, String profile, String st) {
        Abstraction a = put(F.createAbstractionInstance(), owner);
        a.getClient().add((NamedElement) client);
        a.getSupplier().add((NamedElement) supplier);
        if (st != null) apply(a, profile, st);
        return a;
    }

    static Dependency dep(Element client, Element supplier, Package owner, String name) {
        Dependency d = put(F.createDependencyInstance(), owner);
        d.getClient().add((NamedElement) client);
        d.getSupplier().add((NamedElement) supplier);
        if (name != null) d.setName(name);
        return d;
    }

    /** 组合关联：whole 拥有 part，返回关联。mult 为部件多重性文本，如 "3"。 */
    static Association comp(Class whole, Classifier part, String role, String mult) {
        Association as = put(F.createAssociationInstance(), whole.getOwner());
        Property end1 = F.createPropertyInstance();
        end1.setType(whole);
        Property end2 = F.createPropertyInstance();
        end2.setType(part);
        end2.setName(role);
        end2.setAggregation(AggregationKindEnum.COMPOSITE);
        as.getOwnedEnd().add(end1);
        as.getMemberEnd().add(end2);
        as.getMemberEnd().add(end1);
        whole.getOwnedAttribute().add(end2);
        if (mult != null) ModelHelper.setMultiplicity(mult, end2);
        return as;
    }

    static Property attr(Class c, String name, long defVal) {
        Property p = F.createPropertyInstance();
        p.setName(name);
        LiteralInteger li = F.createLiteralIntegerInstance();
        li.setValue((int) defVal);
        p.setDefaultValue(li);
        c.getOwnedAttribute().add(p);
        return p;
    }

    static Property attrS(Class c, String name, String defVal) {
        Property p = F.createPropertyInstance();
        p.setName(name);
        if (defVal != null) {
            LiteralString ls = F.createLiteralStringInstance();
            ls.setValue(defVal);
            p.setDefaultValue(ls);
        }
        c.getOwnedAttribute().add(p);
        return p;
    }
}
