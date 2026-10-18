import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
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
        {"R1", "系统应对低空小型目标在最大 15 km 范围内探测并起批航迹", "C1", "OA1"},
        {"R2", "系统应在最大 25 km 范围内侦测目标无线电信号并给出方位", "C2", "OA2"},
        {"R3", "系统应在 6 km 范围内对目标进行光电识别与跟踪", "C3", "OA7"},
        {"R4", "系统应在 2 km 范围内对声源定向并识别", "C4", "OA14"},
        {"R5", "边缘盒应实现本地处理、多源融合和探测指挥任务移交(接续)", "C5,C6", "OA3,OA4,OA16"},
        {"R6", "系统应识别目标类型和意图", "C5", "OA8"},
        {"R7", "系统应将航迹与目标属性上报指挥所", "C7", "OA6"},
        {"R8", "指挥系统应生成作战方案，由指挥员决策", "C8", "OA9,OA10"},
        {"R9", "指挥所应将打击任务传递至单兵，发射枪应自动部署", "C9", "OA11,OA12"},
        {"R10", "发射枪应具备 200 m 捕获网和 1000 m 近炸弹射界", "C9", "OA13"},
        {"R11", "雷达、电侦应对敌机持续跟踪探测", "C6", "OA5"},
        {"R12", "雷达无法探测时，应由电侦和声学补盲并融合判别", "C2,C4,C5", "OA2,OA14,OA3"},
        {"R13", "指挥所应显示全流程态势、报告威胁等级并评估拦截效果", "C7", "OA15,OA17"},
        {"R14", "各设备间应提供数据通信(假设 A9)", "C7", "OA6"},
        {"R15", "系统应对依赖射频链路的目标实施干扰或驱离", "C10", "OA18"},
        {"R16", "系统应能以拦截无人机对来袭目标实施空中拦截", "C11", "OA19"}};

    /** 资源满足需求 */
    static final String[][] RS_REQ = {
        {"r_radar", "R1,R11"}, {"r_esm", "R2,R11,R12"}, {"r_eo", "R3"}, {"r_aco", "R4,R12"},
        {"r_edge", "R5,R6,R7,R12"}, {"r_c2", "R8,R9,R13"}, {"r_cp", "R8"}, {"r_gun", "R9,R10"}, {"r_net", "R14"}, {"r_jam", "R15"}, {"r_uav", "R16"}};

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
        // 能力 → 需求、活动 → 需求 (Satisfy)；"能力与需求追溯"由 PostMatrix 重配为 能力×需求
        D dst = new D("Actual Strategic Phases to Capabilities Mapping Matrix", str, "能力与需求追溯", 1500);
        for (String[] r : REQ) {
            Class q = U.get("req:" + r[0]);
            for (String c : r[2].split(",")) U.abs(U.get("cap:" + c), q, str, "SysML", "Satisfy");
            for (String oa : r[3].split(",")) U.abs(U.get("oa:" + oa), q, otr, "SysML", "Satisfy");
        }
        for (String[] r : RS_REQ)
            for (String q : r[1].split(","))
                U.abs(U.get("rs:" + r[0]), U.get("req:" + q), rtr, "SysML", "Satisfy");
        U.log("B5.requirements 完成: 需求 " + REQ.length);
    }

    static void actualResources() {
        Package st = U.pkg("Actual Resources", "Actual Resources Structure");
        D d = new D("Actual Resources Structure", st, "实际资源部署", 1500);
        // {名称前缀, 数量, 类型key, 性能说明}
        Object[][] spec = {
            {"Radar", 5, "rs:r_radar", "最大探测距离 15 km"}, {"ESM", 5, "rs:r_esm", "最大探测距离 25 km"},
            {"EO", 5, "rs:r_eo", "识别距离 6 km"}, {"ACO", 10, "rs:r_aco", "识别距离 2 km"},
            {"EDGE", 25, "rs:r_edge", "配装对应：1~5 雷达、6~10 电侦(假设A1)、11~15 光电、16~25 声学"},
            {"GUN", 20, "rs:r_gun", "捕获网射界 200 m；近炸弹射界 1000 m；单兵各携带 1 把"},
            {"C2", 1, "rs:r_c2", "AI 指挥系统"}, {"CP", 1, "rs:r_cp", "指挥所"}, {"NET", 1, "rs:r_net", "抽象通信网络(假设A9)"},
            {"JAM", 5, "rs:r_jam", "参数代号 P_JAM_R、P_JAM_T、P_JAM_W、P_JAM_P，取值待定(A13)"},
            {"INT", 10, "rs:r_uav", "参数代号 P_INT_V、P_INT_E、P_INT_R、P_INT_T、P_INT_P，取值待定(A13)"}};
        int total = 0;
        for (Object[] sp : spec) {
            Class type = U.get((String) sp[2]);
            for (int i = 1; i <= (Integer) sp[1]; i++) {
                InstanceSpecification is = U.put(U.F.createInstanceSpecificationInstance(), st);
                is.setName(sp[0] + "-" + i);
                is.getClassifier().add(type);
                U.apply(is, "UAF", "ActualResource");
                Slot sl = U.F.createSlotInstance();
                sl.setOwner(is);
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString ls = U.F.createLiteralStringInstance();
                ls.setValue(sp[3] + "（部署坐标待确认，假设A2）");
                sl.getValue().add(ls);
                U.reg("ar:" + sp[0] + i, is);
                d.add(is, 120, 50);
                total++;
            }
            d.nextRow();
        }
        U.log("B5.actual 完成: 实例 " + total);
    }

    static void constraints() {
        Package c = U.pkg("Operational", "Operational Constraints");
        D dcons = new D("Operational Constraints", c, "作战约束", 1200);
        List<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element> crow = new ArrayList<>();
        // 名称, 表达式, 被约束活动；时延类取值原文未给出，只写“待确认”不编造数值
        String[][] cons = {
            {"雷达探测距离上限", "最大探测距离 <= 15 km（输入）", "OA1"},
            {"电侦探测距离上限", "最大探测距离 <= 25 km（输入）", "OA2"},
            {"光电识别距离上限", "可见光识别距离 <= 6 km（输入）", "OA7"},
            {"声学识别距离上限", "最大声音识别距离 <= 2 km（输入）", "OA14"},
            {"捕获网射界", "射界 <= 200 m（输入）", "OA13"},
            {"近炸弹射界", "射界 <= 1000 m（输入）", "OA13"},
            {"融合与航迹上报时延上限", "T(OA3)+T(OA4)+T(OA6) <= Tmax（待确认，原文未给出）", "OA3"},
            {"决策与任务下达时延上限", "T(OA9)+T(OA10)+T(OA11) <= Tmax（待确认，原文未给出）", "OA9"},
            {"发射枪自动部署时延上限", "T(OA12) <= Tmax（待确认，原文未给出）", "OA12"},
            {"干扰作用距离 P_JAM_R", "目标距离 <= P_JAM_R（代号，取值待定，A13）", "OA18"},
            {"干扰启动时延 P_JAM_T", "T(OA18) = P_JAM_T（代号，取值待定，A13）", "OA18"},
            {"干扰效果判定时限 P_JAM_W", "干扰超过 P_JAM_W 未奏效则升级为 OA19（A14，取值待定）", "OA18"},
            {"拦截无人机飞行速度 P_INT_V", "飞行速度 = P_INT_V（代号，取值待定，A13）", "OA19"},
            {"拦截无人机续航 P_INT_E", "续航时间 = P_INT_E（代号，取值待定，A13）", "OA19"},
            {"拦截无人机作用半径 P_INT_R", "起降点到拦截点距离 <= P_INT_R（代号，取值待定，A13）", "OA19"},
            {"拦截无人机起飞准备时间 P_INT_T", "收到任务到起飞 = P_INT_T（代号，取值待定，A13）", "OA19"}};
        for (String[] k : cons) {
            Constraint cn = U.put(U.F.createConstraintInstance(), c);
            cn.setName(k[0]);
            OpaqueExpression ex = U.F.createOpaqueExpressionInstance();
            ex.getLanguage().add("Natural");
            ex.getBody().add(k[1]);
            cn.setSpecification(ex);
            cn.getConstrainedElement().add(U.get("oa:" + k[2]));
            U.apply(cn, "UAF", "OperationalConstraint");
            crow.add(cn);
        }
        U.tableRows(dcons.diagram, crow);
        U.log("B5.constraints 完成: " + cons.length);
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
        String[] names = {"未发现", "已探测", "已跟踪", "初判为敌方目标", "已识别", "已判为威胁", "被干扰", "被拦截", "已捕获或摧毁"};
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
        String[][] t = {
            {"未发现", "已探测", "雷达/电侦/声学/光电发现"}, {"已探测", "已跟踪", "起批航迹"},
            {"已跟踪", "初判为敌方目标", "边缘盒融合初判"}, {"初判为敌方目标", "已识别", "类型与意图识别"},
            {"已识别", "已判为威胁", "指挥所威胁评估"}, {"已判为威胁", "被干扰", "干扰机处置(软杀伤优先)"},
            {"已判为威胁", "被拦截", "拦截无人机或发射枪处置"}, {"被干扰", "已捕获或摧毁", "失控、返航或坠落"},
            {"被干扰", "被拦截", "干扰未奏效，升级处置"},
            {"被拦截", "已捕获或摧毁", "捕获/摧毁成功"}, {"被拦截", "已识别", "拦截失败，重新处置"},
            {"已跟踪", "已探测", "航迹丢失，重新探测"}};
        for (String[] x : t) tr(rg, st.get(x[0]), st.get(x[1]), x[2]);
        tr(rg, st.get("已捕获或摧毁"), fin, null);
        D d = new D("Operational States", p, "目标状态", 1800);
        d.dpe.getDiagram().setOwner(sm);
        d.dropAuto();
        int x = 30;
        d.at(init, x, 120, 30, 30);
        x += 80;
        for (String n : names) {
            d.at(st.get(n), x, 100, 130, 60);
            x += 175;
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
        // 序列 A：FPV 无人机
        seq(p, "FPV 无人机反制时序", new String[]{"fpv", "radar", "esm", "eo", "edge", "cp", "cmdr", "sol", "jam", "air"}, new String[][]{
            {"fpv", "radar", "进入探测区(回波)"}, {"fpv", "esm", "无线电辐射"}, {"radar", "edge", "点迹/航迹 IE2"},
            {"esm", "edge", "信号特征/方位线 IE3"}, {"edge", "cp", "融合航迹、初判 IE7"}, {"eo", "edge", "识别结果 IE4"},
            {"edge", "cp", "4旋翼FPV、侦察意图 IE7"}, {"cp", "cmdr", "作战方案建议 IE9"}, {"cmdr", "cp", "决策指令 IE10"},
            {"cp", "jam", "[升级链1] 干扰任务 IE14"}, {"jam", "fpv", "射频干扰 IE18"}, {"jam", "cp", "干扰状态与效果 IE15"},
            {"cp", "air", "[升级链2] 拦截任务 IE16"}, {"air", "fpv", "空中拦截 IE19"}, {"air", "cp", "起飞与拦截结果 IE17"},
            {"cp", "sol", "[升级链3] 打击任务 IE11"}, {"sol", "fpv", "发射捕获网 IE13"}, {"sol", "cp", "捕获结果 IE12"}});
        // 序列 B：巡飞弹
        seq(p, "巡飞弹反制时序", new String[]{"lm", "esm", "edge", "aco", "eo", "cp", "cmdr", "sol", "jam", "air"}, new String[][]{
            {"lm", "esm", "遥控遥测信号"}, {"esm", "edge", "大致方位 IE3"}, {"edge", "cp", "初判为巡飞弹 IE7"},
            {"lm", "aco", "声学特征"}, {"aco", "edge", "方位角、速度 IE5"}, {"edge", "cp", "航迹更新 IE7"},
            {"cp", "cmdr", "高威胁态势 IE9"}, {"cp", "edge", "调度跟踪移交 IE8"}, {"eo", "edge", "持续跟踪 IE4"},
            {"cp", "jam", "[升级链1] 干扰任务 IE14"}, {"jam", "lm", "射频干扰 IE18"}, {"jam", "cp", "干扰状态与效果 IE15"},
            {"cp", "air", "[升级链2] 拦截任务 IE16"}, {"air", "lm", "空中拦截 IE19"}, {"air", "cp", "起飞与拦截结果 IE17"},
            {"cp", "sol", "[升级链3] 打击任务 IE11"}, {"sol", "lm", "发射捕获网/近炸弹 IE13"}, {"sol", "cp", "拦截结果 IE12"}});
        U.log("B5.sequences 完成");
    }

    /** 创建一张顺序图：生命线取自作战执行者，消息按顺序依次排列。 */
    static void seq(Package p, String title, String[] keys, String[][] msgs) {
        Interaction in = U.put(U.F.createInteractionInstance(), p);
        in.setName(title);
        D d = new D("Operational Sequences", p, title, 2000);
        d.dpe.getDiagram().setOwner(in);
        d.dropAuto();
        Map<String, ShapeElement> sh = new LinkedHashMap<>();
        int x = 30;
        for (String k : keys) {
            Lifeline l = U.F.createLifelineInstance();
            l.setName(((Class) U.get("op:" + k)).getName());
            l.setOwner(in);
            try {
                ShapeElement s = U.V.createShapeElement(l, d.dpe);
                U.V.reshapeShapeElement(s, new Rectangle(x, 30, 140, 520));
                sh.put(k, s);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            x += 220;
        }
        Message prev = null;
        for (String[] e : msgs) {
            Message msg = U.F.createMessageInstance();
            msg.setName(e[2]);
            try {
                U.V.createSequenceMessage(msg, MessageSortEnum.SYNCHCALL, sh.get(e[0]), sh.get(e[1]), false, 0, prev, 25);
            } catch (Exception ex) {
                U.log("警告: 消息创建失败 " + e[2] + " : " + ex);
            }
            prev = msg;
        }
    }
}




