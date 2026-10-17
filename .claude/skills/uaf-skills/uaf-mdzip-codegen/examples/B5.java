import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.Interaction;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.Lifeline;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.Message;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.MessageSortEnum;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.*;

import java.awt.Rectangle;
import java.util.*;

/** 第五部分：需求与追溯、实际资源、约束、状态、顺序。 */
public class B5 {
    /** 需求：编号, 文本, 能力, 作战活动 */
    static final String[][] REQ = {
        {"R1", "系统应在目标进入探测范围后完成发现并上报航迹", "C1,C2", "OA1,OA2,OA3,OA4"},
        {"R2", "多源信息应融合形成统一航迹，融合时延不大于 X s", "C3", "OA3"},
        {"R3", "系统应识别目标类型并判断意图", "C4", "OA5"},
        {"R4", "AI 指挥系统应给出作战方案，由指挥员最终决策", "C5", "OA6,OA7"},
        {"R5", "导弹系统应在近界 30 km 至远界 260 km 内实施拦截", "C6", "OA9,OA10"},
        {"R6", "打击过程中应持续跟踪并更新航迹", "C1,C2", "OA4"},
        {"R7", "应完成毁伤评估并上报，态势全程可视", "C7,C8", "OA11,OA12"}};

    static void run() {
        requirements();
        actualResources();
        constraints();
        states();
        sequences();
    }

    static void requirements() {
        Package rp = U.put(U.F.createPackageInstance(), U.P.getPrimaryModel());
        rp.setName("Requirements");
        U.apply(rp, "UAF Packages", "Requirements");
        Package str = U.pkg("Strategy", "Strategic Traceability");
        Package otr = U.pkg("Operational", "Operational Traceability");
        Package rtr = U.pkg("Resources", "Resources Traceability");
        for (String[] r : REQ) {
            Class q = U.put(U.F.createClassInstance(), rp);
            q.setName(r[0]);
            U.apply(q, "SysML", "Requirement");
            U.tag(q, "SysML", "Requirement", "Id", r[0]);
            U.tag(q, "SysML", "Requirement", "Text", r[1]);
            U.reg("req:" + r[0], q);
        }
        D dst = new D("Actual Strategic Phases to Capabilities Mapping Matrix", str, "能力与需求追溯", 1500);
        for (String[] r : REQ) {
            Class q = U.get("req:" + r[0]);
            for (String c : r[2].split(",")) {
                // 能力 → 需求：能力满足需求 (Satisfy)
                Abstraction a = U.abs(U.get("cap:" + c), q, str, "SysML", "Satisfy");
            }
            for (String oa : r[3].split(",")) {
                U.abs(U.get("oa:" + oa), q, otr, "SysML", "Satisfy");
            }
        }
        // 资源满足需求
        String[][] rs = {{"r_radar", "R1,R6,R7"}, {"r_esm", "R1,R6,R7"}, {"r_edge", "R2,R3"}, {"r_cp", "R4,R7"}, {"r_sam", "R5"}};
        for (String[] r : rs)
            for (String q : r[1].split(","))
                U.abs(U.get("rs:" + r[0]), U.get("req:" + q), rtr, "SysML", "Satisfy");
        U.log("B5.requirements 完成: 需求 " + REQ.length);
    }

    static void actualResources() {
        Package st = U.pkg("Actual Resources", "Actual Resources Structure");
        Package tx = U.pkg("Actual Resources", "Actual Resources Taxonomy");
        D d = new D("Actual Resources Structure", st, "实际资源部署", 1500);
        D dtx = new D("Actual Resources Structure", tx, "实际资源清单", 1500);
        // {名称前缀, 数量, 类型key, 探测/侦测/打击半径(km)}
        Object[][] spec = {
            {"Radar", 3, "rs:r_radar", 300}, {"ESM", 2, "rs:r_esm", 400}, {"MSL", 2, "rs:r_sam", 260},
            {"CP", 1, "rs:r_cp", 0}, {"Edge", 5, "rs:r_edge", 0}};
        int y = 30;
        for (Object[] sp : spec) {
            int x = 30;
            Class type = U.get((String) sp[2]);
            for (int i = 1; i <= (Integer) sp[1]; i++) {
                InstanceSpecification is = U.put(U.F.createInstanceSpecificationInstance(), st);
                is.setName(sp[0] + "-" + i);
                is.getClassifier().add(type);
                U.apply(is, "UAF", "ActualResource");
                if ((Integer) sp[3] > 0) {
                    Slot sl = U.F.createSlotInstance();
                    sl.setOwner(is);
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString ls = U.F.createLiteralStringInstance();
                    ls.setValue("半径 " + sp[3] + " km（部署坐标待确认）");
                    sl.getValue().add(ls);
                    U.note(st, is, "半径 " + sp[3] + " km；部署坐标待确认（按 500 km × 500 km 区域均匀布设）");
                }
                U.reg("ar:" + sp[0] + i, is);
                U.abs(is, type, U.pkg("Actual Resources", "Actual Resources Taxonomy"), "UAF", null);
                d.at(is, x, y, 150, 60);
                dtx.at(is, x, y, 150, 60);
                x += 180;
            }
            y += 110;
        }
        U.log("B5.actual 完成: 实例 13");
    }

    static void constraints() {
        Package c = U.pkg("Operational", "Operational Constraints");
        D dcons = new D("Operational Constraints", c, "作战约束", 1200);
        java.util.List<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element> crow = new java.util.ArrayList<>();
        String[][] cons = {
            {"OA3~OA8 链路总时延上限", "T(OA3)+T(OA4)+T(OA5)+T(OA6)+T(OA7)+T(OA8) <= Tmax（示例，Tmax 待论证）"},
            {"目标识别置信度阈值", "识别置信度 >= 阈值 时进入 OA6，否则继续跟踪（阈值待论证）"},
            {"拦截距离", "30 km <= 目标距离 <= 260 km"}};
        for (String[] k : cons) {
            Constraint cn = U.put(U.F.createConstraintInstance(), c);
            cn.setName(k[0]);
            OpaqueExpression ex = U.F.createOpaqueExpressionInstance();
            ex.getLanguage().add("Natural");
            ex.getBody().add(k[1]);
            cn.setSpecification(ex);
            cn.getConstrainedElement().add(k[0].startsWith("OA3") ? U.get("oa:OA3") : k[0].startsWith("目标") ? U.get("oa:OA5") : U.get("oa:OA10"));
            U.apply(cn, "UAF", "OperationalConstraint");
            crow.add(cn);
        }
        U.tableRows(dcons.diagram, crow);
    }

    static void states() {
        Package p = U.pkg("Operational", "Operational States");
        StateMachine sm = U.put(U.F.createStateMachineInstance(), p);
        sm.setName("目标状态");
        U.apply(sm, "UAF", "OperationalStateDescription");
        Region rg = U.F.createRegionInstance();
        rg.setStateMachine(sm);
        Pseudostate init = U.F.createPseudostateInstance();
        init.setKind(PseudostateKindEnum.INITIAL);
        init.setContainer(rg);
        String[] names = {"未发现", "已发现", "初判敌情", "跟踪监视", "已识别", "被打击", "已击毁"};
        Map<String, State> st = new LinkedHashMap<>();
        for (String n : names) {
            State s = U.F.createStateInstance();
            s.setName(n);
            s.setContainer(rg);
            st.put(n, s);
        }
        FinalState fin = U.F.createFinalStateInstance();
        fin.setContainer(rg);
        tr(rg, init, st.get("未发现"), null);
        String[][] t = {{"未发现", "已发现", "雷达/ESM 发现"}, {"已发现", "初判敌情", "边缘处理初判"}, {"初判敌情", "跟踪监视", "上报航迹"},
            {"跟踪监视", "已识别", "置信度达阈值"}, {"已识别", "被打击", "下达打击任务"}, {"被打击", "已击毁", "已毁伤"},
            {"被打击", "跟踪监视", "未毁伤返回跟踪"}};
        for (String[] x : t) tr(rg, st.get(x[0]), st.get(x[1]), x[2]);
        tr(rg, st.get("已击毁"), fin, null);
        D d = new D("Operational States", p, "目标状态", 1500);
        d.dpe.getDiagram().setOwner(sm);
        int x = 30;
        d.at(init, x, 120, 30, 30);
        x += 80;
        for (String n : names) {
            d.at(st.get(n), x, 100, 130, 60);
            x += 170;
        }
        d.at(fin, x, 120, 30, 30);
        for (Object o : new ArrayList<>(rg.getTransition())) {
            Transition tt = (Transition) o;
            d.link(tt, tt.getSource(), tt.getTarget());
        }
        U.log("B5.states 完成");
    }

    static Transition tr(Region rg, Vertex s, Vertex t, String name) {
        Transition tt = U.F.createTransitionInstance();
        tt.setSource(s);
        tt.setTarget(t);
        tt.setContainer(rg);
        if (name != null) tt.setName(name);
        return tt;
    }

    static void sequences() {
        Package p = U.pkg("Operational", "Operational Sequences");
        Interaction in = U.put(U.F.createInteractionInstance(), p);
        in.setName("区域防空作战时序");
        D d = new D("Operational Sequences", p, "区域防空作战时序", 2000);
        d.dpe.getDiagram().setOwner(in);
        String[] keys = {"enemy", "radar", "edge", "cp", "cmdr", "sam"};
        Map<String, Lifeline> ll = new LinkedHashMap<>();
        Map<String, ShapeElement> sh = new LinkedHashMap<>();
        int x = 30;
        for (String k : keys) {
            Lifeline l = U.F.createLifelineInstance();
            l.setName(((Class) U.get("op:" + k)).getName());
            l.setOwner(in);
            ShapeElement s;
            try {
                s = U.V.createShapeElement(l, d.dpe);
                U.V.reshapeShapeElement(s, new Rectangle(x, 30, 140, 400));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ll.put(k, l);
            sh.put(k, s);
            x += 220;
        }
        String[][] m = {
            {"enemy", "radar", "进入探测区（发现）"}, {"radar", "edge", "点迹/侦察报告"}, {"edge", "cp", "融合航迹、识别结果"},
            {"cp", "cmdr", "态势、威胁判断、作战方案"}, {"cmdr", "cp", "决策结果"}, {"cp", "sam", "打击任务、目标指示"},
            {"radar", "sam", "持续引导数据"}, {"sam", "enemy", "拦截、击毁"}, {"radar", "cp", "毁伤评估结果"}};
        Message prev = null;
        for (String[] e : m) {
            Message msg = U.F.createMessageInstance();
            msg.setName(e[2]);
            try {
                U.V.createSequenceMessage(msg, MessageSortEnum.SYNCHCALL, sh.get(e[0]), sh.get(e[1]), false, 0, prev, 25);
            } catch (Exception ex) {
                U.log("警告: 消息创建失败 " + e[2] + " : " + ex);
            }
            prev = msg;
        }
        U.log("B5.sequences 完成");
    }
}
