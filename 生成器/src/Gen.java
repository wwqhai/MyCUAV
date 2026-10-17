import com.nomagic.magicdraw.commandline.CommandLine;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.openapi.uml.SessionManager;

import java.io.File;
import java.io.PrintWriter;

/**
 * 入口：以 UAF 模板为底板，注入 UAF 模型并保存为 .mdzip。
 * 系统属性：gen.in 模板路径；gen.out 输出路径；gen.log 结果日志；gen.name 工程名；gen.only 只跑某一块(可选)。
 * 使用时在 BLOCKS 里按依赖顺序登记各块（B1..Bn），每块是 Runnable。
 */
public class Gen extends CommandLine {
    /** 按依赖顺序执行的建模块。新增块时在此追加。 */
    static final Runnable[] BLOCKS = {B1::run, B2::run, B3::run, B4::run, B5::run};
    static final String[] NAMES = {"B1", "B2", "B3", "B4", "B5"};

    public static void main(String[] a) throws Exception {
        new Gen().launch(a);
    }

    @Override
    protected byte execute() {
        String logPath = System.getProperty("gen.log");
        try {
            ProjectsManager pm = Application.getInstance().getProjectsManager();
            File in = new File(System.getProperty("gen.in"));
            File out = new File(System.getProperty("gen.out"));
            pm.loadProject(ProjectDescriptorsFactory.createProjectDescriptor(in.toURI()), true);
            Project p = Application.getInstance().getProject();
            U.init();
            SessionManager sm = SessionManager.getInstance();
            sm.createSession(p, "UAF 建模");
            try {
                String only = System.getProperty("gen.only", "");
                for (int i = 0; i < BLOCKS.length; i++) {
                    BLOCKS[i].run();
                    if (!only.isEmpty() && only.equals(NAMES[i])) break;
                }
                sm.closeSession(p);
            } catch (Throwable t) {
                sm.cancelSession(p);
                throw t;
            }
            p.setName(System.getProperty("gen.name", "重点区域反无人机"));
            ProjectDescriptor pd = ProjectDescriptorsFactory.createLocalProjectDescriptor(p, out);
            pm.saveProject(pd, true);
            U.log("已保存: " + out + " 大小=" + out.length());
            pm.closeProject();
            pm.loadProject(ProjectDescriptorsFactory.createProjectDescriptor(out.toURI()), true);
            U.log("重新打开成功: " + Application.getInstance().getProject().getName());
        } catch (Throwable t) {
            U.log("失败: " + t);
            t.printStackTrace(System.out);
        } finally {
            if (logPath != null) {
                try (PrintWriter w = new PrintWriter(logPath, "UTF-8")) {
                    for (String s : U.LOG) w.println(s);
                } catch (Exception ignore) {
                }
            }
        }
        return 0;
    }
}
