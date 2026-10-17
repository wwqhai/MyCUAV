import com.nomagic.magicdraw.commandline.CommandLine;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/** 独立验证：全新进程打开工程，逐个打开视图，统计元素/构造型/视图内容。 */
public class Verify extends CommandLine {
    public static void main(String[] a) throws Exception {
        new Verify().launch(a);
    }

    @Override
    protected byte execute() {
        List<String> out = new ArrayList<>();
        try {
            File f = new File(System.getProperty("ver.in"));
            Application.getInstance().getProjectsManager()
                .loadProject(ProjectDescriptorsFactory.createProjectDescriptor(f.toURI()), true);
            Project p = Application.getInstance().getProject();
            out.add("PROJECT " + p.getName());
            Map<String, Integer> st = new TreeMap<>();
            int total = 0;
            Deque<Element> q = new ArrayDeque<>();
            q.add(p.getPrimaryModel());
            while (!q.isEmpty()) {
                Element e = q.poll();
                total++;
                for (Stereotype s : StereotypesHelper.getStereotypes(e)) st.merge(s.getName(), 1, Integer::sum);
                q.addAll(e.getOwnedElement());
            }
            out.add("ELEMENTS " + total);
            for (Map.Entry<String, Integer> en : st.entrySet()) out.add("ST " + en.getKey() + " " + en.getValue());
            int bad = 0, nd = 0;
            for (DiagramPresentationElement dp0 : p.getDiagrams()) {
                Diagram d = dp0.getDiagram();
                DiagramPresentationElement dp = p.getDiagram(d);
                if (dp == null) {
                    bad++;
                    out.add("BADDIAGRAM " + d.getName());
                    continue;
                }
                dp.open();
                nd++;
                out.add("DIAGRAM " + d.getHumanType() + " | " + d.getName() + " | shapes=" + dp.getPresentationElements().size());
            }
            out.add("DIAGRAMS " + nd + " BAD " + bad);
        } catch (Throwable t) {
            out.add("FAIL " + t);
            for (StackTraceElement s : t.getStackTrace()) out.add("  at " + s);
        }
        try (PrintWriter w = new PrintWriter(System.getProperty("ver.log"), "UTF-8")) {
            for (String s : out) w.println(s);
        } catch (Exception ignore) {
        }
        return 0;
    }
}
