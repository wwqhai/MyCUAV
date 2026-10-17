import com.nomagic.magicdraw.commandline.CommandLine;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.dependencymatrix.persistence.PersistenceManager;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 后处理：把工程里所有依赖矩阵视图的行/列范围设为"已定义"，范围取整个模型，与官方 UAF 模板一致。
 * 依赖矩阵插件类不能放进 Gen 的类路径（会让 UAF 视图类型注册失败），所以放在独立进程中处理。
 * 系统属性：post.in 输入 mdzip；post.out 输出 mdzip；post.log 日志。
 */
public class PostMatrix extends CommandLine {
    public static void main(String[] a) throws Exception {
        new PostMatrix().launch(a);
    }

    /** 能力×需求追溯：行 = Capability，列 = SysML Requirement，准则 = SysML Satisfy（client=能力，supplier=需求）。 */
    static void reconfigCapReq(Project p, PersistenceManager m, List<String> out) {
        Stereotype cap = StereotypesHelper.getStereotype(p, "Capability", StereotypesHelper.getProfile(p, "UAF"));
        Stereotype req = StereotypesHelper.getStereotype(p, "Requirement", StereotypesHelper.getProfile(p, "SysML"));
        Stereotype sat = StereotypesHelper.getStereotype(p, "Satisfy", StereotypesHelper.getProfile(p, "SysML"));
        m.getRowSettings().setElementTypes(new ArrayList<Element>(java.util.Collections.singletonList(cap)));
        m.getColumnSettings().setElementTypes(new ArrayList<Element>(java.util.Collections.singletonList(req)));
        String q = "\"";
        String xml = "<?xml version=" + q + "1.0" + q + " encoding=" + q + "UTF-8" + q + " standalone=" + q + "yes" + q + "?>"
            + "<callExpressionSpecification includeCustomTypes=" + q + "true" + q + " includeSubtypes=" + q + "false" + q
            + " xmlns=" + q + "http://www.nomagic.com/schemas/MagicDraw/StructuredExpression/2013" + q + ">"
            + "<taggedValues><entry key=" + q + "name" + q + "><value>Satisfy</value></entry></taggedValues>"
            + "<argument xsi:type=" + q + "lookupExpressionSpecification" + q + " symbol=" + q + "THIS" + q
            + " xmlns:xsi=" + q + "http://www.w3.org/2001/XMLSchema-instance" + q + "/>"
            + "<expression xsi:type=" + q + "dslRelationExpressionSpecification" + q + " stereotype=" + q + sat.getID() + q
            + " direction=" + q + "DIRECT" + q + " includeCustomTypes=" + q + "true" + q + " includeSubtypes=" + q + "true" + q
            + " xmlns:xsi=" + q + "http://www.w3.org/2001/XMLSchema-instance" + q + ">"
            + "<taggedValues><entry key=" + q + "name" + q + "><value>Satisfy</value></entry></taggedValues>"
            + "</expression></callExpressionSpecification>";
        m.getMatrixSettings().setDependencyCriteria(new ArrayList<String>(java.util.Collections.singletonList(xml)));
        out.add("已重配 能力与需求追溯: 行=Capability 列=Requirement 准则=Satisfy(" + sat.getID() + ")");
    }

    @Override
    protected byte execute() {
        List<String> out = new ArrayList<>();
        try {
            ProjectsManager pm = Application.getInstance().getProjectsManager();
            File in = new File(System.getProperty("post.in"));
            File dst = new File(System.getProperty("post.out"));
            pm.loadProject(ProjectDescriptorsFactory.createProjectDescriptor(in.toURI()), true);
            Project p = Application.getInstance().getProject();
            SessionManager sm = SessionManager.getInstance();
            sm.createSession(p, "矩阵范围");
            int n = 0;
            for (DiagramPresentationElement dp : p.getDiagrams()) {
                Diagram d = dp.getDiagram();
                if (!StereotypesHelper.hasStereotype(d, "DependencyMatrix")) continue;
                // 只处理 UAF 视图矩阵，跳过模板自带的单位库矩阵
                if (!StereotypesHelper.getStereotypes(d).stream().anyMatch(st -> st.getName().contains("Mapping Matrix")
                    || st.getName().contains("Connectivity Matrix"))) continue;
                PersistenceManager m = new PersistenceManager(d);
                if ("能力与需求追溯".equals(d.getName())) reconfigCapReq(p, m, out);
                m.getMatrixSettings().setTakeWholeModelAsScope(true);
                m.getRowSettings().setScopeDefined(true);
                m.getColumnSettings().setScopeDefined(true);
                out.add("矩阵: " + d.getName() + " row=" + m.getRowSettings().isScopeDefined()
                    + " col=" + m.getColumnSettings().isScopeDefined());
                n++;
            }
            sm.closeSession(p);
            ProjectDescriptor pd = ProjectDescriptorsFactory.createLocalProjectDescriptor(p, dst);
            pm.saveProject(pd, true);
            out.add("已处理矩阵 " + n + " 个，保存: " + dst + " 大小=" + dst.length());
        } catch (Throwable t) {
            out.add("失败: " + t);
            for (StackTraceElement s : t.getStackTrace()) out.add("  at " + s);
        }
        try (PrintWriter w = new PrintWriter(System.getProperty("post.log"), "UTF-8")) {
            for (String s : out) w.println(s);
        } catch (Exception ignore) {
        }
        return 0;
    }
}
