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
}

