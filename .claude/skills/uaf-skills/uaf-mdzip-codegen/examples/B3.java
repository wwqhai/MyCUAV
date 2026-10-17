import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.*;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.*;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityGroup;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import java.util.*;

/** 第三部分：作战活动(Op-Pr 过程)与活动到能力的追溯(Op-Tr)。 */
public class B3 {
    /** 活动：key, 名称, 执行者key, 场景步骤, 输出 */
    static final String[][] OA = {
        {"OA1", "空域搜索", "radar", "1", "点迹"},
        {"OA2", "电磁侦察", "esm", "1", "辐射源报告"},
        {"OA3", "多源信息交换与融合", "edge", "2", "初判目标、航迹"},
        {"OA4", "跟踪监视并上报", "radar", "2/6", "持续航迹"},
        {"OA5", "目标识别与意图判断", "edge", "3", "固定翼飞机、侦察/打击意图"},
        {"OA6", "威胁评估与方案生成", "cp", "4", "作战方案"},
        {"OA7", "方案决策", "cmdr", "4", "决策结果"},
        {"OA8", "打击任务下达", "cp", "5", "打击任务"},
        {"OA9", "发射与制导", "sam", "5", "导弹飞行、持续引导"},
        {"OA10", "拦截与击毁", "sam", "7", "目标毁伤"},
        {"OA11", "毁伤评估与上报", "radar", "8", "评估结果"},
        {"OA12", "全流程态势显示", "cp", "8", "态势图"}};

    /** 活动 -> 能力 (MapsToCapability) */
    static final String[][] MAP = {
        {"OA1", "C1"}, {"OA2", "C2"}, {"OA3", "C3"}, {"OA4", "C1"}, {"OA4", "C2"}, {"OA5", "C4"},
        {"OA6", "C5"}, {"OA7", "C5"}, {"OA8", "C5"}, {"OA9", "C6"}, {"OA10", "C6"}, {"OA11", "C7"}, {"OA12", "C8"}};

    static void run() {
        Package pr = U.pkg("Operational", "Operational Processes");
        Package tr = U.pkg("Operational", "Operational Traceability");
        Package op = U.pkg("Operational", "Operational Structure");

        // Op-Pr 过程(分类视图)：列出全部作战活动
        D dpr = new D("Operational Processes", pr, "作战活动", 1500);
        for (String[] a : OA) {
            Activity act = U.put(U.F.createActivityInstance(), pr);
            act.setName(a[0] + " " + a[1]);
            U.apply(act, "UAF", "OperationalActivity");
            U.reg("oa:" + a[0], act);
            U.note(pr, act, "场景步骤 " + a[3] + "；输出：" + a[4]);
            dpr.add(act, 200, 60);
        }

        // 顶层场景活动：包含泳道 + 动作节点 + 控制流
        Activity flow = U.put(U.F.createActivityInstance(), pr);
        flow.setName("区域防空作战流程");
        U.apply(flow, "UAF", "OperationalActivity");
        U.reg("oa:flow", flow);

        String[] lanes = {"radar", "esm", "edge", "cp", "cmdr", "sam"};
        Map<String, ActivityPartition> part = new LinkedHashMap<>();
        for (String l : lanes) {
            ActivityPartition ap = U.F.createActivityPartitionInstance();
            ap.setName(((Class) U.get("op:" + l)).getName());
            ap.setRepresents(U.get("op:" + l));
            ap.setOwner(flow);
            flow.getPartition().add(ap);
            part.put(l, ap);
        }

        D df = new D("Operational Process Flow", pr, "区域防空作战流程", 2000);
        df.dpe.getDiagram().setOwner(flow);
        try {
            U.V.createSwimlane(new ArrayList<>(part.values()), new ArrayList<ActivityPartition>(), df.dpe);
        } catch (Exception e) {
            U.log("警告: 泳道创建失败 " + e);
        }

        InitialNode init = U.F.createInitialNodeInstance();
        init.setActivity(flow);
        ActivityFinalNode fin = U.F.createActivityFinalNodeInstance();
        fin.setActivity(flow);
        ForkNode fork = U.F.createForkNodeInstance();
        fork.setActivity(flow);
        JoinNode join = U.F.createJoinNodeInstance();
        join.setActivity(flow);
        DecisionNode dIdent = U.F.createDecisionNodeInstance();
        dIdent.setName("识别置信度达阈值?");
        dIdent.setActivity(flow);
        DecisionNode dKill = U.F.createDecisionNodeInstance();
        dKill.setName("已毁伤?");
        dKill.setActivity(flow);
        MergeNode mIdent = U.F.createMergeNodeInstance();
        mIdent.setActivity(flow);
        MergeNode mPlan = U.F.createMergeNodeInstance();
        mPlan.setActivity(flow);

        Map<String, CallBehaviorAction> act = new LinkedHashMap<>();
        for (String[] a : OA) {
            CallBehaviorAction cba = U.F.createCallBehaviorActionInstance();
            cba.setName(a[0] + " " + a[1]);
            cba.setBehavior(U.get("oa:" + a[0]));
            cba.setActivity(flow);
            U.apply(cba, "UAF", "OperationalActivityAction");
            if (part.containsKey(a[2])) {
                part.get(a[2]).getNode().add(cba);
                cba.getInPartition().add(part.get(a[2]));
            }
            act.put(a[0], cba);
        }

        // 控制流：初始 → (OA1 ∥ OA2) → OA3 → OA4 → 合并 → OA5 → 判断 → OA6 → OA7 → OA8 → OA9 → OA10 → OA11 → 判断 → 结束；OA12 并行
        flow(flow, init, fork, null);
        flow(flow, fork, act.get("OA1"), null);
        flow(flow, fork, act.get("OA2"), null);
        flow(flow, act.get("OA1"), join, null);
        flow(flow, act.get("OA2"), join, null);
        flow(flow, join, act.get("OA3"), null);
        flow(flow, act.get("OA3"), act.get("OA4"), null);
        flow(flow, act.get("OA4"), mIdent, null);
        flow(flow, mIdent, act.get("OA5"), null);
        flow(flow, act.get("OA5"), dIdent, null);
        flow(flow, dIdent, mIdent, "否：继续跟踪");
        flow(flow, dIdent, mPlan, "是");
        flow(flow, mPlan, act.get("OA6"), null);
        flow(flow, act.get("OA6"), act.get("OA7"), null);
        flow(flow, act.get("OA7"), act.get("OA8"), null);
        flow(flow, act.get("OA8"), act.get("OA9"), null);
        flow(flow, act.get("OA9"), act.get("OA10"), null);
        flow(flow, act.get("OA10"), act.get("OA11"), null);
        flow(flow, act.get("OA11"), dKill, null);
        flow(flow, dKill, mPlan, "否：二次打击");
        flow(flow, dKill, act.get("OA12"), "是");
        flow(flow, act.get("OA12"), fin, null);

        // 放置到图中：泳道内按顺序排布
        int[] laneY = new int[lanes.length];
        Map<String, Integer> laneIdx = new HashMap<>();
        for (int i = 0; i < lanes.length; i++) laneIdx.put(lanes[i], i);
        int x0 = 140, step = 190;
        int col = 0;
        place(df, init, 40, 20);
        place(df, fork, 90, 20);
        String[] seq = {"OA1", "OA2", "OA3", "OA4", "OA5", "OA6", "OA7", "OA8", "OA9", "OA10", "OA11", "OA12"};
        int[] colOf = {1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        for (int i = 0; i < seq.length; i++) {
            String[] a = Arrays.stream(OA).filter(r -> r[0].equals(seq[0])).findFirst().get();
        }
        for (int i = 0; i < OA.length; i++) {
            String[] a = OA[i];
            int lane = laneIdx.get(a[2]);
            df.at(act.get(a[0]), x0 + colOf[i] * step, 120 + lane * 140, 170, 60);
        }
        place(df, join, x0 + 2 * step - 60, 120 + 4 * 140);
        place(df, mIdent, x0 + 4 * step - 60, 120 + 2 * 140);
        place(df, dIdent, x0 + 4 * step + 60, 120 + 3 * 140);
        place(df, mPlan, x0 + 5 * step - 60, 120 + 3 * 140);
        place(df, dKill, x0 + 10 * step, 120 + 3 * 140);
        place(df, fin, x0 + 12 * step, 120 + 3 * 140);
        for (ActivityEdge e : new ArrayList<>(flow.getEdge())) {
            ShapeElement a = df.shapes.get(e.getSource()), b = df.shapes.get(e.getTarget());
            if (a != null && b != null) df.link(e, e.getSource(), e.getTarget());
        }

        // Op-Tr：活动 -> 能力 (MapsToCapability)
        D dtr = new D("Operational Activities to Capabilities Mapping Matrix", tr, "作战活动与能力映射", 1500);
        for (String[] m : MAP)
            U.abs(U.get("oa:" + m[0]), U.get("cap:" + m[1]), tr, "UAF", "MapsToCapability");
        // 执行者 IsCapableToPerform 活动
        for (String[] a : OA)
            U.abs(U.get("op:" + a[2]), U.get("oa:" + a[0]), tr, "UAF", "IsCapableToPerform");
        U.log("B3 完成: 活动 " + OA.length + "，MapsToCapability " + MAP.length);
    }

    static void place(D d, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element e, int x, int y) {
        d.at(e, x, y, 40, 40);
    }

    static ControlFlow flow(Activity owner, ActivityNode s, ActivityNode t, String guard) {
        ControlFlow cf = U.F.createControlFlowInstance();
        cf.setSource(s);
        cf.setTarget(t);
        cf.setActivity(owner);
        if (guard != null) {
            com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString ls = U.F.createLiteralStringInstance();
            ls.setValue(guard);
            cf.setGuard(ls);
        }
        return cf;
    }
}
