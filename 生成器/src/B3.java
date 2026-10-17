import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.*;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.*;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import java.util.*;

/** 第三部分：作战活动(Op-Pr)与活动到能力、执行者的追溯(Op-Tr)。 */
public class B3 {
    /** 活动：key, 名称, 执行者key, 场景步骤, 输出 */
    static final String[][] OA = {
        {"OA1", "低空搜索探测", "radar", "1", "点迹、航迹(IE2)"},
        {"OA2", "无源电磁侦察", "esm", "1/7", "信号特征、方位线(IE3)"},
        {"OA3", "多源数据融合", "edge", "2/3/8", "融合航迹(IE6)"},
        {"OA4", "目标初步判别", "edge", "2/8", "初判结论"},
        {"OA5", "持续跟踪探测", "radar", "2/6", "持续航迹(IE2、IE3)；雷达、电侦各一条泳道"},
        {"OA6", "航迹上报", "edge", "2/9", "融合航迹、目标属性(IE7)"},
        {"OA7", "光电识别与跟踪", "eo", "3/12", "识别结果、跟踪航迹(IE4)"},
        {"OA8", "目标类型与意图识别", "edge", "3", "4 旋翼 FPV、侦察意图(IE7)"},
        {"OA9", "生成作战方案", "cp", "4", "作战方案建议(IE9)"},
        {"OA10", "作战决策", "cmdr", "4", "决策指令(IE10)"},
        {"OA11", "下达并传递打击任务", "cp", "4/5", "打击任务(IE11)"},
        {"OA12", "自动部署发射枪", "sol", "5/11", "部署状态(IE12)"},
        {"OA13", "发射拦截(捕获网/近炸弹)", "sol", "5/11", "发射报告、捕获结果(IE12、IE13)"},
        {"OA14", "声学探测识别", "aco", "9", "方位角、速度(IE5)"},
        {"OA15", "威胁评估与态势显示", "cp", "10/12", "威胁等级、全流程态势(IE9)"},
        {"OA16", "跟踪任务移交(接续)", "edge", "11", "移交指令(IE6、IE8)"},
        {"OA17", "拦截效果评估(补充)", "cp", "补充", "评估结论，触发二次打击或结束"}};

    /** 活动 → 能力 (MapsToCapability) */
    static final String[][] MAP = {
        {"OA1", "C1"}, {"OA2", "C2"}, {"OA3", "C5"}, {"OA4", "C5"}, {"OA5", "C1"}, {"OA5", "C2"}, {"OA5", "C6"},
        {"OA6", "C7"}, {"OA7", "C3"}, {"OA8", "C5"}, {"OA9", "C8"}, {"OA10", "C8"}, {"OA11", "C7"},
        {"OA12", "C9"}, {"OA13", "C9"}, {"OA14", "C4"}, {"OA15", "C7"}, {"OA16", "C6"}, {"OA17", "C7"}};

    /** 流程 A(FPV)的节点：id, 类型, 引用(OA key 或标签), 泳道, 列 */
    static final String[][] NODES_A = {
        {"init", "init", "", "radar", "0"}, {"f1", "fork", "", "radar", "1"},
        {"OA1", "oa", "OA1", "radar", "2"}, {"OA2", "oa", "OA2", "esm", "2"}, {"j1", "join", "", "edge", "3"},
        {"OA3", "oa", "OA3", "edge", "4"}, {"OA4", "oa", "OA4", "edge", "5"}, {"OA6", "oa", "OA6", "edge", "6"},
        {"f2", "fork", "", "edge", "7"},
        {"OA5r", "oa", "OA5", "radar", "8"}, {"OA5e", "oa", "OA5", "esm", "8"}, {"OA15", "oa", "OA15", "cp", "8"},
        {"m1", "mrg", "", "eo", "8"}, {"OA7", "oa", "OA7", "eo", "9"}, {"OA8", "oa", "OA8", "edge", "10"},
        {"d1", "dec", "识别置信度达阈值?", "edge", "11"},
        {"OA9", "oa", "OA9", "cp", "12"}, {"OA10", "oa", "OA10", "cmdr", "13"}, {"m2", "mrg", "", "cp", "13"},
        {"OA11", "oa", "OA11", "cp", "14"}, {"OA12", "oa", "OA12", "sol", "15"}, {"OA13", "oa", "OA13", "sol", "16"},
        {"OA17", "oa", "OA17", "cp", "17"}, {"d2", "dec", "已捕获?", "cp", "18"},
        {"j3", "join", "", "cp", "19"}, {"fin", "fin", "", "cp", "20"}};

    /** 流程 A 的控制流：源, 目标, 守卫 */
    static final String[][] EDGES_A = {
        {"init", "f1", null}, {"f1", "OA1", null}, {"f1", "OA2", null}, {"OA1", "j1", null}, {"OA2", "j1", null},
        {"j1", "OA3", null}, {"OA3", "OA4", null}, {"OA4", "OA6", null}, {"OA6", "f2", null},
        {"f2", "OA5r", null}, {"f2", "OA5e", null}, {"f2", "OA15", null}, {"f2", "m1", null},
        {"m1", "OA7", null}, {"OA7", "OA8", null}, {"OA8", "d1", null},
        {"d1", "m1", "否：继续监视"}, {"d1", "OA9", "是"}, {"OA9", "OA10", null}, {"OA10", "m2", null},
        {"m2", "OA11", null}, {"OA11", "OA12", null}, {"OA12", "OA13", null}, {"OA13", "OA17", null},
        {"OA17", "d2", null}, {"d2", "m2", "否：二次打击"}, {"d2", "j3", "是"},
        {"OA5r", "j3", null}, {"OA5e", "j3", null}, {"OA15", "j3", null}, {"j3", "fin", null}};

    /** 流程 B(巡飞弹)的节点 */
    static final String[][] NODES_B = {
        {"init", "init", "", "esm", "0"}, {"OA2", "oa", "OA2", "esm", "1"},
        {"OA3", "oa", "OA3", "edge", "2"}, {"OA4", "oa", "OA4", "edge", "3"}, {"f1", "fork", "", "edge", "4"},
        {"OA14", "oa", "OA14", "aco", "5"}, {"OA7", "oa", "OA7", "eo", "5"}, {"OA6", "oa", "OA6", "edge", "6"},
        {"OA15", "oa", "OA15", "cp", "7"}, {"OA16", "oa", "OA16", "edge", "8"}, {"m1", "mrg", "", "sol", "8"},
        {"OA12", "oa", "OA12", "sol", "9"}, {"OA13", "oa", "OA13", "sol", "10"}, {"OA17", "oa", "OA17", "cp", "11"},
        {"d1", "dec", "已捕获?", "cp", "12"}, {"j1", "join", "", "cp", "13"}, {"fin", "fin", "", "cp", "14"}};

    static final String[][] EDGES_B = {
        {"init", "OA2", null}, {"OA2", "OA3", null}, {"OA3", "OA4", null}, {"OA4", "f1", null},
        {"f1", "OA14", null}, {"f1", "OA7", null}, {"OA14", "OA6", null}, {"OA6", "OA15", null},
        {"OA15", "OA16", null}, {"OA16", "m1", null}, {"m1", "OA12", null}, {"OA12", "OA13", null},
        {"OA13", "OA17", null}, {"OA17", "d1", null}, {"d1", "m1", "否：再次拦截"}, {"d1", "j1", "是"},
        {"OA7", "j1", null}, {"j1", "fin", null}};

    static final int X0 = 140, STEP = 170, LANE_H = 140;

    static void run() {
        Package pr = U.pkg("Operational", "Operational Processes");
        Package tr = U.pkg("Operational", "Operational Traceability");

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

        buildFlow(pr, "flowA", "FPV 无人机反制作战流程",
            new String[]{"radar", "esm", "eo", "edge", "cp", "cmdr", "sol"}, NODES_A, EDGES_A);
        buildFlow(pr, "flowB", "巡飞弹反制作战流程",
            new String[]{"esm", "eo", "aco", "edge", "cp", "sol"}, NODES_B, EDGES_B);

        // Op-Tr：活动 → 能力 (MapsToCapability)；执行者 IsCapableToPerform 活动
        D dtr = new D("Operational Activities to Capabilities Mapping Matrix", tr, "作战活动与能力映射", 1500);
        for (String[] m : MAP)
            U.abs(U.get("oa:" + m[0]), U.get("cap:" + m[1]), tr, "UAF", "MapsToCapability");
        for (String[] a : OA)
            U.abs(U.get("op:" + a[2]), U.get("oa:" + a[0]), tr, "UAF", "IsCapableToPerform");
        // OA5 在电侦泳道也执行
        U.abs(U.get("op:esm"), U.get("oa:OA5"), tr, "UAF", "IsCapableToPerform");
        U.log("B3 完成: 活动 " + OA.length + "，MapsToCapability " + MAP.length);
    }

    /** 按节点/控制流表创建一张带泳道的活动图。 */
    static void buildFlow(Package pr, String key, String title, String[] lanes, String[][] nodes, String[][] edges) {
        Activity flow = U.put(U.F.createActivityInstance(), pr);
        flow.setName(title);
        U.apply(flow, "UAF", "OperationalActivity");
        U.reg("oa:" + key, flow);

        Map<String, ActivityPartition> part = new LinkedHashMap<>();
        Map<String, Integer> laneIdx = new HashMap<>();
        for (int i = 0; i < lanes.length; i++) {
            ActivityPartition ap = U.F.createActivityPartitionInstance();
            ap.setName(((Class) U.get("op:" + lanes[i])).getName());
            ap.setRepresents(U.get("op:" + lanes[i]));
            ap.setOwner(flow);
            flow.getPartition().add(ap);
            part.put(lanes[i], ap);
            laneIdx.put(lanes[i], i);
        }

        D df = new D("Operational Process Flow", pr, title, 4000);
        df.dpe.getDiagram().setOwner(flow);
        df.dropAuto();
        try {
            U.V.createSwimlane(new ArrayList<>(part.values()), new ArrayList<ActivityPartition>(), df.dpe);
        } catch (Exception e) {
            U.log("警告: 泳道创建失败 " + e);
        }

        Map<String, ActivityNode> n = new LinkedHashMap<>();
        for (String[] d : nodes) {
            ActivityNode an;
            switch (d[1]) {
                case "init": { InitialNode x = U.F.createInitialNodeInstance(); x.setActivity(flow); an = x; break; }
                case "fin": { ActivityFinalNode x = U.F.createActivityFinalNodeInstance(); x.setActivity(flow); an = x; break; }
                case "fork": { ForkNode x = U.F.createForkNodeInstance(); x.setActivity(flow); an = x; break; }
                case "join": { JoinNode x = U.F.createJoinNodeInstance(); x.setActivity(flow); an = x; break; }
                case "dec": { DecisionNode x = U.F.createDecisionNodeInstance(); x.setName(d[2]); x.setActivity(flow); an = x; break; }
                case "mrg": { MergeNode x = U.F.createMergeNodeInstance(); x.setActivity(flow); an = x; break; }
                default: {
                    CallBehaviorAction cba = U.F.createCallBehaviorActionInstance();
                    String[] a = Arrays.stream(OA).filter(r -> r[0].equals(d[2])).findFirst().get();
                    cba.setName(a[0] + " " + a[1]);
                    cba.setBehavior(U.get("oa:" + a[0]));
                    cba.setActivity(flow);
                    U.apply(cba, "UAF", "OperationalActivityAction");
                    part.get(d[3]).getNode().add(cba);
                    cba.getInPartition().add(part.get(d[3]));
                    an = cba;
                }
            }
            n.put(d[0], an);
            int x = X0 + Integer.parseInt(d[4]) * STEP, y = 120 + laneIdx.get(d[3]) * LANE_H;
            if (d[1].equals("oa")) df.at(an, x, y, 140, 60);
            else df.at(an, x + 50, y + 10, 40, 40);
        }
        for (String[] e : edges) {
            ControlFlow cf = flow(flow, n.get(e[0]), n.get(e[1]), e[2]);
            df.link(cf, n.get(e[0]), n.get(e[1]));
        }
        U.log("B3 流程图 " + title + ": 节点 " + nodes.length + "，控制流 " + edges.length);
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

