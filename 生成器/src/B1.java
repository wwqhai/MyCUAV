import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

/** 第一部分：概述与摘要(Sm)、词典、战略视点(St)。 */
public class B1 {
    /** 能力：编号, 名称, 描述 */
    static final String[][] CAPS = {
        {"C1", "低空雷达探测", "对低空小型目标搜索、检测、起批航迹"},
        {"C2", "无线电侦测与测向", "截获遥控、图传、遥测信号并测向"},
        {"C3", "光电识别与跟踪", "目标成像、类型识别、持续跟踪"},
        {"C4", "声学探测与定向", "近距声源定向、速度估计、声纹识别"},
        {"C5", "多源融合与目标判别", "本地处理、异构数据融合、目标性质/类型/意图判别"},
        {"C6", "持续跟踪与任务移交", "维持融合航迹，探测任务在设备间移交(接续)"},
        {"C7", "指挥控制与态势显示", "航迹汇聚、威胁评估、全流程态势显示、任务分发"},
        {"C8", "辅助决策", "AI 生成作战方案，指挥员决策"},
        {"C9", "近程拦截处置", "自动部署、发射捕获网或近炸弹"}};

    /** 词典术语：名称, 定义 */
    static final String[][] TERMS = {
        {"航迹", "对目标位置、速度、航向随时间变化的连续估计结果"},
        {"多源融合", "对雷达、电侦、光电、声学等异构感知数据进行关联、合并，形成统一航迹与目标属性的过程"},
        {"边缘 AI 计算盒子", "与探测设备配装的本地信息处理装备，完成本地处理、多源融合、探测指挥任务移交(接续)"},
        {"无源电侦设备(ESM)", "高精度被动接收设备，截获目标遥控/图传/遥测信号并测向，最大探测距离 25 km"},
        {"低慢小目标", "低空、低速、小型的空中目标，雷达难以探测"},
        {"FPV 无人机", "小型化第一视角穿越机，本场景为 4 旋翼，意图为侦察"},
        {"巡飞弹", "可在空域巡飞的攻击型弹药，本场景共 3 枚，雷达无法探测"},
        {"声学阵列", "采集声信号并定向、识别的设备，最大声音识别距离 2 km"},
        {"反无人机发射枪", "单兵携带，可发射捕获网(射界 200 m)或近炸弹(射界 1000 m)"},
        {"防护圈", "按探测/识别/拦截距离划分的四层防护：探测圈、光电识别圈、声学圈、单兵拦截圈"},
        {"跟踪移交(接续)", "探测任务在不同设备/节点之间交接，保持航迹连续"},
        {"指挥所", "数据汇聚、态势显示、方案生成、任务分发的中枢，含 AI 指挥系统"}};

    static void run() {
        summary();
        dictionary();
        strategy();
        scenario();
    }

    static void summary() {
        Package p = U.pkg("Summary & Overview");
        D d = new D("Summary and Overview", p, "重点区域反无人机概述与摘要", 1200);
        String t = "【目的】采用 UAF 1.2 方法对重点区域(武器仓库)反无人机作战场景进行体系化分析与建模，形成可追溯的架构模型。\n"
            + "【范围】己方：武器仓库 500 m × 500 m，四层防护圈，雷达 5、电侦 5、光电 5、声学阵列 10、单兵发射枪 20，"
            + "指挥所 1 处，探测设备配装边缘 AI 计算盒子；敌方：FPV 无人机 1 架，巡飞弹 3 枚。\n"
            + "场景自 FPV 进入探测搜索区起，至巡飞弹被捕获、态势显示全流程止。\n"
            + "【假设】电侦也配装边缘盒(共 25 套)等，见分析报告 1.4 节，待确认。\n"
            + "【建模方法】UAF 1.2，MagicDraw 2022x。";
        d.text(t, 30, 30, 900, 240);
        U.note(p, null, t);
    }

    static void dictionary() {
        Package p = U.pkg("Architecture Management");
        // 词典是表格视图：行元素为带 Definition 注释的术语，范围为整个模型
        D dd = new D("Dictionary", p, "重点区域反无人机词典", 1400);
        java.util.List<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element> rows = new java.util.ArrayList<>();
        for (String[] t : TERMS) {
            Class c = U.cls(p, "term:" + t[0], t[0], null, null);
            com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment cm = U.note(p, c, t[1]);
            U.apply(cm, "UAF", "Definition");
            rows.add(c);
        }
        U.tableRows(dd.diagram, rows);
        U.tableTag(dd.diagram, "scope", U.P.getPrimaryModel());
    }

    static void strategy() {
        Package tx = U.pkg("Strategy", "Strategic Taxonomy");
        Package sr = U.pkg("Strategy", "Strategic Structure");
        for (String[] c : CAPS) {
            Class k = U.uaf(tx, "cap:" + c[0], c[0] + " " + c[1], "Capability");
            U.note(tx, k, c[2]);
        }
        // St-Tx 能力分类：按探测感知、融合跟踪、指挥决策、拦截处置分四列
        D dt = new D("Strategic Taxonomy", tx, "重点区域反无人机能力分类", 1500);
        int[][] tpos = {{30, 40}, {30, 140}, {30, 240}, {30, 340}, {330, 140}, {330, 280}, {630, 140}, {630, 280}, {930, 210}};
        for (int i = 0; i < CAPS.length; i++)
            dt.at(U.get("cap:" + CAPS[i][0]), tpos[i][0], tpos[i][1], 240, 60);
        // St-Sr 能力依赖：C1~C4→C5→C6→C7→C8→C9，C6→C9(引导)，C9→C7(效果回送)
        D ds = new D("Strategic Structure", sr, "能力依赖", 1500);
        for (int i = 0; i < CAPS.length; i++)
            ds.at(U.get("cap:" + CAPS[i][0]), tpos[i][0], tpos[i][1], 240, 60);
        String[][] deps = {{"C1", "C5"}, {"C2", "C5"}, {"C3", "C5"}, {"C4", "C5"}, {"C5", "C6"}, {"C6", "C7"},
            {"C7", "C8"}, {"C8", "C9"}, {"C6", "C9"}, {"C9", "C7"}};
        for (String[] e : deps) {
            com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency dp =
                U.dep(U.get("cap:" + e[0]), U.get("cap:" + e[1]), sr, null);
            ds.link(dp, U.get("cap:" + e[0]), U.get("cap:" + e[1]));
        }
        U.log("B1 完成: 能力 " + CAPS.length + " 个，词条 " + TERMS.length + " 个，依赖 " + deps.length + " 条");
    }

    /** 想定片段：编号, 名称, 威胁特征 */
    static final String[][] VIG = {
        {"V1", "单架次侦察渗透", "单目标、低速、低空；以 ISR 为主"},
        {"V2", "多批次集群突防", "巡飞弹 3 枚协同；可能携带小型载荷；雷达难以探测"},
        {"V3", "伴随干扰的饱和攻击(扩展)", "多方向同时进入；强电磁干扰掩护；原场景输入未涉及，待确认"}};

    /** 场景目标：编号, 名称, 度量 */
    static final String[][] GOALS = {
        {"G1", "探测告警时效性", "从首次探测到生成告警的时间 ≤ 30 s"},
        {"G2", "识别正确性", "目标类型识别正确率 ≥ 90%"},
        {"G3", "拦截有效性与安全性", "对确认威胁拦截成功率 ≥ 85%；拦截导致非目标损失事件 = 0"}};

    /** 任务线程(不依赖具体装备)：编号, 名称 */
    static final String[][] MT = {
        {"MT1", "预警探测"}, {"MT2", "目标识别与分类"}, {"MT3", "威胁评估与决策授权"},
        {"MT4", "拦截/驱离行动"}, {"MT5", "效果评估与再交战决策"}};

    /** 想定片段考验的目标 */
    static final String[][] V2G = {{"V1", "G1"}, {"V1", "G2"}, {"V2", "G2"}, {"V2", "G3"},
        {"V3", "G1"}, {"V3", "G2"}, {"V3", "G3"}};
    /** 任务线程实现的目标 */
    static final String[][] MT2G = {{"MT1", "G1"}, {"MT2", "G2"}, {"MT3", "G1"}, {"MT3", "G3"},
        {"MT4", "G3"}, {"MT5", "G3"}};

    static com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile extProfile;

    /** 创建自定义构造型(UAF 1.2 没有 Mission* 原生元素)，放入模型内的“CUAV扩展”Profile；失败返回 null，元素退化为普通类。 */
    static com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype custom(String name, String base) {
        try {
            if (extProfile == null) {
                extProfile = U.put(U.F.createProfileInstance(), U.P.getPrimaryModel());
                extProfile.setName("CUAV扩展");
                com.nomagic.uml2.ext.magicdraw.mdprofiles.ProfileApplication pa = U.F.createProfileApplicationInstance();
                pa.setApplyingPackage(U.P.getPrimaryModel());
                pa.setAppliedProfile(extProfile);
            }
            Class meta = com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper.getMetaClassByName(U.P, base);
            com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype s =
                com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper.createStereotype(extProfile, name, java.util.Arrays.asList(meta));
            U.log("自定义构造型 " + name + " 位于 " + (s.getOwner() == null ? "?" : s.getOwner().getHumanName()));
            return s;
        } catch (Throwable t) {
            U.log("警告: 创建自定义构造型失败 " + name + " : " + t);
            return null;
        }
    }

    /** St-Tx 场景与目标分解图：场景→想定片段→目标←任务线程←任务工程线程(MET)。 */
    static void scenario() {
        Package tx = U.pkg("Strategy", "Strategic Taxonomy");
        com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype stMT = custom("MissionThread", "Class");
        com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype stMET = custom("MissionEngineeringThread", "Class");

        Class sc = U.uaf(tx, "sc:SC1", "SC1 重点区域反无人机防护", "StrategicPhase");
        U.attrS(sc, "定义", "在重点区域(武器仓库)对敌方无人机实施探测、识别、决策与拦截的端到端任务情境");
        for (String[] v : VIG) {
            Class k = U.uaf(tx, "vig:" + v[0], v[0] + " " + v[1], "StrategicPhase");
            U.attrS(k, "威胁特征", v[2]);
            U.comp(sc, k, v[0], null);
                    }
        for (String[] g : GOALS) {
            Class k = U.uaf(tx, "goal:" + g[0], g[0] + " " + g[1], "EnterpriseGoal");
            U.attrS(k, "度量", g[2]);
        }
        for (String[] m : MT) {
            Class k = U.cls(tx, "mt:" + m[0], m[0] + " " + m[1], null, null);
            if (stMT != null) com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper.addStereotype(k, stMT);
        }
        Class met = U.cls(tx, "met:MET1", "MET1 基线反无人机方案", null, null);
        if (stMET != null) com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper.addStereotype(met, stMET);
        U.attrS(met, "探测资源", "低空雷达 + 无源电侦 + 光电 + 声学阵列(边缘 AI 盒子融合)");
        U.attrS(met, "决策节点", "指挥所(AI 指挥系统 + 指挥员)");
        U.attrS(met, "拦截资源", "单兵反无人机发射枪(捕获网 / 近炸弹)");
        U.attrS(met, "评估节点", "指挥所态势显示与效果评估");

        D d = new D("Strategic Taxonomy", tx, "场景与目标分解", 1500);
        d.at(U.get("sc:SC1"), 560, 30, 400, 110);
        int[] vx = {100, 560, 1020};
        for (int i = 0; i < VIG.length; i++) d.at(U.get("vig:" + VIG[i][0]), vx[i], 230, 380, 110);
        for (int i = 0; i < GOALS.length; i++) d.at(U.get("goal:" + GOALS[i][0]), vx[i], 440, 380, 110);
        for (int i = 0; i < MT.length; i++) d.at(U.get("mt:" + MT[i][0]), 30 + i * 290, 660, 260, 60);
        d.at(U.get("met:MET1"), 480, 800, 560, 160);

        int n = 0;
        // 场景→想定片段的组合关联在模型中，MagicDraw API 无法在图中为其连线(已三次尝试均失败)，需在界面中拖入关系
        for (String[] e : V2G) { trace(d, tx, "vig:" + e[0], "goal:" + e[1]); n++; }
        for (String[] e : MT2G) { trace(d, tx, "mt:" + e[0], "goal:" + e[1]); n++; }
        for (String[] m : MT) { trace(d, tx, "met:MET1", "mt:" + m[0]); n++; }
        U.log("B1 场景与目标分解: 想定 " + VIG.length + "，目标 " + GOALS.length + "，任务线程 " + MT.length + "，追溯 " + n + " 条");
    }

    static void trace(D d, Package owner, String from, String to) {
        com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction a =
            U.abs(U.get(from), U.get(to), owner, "UML Standard Profile", "Trace");
        d.link(a, U.get(from), U.get(to));
    }
}
