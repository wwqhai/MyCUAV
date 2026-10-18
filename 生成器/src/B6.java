import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.*;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import java.util.*;

/**
 * 第六部分：任务工程(ME)链条 —— OA→MT 追溯(D2)、MET 资源流程(D4)、E2E 连接图(D5)、度量(D6)、度量追溯(D7)。
 * 依赖 B1(MT/目标)、B3(作战活动)、B4(资源、资源交换)，必须在它们之后运行。
 */
public class B6 {
    /** 任务线程 → 作战活动 (D2)：每个活动归入一个 MT */
    static final String[][] MT_OA = {
        {"MT1", "OA1,OA2,OA5,OA14,OA16"}, {"MT2", "OA3,OA4,OA6,OA7,OA8"}, {"MT3", "OA9,OA10,OA15"},
        {"MT4", "OA11,OA12,OA13,OA18,OA19"}, {"MT5", "OA17"}};

    /** MOE：编号, 名称, 目标值, 目标 */
    static final String[][] MOE = {
        {"MOE1", "首次探测到生成告警的时间", "≤ 30 s", "G1"}, {"MOE2", "目标类型识别正确率", "≥ 90%", "G2"},
        {"MOE3", "对确认威胁的拦截成功率", "≥ 85%", "G3"}, {"MOE4", "拦截导致的非目标损失事件数", "= 0", "G3"}};

    /** MOP：编号, 名称, 起止, 关联活动, MT, 主要资源, 目标值, 上层 MOE */
    static final String[][] MOP = {
        {"MOP1", "起批延迟", "目标进入范围 → 起批", "OA1,OA2,OA14", "MT1", "RS1、RS2、RS4", "待定", "MOE1"},
        {"MOP2", "融合判别时延", "收到探测数据 → 初判结论", "OA3,OA4,OA8", "MT2", "RS5", "待定", "MOE1"},
        {"MOP3", "航迹上报时延", "初判 → 指挥所收到", "OA6", "MT2", "RS5、RS9", "待定", "MOE1"},
        {"MOP4", "光电识别时延", "收到引导 → 输出识别结果", "OA7", "MT2", "RS3", "待定", "MOE2"},
        {"MOP5", "决策时延", "收到上报 → 决策指令", "OA9,OA10", "MT3", "RS7、RS8", "待定", ""},
        {"MOP6", "任务下发与部署时延", "决策指令 → 发射枪就绪", "OA11,OA12", "MT4", "RS7、RS6", "待定", "MOE3"},
        {"MOP7", "发射到捕获时间", "发射 → 捕获结果", "OA13", "MT4", "RS6", "待定", "MOE3"},
        {"MOP8", "效果评估时延", "捕获结果 → 评估结论", "OA17", "MT5", "RS7", "待定", ""},
        {"MOP9", "干扰启动时延", "收到干扰任务 → 干扰生效", "OA18", "MT4", "RS7、RS10", "含 P_JAM_T，取值待定", "MOE3"},
        {"MOP10", "拦截无人机出动与接近时间", "收到拦截任务 → 到达拦截位置", "OA19", "MT4", "RS7、RS11",
            "约为 P_INT_T + 距离 ÷ P_INT_V，取值待定", "MOE3"}};

    /** TPM：编号, 名称, 值, 资源 key, 上层(MOP 或 MOE) */
    static final String[][] TPM = {
        {"TPM1", "最大探测距离", "15 km", "r_radar", "MOP1"}, {"TPM2", "最大探测距离", "25 km", "r_esm", "MOP1"},
        {"TPM3", "识别距离", "6 km", "r_eo", "MOP1"}, {"TPM4", "识别距离", "2 km", "r_aco", "MOP1"},
        {"TPM5", "捕获网射界", "200 m", "r_gun", "MOP7"}, {"TPM6", "近炸弹射界", "1000 m", "r_gun", "MOP7"},
        {"TPM7", "单次捕获概率", "待定", "r_gun", "MOE3"}, {"TPM8", "识别置信度阈值", "待定", "r_edge", "MOP2"},
        {"TPM9", "干扰作用距离", "P_JAM_R", "r_jam", "MOP9"}, {"TPM10", "干扰启动时延", "P_JAM_T", "r_jam", "MOP9"},
        {"TPM11", "干扰效果判定时限", "P_JAM_W", "r_jam", "MOP9"}, {"TPM12", "干扰有效率", "P_JAM_P", "r_jam", "MOE3"},
        {"TPM13", "拦截无人机飞行速度", "P_INT_V", "r_uav", "MOP10"}, {"TPM14", "拦截无人机续航时间", "P_INT_E", "r_uav", "MOP10"},
        {"TPM15", "拦截无人机作用半径", "P_INT_R", "r_uav", "MOP10"}, {"TPM16", "拦截无人机起飞准备时间", "P_INT_T", "r_uav", "MOP10"},
        {"TPM17", "单次拦截概率", "P_INT_P", "r_uav", "MOE3"}};

    static void run() {
        mtTrace();
        measurements();
        metFlows();
        e2e();
    }

    static com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction trace(Element from, Element to, Package owner) {
        return U.abs(from, to, owner, "UML Standard Profile", "Trace");
    }

    // ------------------------------------------------------------------ D2 / D7：OA → MT
    static void mtTrace() {
        Package otr = U.pkg("Operational", "Operational Traceability");
        int n = 0;
        for (String[] m : MT_OA)
            for (String oa : m[1].split(",")) {
                trace(U.get("oa:" + oa), U.get("mt:" + m[0]), otr);
                n++;
            }
        // 依赖矩阵：行 = 作战活动，列 = 任务线程，准则 = Trace(由 PostMatrix 按名称重配)
        new D("Operational Activities to Capabilities Mapping Matrix", otr, "作战活动与任务线程追溯", 1500);
        U.log("B6 OA→MT 追溯: " + n + " 条");
    }

    // ------------------------------------------------------------------ D6 / D7：度量
    static DataType measureSet(Package pkg, String name) {
        DataType dt = U.put(U.F.createDataTypeInstance(), pkg);
        dt.setName(name);
        U.apply(dt, "UAF", "MeasurementSet");
        return dt;
    }

    static Property measure(Package pkg, DataType set, String key, String name, String value, String desc) {
        Property p = U.F.createPropertyInstance();
        p.setName(key + " " + name);
        com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString ls = U.F.createLiteralStringInstance();
        ls.setValue(value);
        p.setDefaultValue(ls);
        set.getOwnedAttribute().add(p);
        U.apply(p, "UAF", "Measurement");
        U.reg("ms:" + key, p);
        if (desc != null) U.note(pkg, p, desc);
        return p;
    }

    static void measurements() {
        Package ps = U.pkg("Strategy", "Strategic Parameters");
        Package po = U.pkg("Operational", "Operational Parameters");
        Package pr = U.pkg("Resources", "Resources Parameters");
        DataType sMoe = measureSet(ps, "MOE 场景层度量集");
        DataType sMop = measureSet(po, "MOP 任务层度量集");
        DataType sTpm = measureSet(pr, "TPM 资源层度量集");
        int n = 0;
        for (String[] m : MOE) {
            measure(ps, sMoe, m[0], m[1], m[2], "追溯到场景目标 " + m[3]);
            trace(U.get("ms:" + m[0]), U.get("goal:" + m[3]), ps);
            n++;
        }
        for (String[] m : MOP) {
            measure(po, sMop, m[0], m[1], m[6], "起止：" + m[2] + "；任务线程：" + m[4] + "；主要资源：" + m[5]);
            for (String oa : m[3].split(",")) trace(U.get("ms:" + m[0]), U.get("oa:" + oa), po);
            if (!m[7].isEmpty()) trace(U.get("ms:" + m[0]), U.get("ms:" + m[7]), po);
            n++;
        }
        for (String[] t : TPM) {
            measure(pr, sTpm, t[0], t[1], t[2], "资源：" + ((Class) U.get("rs:" + t[3])).getName() + "；影响：" + t[4]);
            trace(U.get("ms:" + t[0]), U.get("rs:" + t[3]), pr);
            trace(U.get("ms:" + t[0]), U.get("ms:" + t[4]), pr);
            n++;
        }
        // Pm-Me 表(典型度量)：行 = 度量集，范围 = 整个模型
        Object[][] tabs = {{ps, "场景层度量(MOE)", sMoe}, {po, "任务层度量(MOP)", sMop}, {pr, "资源层度量(TPM)", sTpm}};
        for (Object[] t : tabs) {
            D d = new D("Typical Measurements Table", (Package) t[0], (String) t[1], 1400);
            U.tableRows(d.diagram, Collections.singletonList((Element) t[2]));
            U.tableTag(d.diagram, "scope", U.P.getPrimaryModel());
        }
        // 依赖矩阵(由 PostMatrix 按名称重配)：度量 × 目标，度量 × 作战活动，度量 × 资源
        new D("Actual Strategic Phases to Capabilities Mapping Matrix", U.pkg("Strategy", "Strategic Traceability"), "度量与目标追溯", 1500);
        new D("Operational Activities to Capabilities Mapping Matrix", U.pkg("Operational", "Operational Traceability"), "度量与作战活动追溯", 1500);
        new D("Resources to Operational Activities Mapping Matrix", U.pkg("Resources", "Resources Traceability"), "度量与资源追溯", 1500);
        U.log("B6 度量: " + n + " 个(MOE " + MOE.length + "，MOP " + MOP.length + "，TPM " + TPM.length + ")");
    }

    // ------------------------------------------------------------------ D4：MET 资源流程
    static final int X0 = 150, STEP = 165, LANE_H = 130;

    /** 节点：id, 类型(init/fin/fork/join/dec/mrg/fn), 引用(判断文本或功能名), 泳道(资源 key), 列 */
    static String[] nd(String id, String type, String ref, String lane, int col) {
        return new String[]{id, type, ref, lane, String.valueOf(col)};
    }

    /** 升级链子图(A14)：from → 依赖射频链路? → 干扰机 → 干扰奏效? → 拦截无人机 → 拦截成功? → 发射枪 → 合并 ms。返回下一列。 */
    static int escalation(List<String[]> nodes, List<String[]> edges, String from, int col, String c2) {
        nodes.add(nd("dx", "dec", "目标依赖射频链路?", c2, col));
        String[] jam = {"任务接收", "信号分析与干扰样式选择", "射频干扰发射", "干扰效果监测"};
        String[] uav = {"任务接收与起飞", "飞向目标与末端制导", "拦截执行", "状态回传"};
        String[] gun = {"任务接收与自动部署", "瞄准解算与发射"};
        int c = col + 1;
        String prev = "dx";
        for (int i = 0; i < jam.length; i++) {
            nodes.add(nd("j" + i, "fn", jam[i], "r_jam", c++));
            edges.add(new String[]{prev, "j" + i, i == 0 ? "是：先干扰" : null});
            prev = "j" + i;
        }
        nodes.add(nd("dy", "dec", "干扰奏效?", c2, c++));
        edges.add(new String[]{prev, "dy", null});
        nodes.add(nd("mi", "mrg", "", "r_uav", c++));
        edges.add(new String[]{"dx", "mi", "否：不受干扰"});
        edges.add(new String[]{"dy", "mi", "否：超过 P_JAM_W 未奏效"});
        prev = "mi";
        for (int i = 0; i < uav.length; i++) {
            nodes.add(nd("u" + i, "fn", uav[i], "r_uav", c++));
            edges.add(new String[]{prev, "u" + i, null});
            prev = "u" + i;
        }
        nodes.add(nd("dz", "dec", "拦截成功?", c2, c++));
        edges.add(new String[]{prev, "dz", null});
        nodes.add(nd("mg", "mrg", "", "r_gun", c++));
        edges.add(new String[]{"dz", "mg", "否：升级发射枪"});
        prev = "mg";
        for (int i = 0; i < gun.length; i++) {
            nodes.add(nd("g" + i, "fn", gun[i], "r_gun", c++));
            edges.add(new String[]{prev, "g" + i, null});
            prev = "g" + i;
        }
        nodes.add(nd("ms", "mrg", "", c2, c++));
        edges.add(new String[]{prev, "ms", null});
        edges.add(new String[]{"dy", "ms", "是"});
        edges.add(new String[]{"dz", "ms", "是"});
        edges.add(new String[]{from, "dx", null});
        return c;
    }

    static void chain(List<String[]> nodes, List<String[]> edges, String prev, String idp, String lane, String[] fns, int col) {
        for (int i = 0; i < fns.length; i++) {
            nodes.add(nd(idp + i, "fn", fns[i], lane, col + i));
            edges.add(new String[]{i == 0 ? prev : idp + (i - 1), idp + i, null});
        }
    }

    static void metFlows() {
        Package pr = U.pkg("Resources", "Resources Processes");
        // ---- MET1-V1：FPV 侦察渗透(想定 V1)
        List<String[]> n = new ArrayList<>();
        List<String[]> e = new ArrayList<>();
        n.add(nd("init", "init", "", "r_radar", 0));
        n.add(nd("f1", "fork", "", "r_radar", 1));
        e.add(new String[]{"init", "f1", null});
        chain(n, e, "f1", "ra", "r_radar", new String[]{"空域搜索", "目标检测", "航迹起始"}, 2);
        chain(n, e, "f1", "es", "r_esm", new String[]{"信号截获", "测向"}, 2);
        n.add(nd("jn", "join", "", "r_edge", 5));
        e.add(new String[]{"ra2", "jn", null});
        e.add(new String[]{"es1", "jn", null});
        chain(n, e, "jn", "ed", "r_edge", new String[]{"本地处理融合", "航迹关联", "目标判别", "航迹上报"}, 6);
        chain(n, e, "ed3", "c1", "r_c2", new String[]{"态势生成显示", "威胁评估"}, 10);
        n.add(nd("m1", "mrg", "", "r_eo", 12));
        e.add(new String[]{"c11", "m1", null});
        chain(n, e, "m1", "eo", "r_eo", new String[]{"目标捕获成像", "类型识别", "光电跟踪"}, 13);
        chain(n, e, "eo2", "ei", "r_edge", new String[]{"类型意图识别"}, 16);
        n.add(nd("d1", "dec", "识别置信度达阈值?", "r_edge", 17));
        e.add(new String[]{"ei0", "d1", null});
        e.add(new String[]{"d1", "m1", "否：继续监视"});
        n.add(nd("pl", "fn", "方案生成", "r_c2", 18));
        e.add(new String[]{"d1", "pl", "是"});
        n.add(nd("dc", "fn", "决策席位支撑", "r_cp", 19));
        e.add(new String[]{"pl", "dc", null});
        n.add(nd("m2", "mrg", "", "r_c2", 20));
        e.add(new String[]{"dc", "m2", null});
        n.add(nd("tk", "fn", "任务分发", "r_c2", 21));
        e.add(new String[]{"m2", "tk", null});
        int c = escalation(n, e, "tk", 22, "r_c2");
        n.add(nd("ev", "fn", "效果评估", "r_c2", c));
        e.add(new String[]{"ms", "ev", null});
        n.add(nd("d2", "dec", "目标已处置?", "r_c2", c + 1));
        e.add(new String[]{"ev", "d2", null});
        e.add(new String[]{"d2", "m2", "否：二次处置"});
        n.add(nd("fin", "fin", "", "r_c2", c + 2));
        e.add(new String[]{"d2", "fin", "是"});
        buildFlow(pr, "MET1-V1 FPV 侦察渗透资源流程",
            new String[]{"r_radar", "r_esm", "r_eo", "r_edge", "r_c2", "r_cp", "r_gun", "r_jam", "r_uav"}, n, e);

        // ---- MET1-V2：巡飞弹(想定 V2)，按单枚目标；预授权，无决策席位
        n = new ArrayList<>();
        e = new ArrayList<>();
        n.add(nd("init", "init", "", "r_esm", 0));
        chain(n, e, "init", "es", "r_esm", new String[]{"信号截获", "测向"}, 1);
        chain(n, e, "es1", "ed", "r_edge", new String[]{"本地处理融合", "目标判别"}, 3);
        n.add(nd("f1", "fork", "", "r_edge", 5));
        e.add(new String[]{"ed1", "f1", null});
        chain(n, e, "f1", "ac", "r_aco", new String[]{"声信号采集", "声源定向", "声纹识别", "速度估计"}, 6);
        chain(n, e, "f1", "eo", "r_eo", new String[]{"目标捕获成像", "类型识别", "光电跟踪"}, 6);
        chain(n, e, "ac3", "up", "r_edge", new String[]{"航迹上报"}, 10);
        chain(n, e, "up0", "c1", "r_c2", new String[]{"态势生成显示", "威胁评估"}, 11);
        chain(n, e, "c11", "ho", "r_edge", new String[]{"跟踪移交"}, 13);
        c = escalation(n, e, "ho0", 14, "r_c2");
        n.add(nd("ev", "fn", "效果评估", "r_c2", c));
        e.add(new String[]{"ms", "ev", null});
        n.add(nd("d2", "dec", "目标已处置?", "r_c2", c + 1));
        e.add(new String[]{"ev", "d2", null});
        e.add(new String[]{"d2", "dx", "否：再次处置"});
        n.add(nd("jn", "join", "", "r_c2", c + 2));
        e.add(new String[]{"d2", "jn", "是"});
        e.add(new String[]{"eo2", "jn", null});
        n.add(nd("fin", "fin", "", "r_c2", c + 3));
        e.add(new String[]{"jn", "fin", null});
        buildFlow(pr, "MET1-V2 巡飞弹资源流程",
            new String[]{"r_esm", "r_aco", "r_eo", "r_edge", "r_c2", "r_gun", "r_jam", "r_uav"}, n, e);
    }

    static Activity funcOf(String rkey, String name) {
        for (String[] r : B4.RS) {
            if (!r[0].equals(rkey)) continue;
            int i = 0;
            for (String fo : r[3].split("\\|")) {
                if (fo.split("=")[0].equals(name)) return U.get("fn:" + rkey + ":" + i);
                i++;
            }
        }
        throw new IllegalStateException("找不到功能 " + rkey + "/" + name);
    }

    static void buildFlow(Package pr, String title, String[] lanes, List<String[]> nodes, List<String[]> edges) {
        Activity flow = U.put(U.F.createActivityInstance(), pr);
        flow.setName(title);
        U.apply(flow, "UAF", "Function");
        Map<String, ActivityPartition> part = new LinkedHashMap<>();
        Map<String, Integer> laneIdx = new HashMap<>();
        for (int i = 0; i < lanes.length; i++) {
            ActivityPartition ap = U.F.createActivityPartitionInstance();
            ap.setName(((Class) U.get("rs:" + lanes[i])).getName());
            ap.setRepresents(U.get("rs:" + lanes[i]));
            ap.setOwner(flow);
            flow.getPartition().add(ap);
            part.put(lanes[i], ap);
            laneIdx.put(lanes[i], i);
        }
        D df = new D("Resources Process Flow", pr, title, 6000);
        df.dpe.getDiagram().setOwner(flow);
        df.dropAuto();
        try {
            U.V.createSwimlane(new ArrayList<>(part.values()), new ArrayList<ActivityPartition>(), df.dpe);
        } catch (Exception ex) {
            U.log("警告: 泳道创建失败 " + ex);
        }
        Map<String, ActivityNode> m = new LinkedHashMap<>();
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
                    Activity fn = funcOf(d[3], d[2]);
                    CallBehaviorAction cba = U.F.createCallBehaviorActionInstance();
                    cba.setName(d[2]);
                    cba.setBehavior(fn);
                    cba.setActivity(flow);
                    U.apply(cba, "UAF", "FunctionAction");
                    part.get(d[3]).getNode().add(cba);
                    cba.getInPartition().add(part.get(d[3]));
                    an = cba;
                }
            }
            m.put(d[0], an);
            int x = X0 + Integer.parseInt(d[4]) * STEP, y = 120 + laneIdx.get(d[3]) * LANE_H;
            if (d[1].equals("fn")) df.at(an, x, y, 140, 60);
            else df.at(an, x + 50, y + 10, 40, 40);
        }
        for (String[] ed : edges) {
            if (m.get(ed[0]) == null || m.get(ed[1]) == null)
                throw new IllegalStateException("流程边的端点不存在: " + ed[0] + "→" + ed[1]);
            df.link(B3.flow(flow, m.get(ed[0]), m.get(ed[1]), ed[2]), m.get(ed[0]), m.get(ed[1]));
        }
        U.log("B6 资源流程 " + title + ": 节点 " + nodes.size() + "，控制流 " + edges.size());
    }

    // ------------------------------------------------------------------ D5：E2E 连接图
    static void e2e() {
        Package cn = U.pkg("Resources", "Resources Connectivity");
        D d = new D("Resources Connectivity", cn, "MET 端到端连接", 2000);
        String[] stage = {"探测", "识别融合", "决策", "交战(干扰机 → 拦截无人机 → 发射枪)", "评估(回送指挥系统)"};
        int[] sx = {30, 400, 770, 1140, 1510};
        for (int i = 0; i < stage.length; i++) d.text(stage[i], sx[i], 10, 330, 30);
        Object[][] pos = {{"r_radar", 30, 60}, {"r_esm", 30, 170}, {"r_eo", 30, 280}, {"r_aco", 30, 390},
            {"r_edge", 400, 225}, {"r_c2", 770, 225}, {"r_cp", 770, 420}, {"r_net", 400, 480},
            {"r_jam", 1140, 60}, {"r_uav", 1140, 225}, {"r_gun", 1140, 390}};
        for (Object[] p : pos) d.at(U.get("rs:" + p[0]), (Integer) p[1], (Integer) p[2], 220, 70);
        int n = 0;
        for (String[] f : B4.RS_FLOW) {
            d.link(U.get("rf:" + f[0]), U.get("rs:" + f[1]), U.get("rs:" + f[2]));
            n++;
        }
        U.log("B6 E2E 连接图: 资源 " + pos.length + "，接口 " + n + " 条");
    }
}
