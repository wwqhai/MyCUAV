import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

/** 第一部分：概述与摘要(Sm)、词典(Am-If)、战略视点(St)。 */
public class B1 {
    static final String[][] CAPS = {
        {"C1", "空情探测", "对空中目标搜索、发现、跟踪"},
        {"C2", "电磁侦察", "被动截获、测向、识别辐射源"},
        {"C3", "信息融合", "多源数据关联、融合、航迹生成"},
        {"C4", "目标识别与意图判断", "属性、类型、意图判别"},
        {"C5", "指挥决策", "方案生成、决策、任务下达"},
        {"C6", "火力打击", "发射、制导、拦截"},
        {"C7", "毁伤评估", "判断目标是否被摧毁"},
        {"C8", "态势呈现", "全流程态势生成与显示"}};

    static final String[][] TERMS = {
        {"航迹", "对空中目标位置、速度、航向随时间变化的连续估计结果"},
        {"信息融合", "对多源探测与侦察数据进行关联、合并，形成统一航迹与目标属性的过程"},
        {"毁伤评估", "判断目标是否被摧毁、失去威胁能力的过程"},
        {"相控阵雷达", "三坐标相控阵雷达，提供目标距离、方位、高度信息，探测半径 300 km"},
        {"电子侦察设备(ESM)", "被动接收辐射源信号，实现截获、测向、识别，侦测半径 400 km"},
        {"边缘信息处理装备", "与探测/侦察设备配装的本地信息处理装备，完成本地融合、初判与航迹上报"},
        {"地空导弹系统", "近界 30 km、远界 260 km 的地面防空武器系统"},
        {"指挥所", "数据汇聚、信息处理、任务下达中枢，含 AI 指挥系统与态势系统"}};

    static void run() {
        summary();
        dictionary();
        strategy();
    }

    static void summary() {
        Package p = U.pkg("Summary & Overview");
        D d = new D("Summary and Overview", p, "区域防空概述与摘要", 1200);
        String t = "【目的】采用 UAF 方法对区域防空作战场景进行体系化分析与建模，形成可追溯、可验证的架构模型。\n"
            + "【范围】己方：3 部三坐标相控阵雷达、2 部电子侦察设备(ESM)、1 处地面指挥所、2 套地空导弹系统，"
            + "探测与侦察设备配装边缘信息处理装备；敌方：1 架作战飞机。\n"
            + "场景自敌机进入侦测区起，至击毁、毁伤评估、态势显示止。\n"
            + "【区域】部署区域 500 km × 500 km（假设，已与用户确认）。\n"
            + "【建模方法】UAF 1.2，MagicDraw 2022x。";
        d.text(t, 30, 30, 900, 220);
        U.note(p, null, t);
    }

    static void dictionary() {
        Package p = U.pkg("Architecture Management");
        // 词典视图(Am-If)是表格型视图，内容由 Definition 构造型的注释经报表生成，图中不放形状
        D dd = new D("Dictionary", p, "区域防空词典", 1400);
        java.util.List<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element> rows = new java.util.ArrayList<>();
        for (String[] t : TERMS) {
            Class c = U.cls(p, "term:" + t[0], t[0], null, null);
            com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment cm = U.note(p, c, t[1]);
            U.apply(cm, "UAF", "Definition");
            rows.add(c);
        }
        // 词典是表格视图：行元素为术语，范围为整个模型（与官方示例一致）
        U.tableRows(dd.diagram, rows);
        U.tableTag(dd.diagram, "scope", U.P.getPrimaryModel());
    }

    static void strategy() {
        Package tx = U.pkg("Strategy", "Strategic Taxonomy");
        Package sr = U.pkg("Strategy", "Strategic Structure");
        Class root = U.uaf(tx, "cap:root", "区域防空能力", "Capability");
        for (String[] c : CAPS) {
            Class k = U.uaf(tx, "cap:" + c[0], c[0] + " " + c[1], "Capability");
            U.note(tx, k, c[2]);
        }
        // St-Tx 战略分类：区域防空能力 ← C1..C8
        D dt = new D("Strategic Taxonomy", tx, "区域防空能力分类", 1500);
        dt.at(root, 560, 30, 220, 60);
        int x = 30;
        for (String[] c : CAPS) {
            Class k = U.get("cap:" + c[0]);
            dt.at(k, x, 200, 165, 60);
            x += 180;
        }
        for (String[] c : CAPS) {
            Class k = U.get("cap:" + c[0]);
            com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization g = U.gen(k, root);
            dt.link(g, k, root);
        }
        // St-Sr 战略结构：能力依赖 C1,C2→C3→C4→C5→C6→C7→C8, C8→C5
        D ds = new D("Strategic Structure", sr, "能力依赖", 1500);
        int[][] pos = {{30, 30}, {30, 200}, {300, 115}, {560, 115}, {820, 115}, {1080, 115}, {1080, 300}, {820, 300}};
        for (int i = 0; i < CAPS.length; i++)
            ds.at(U.get("cap:" + CAPS[i][0]), pos[i][0], pos[i][1], 200, 60);
        String[][] deps = {{"C1", "C3"}, {"C2", "C3"}, {"C3", "C4"}, {"C4", "C5"}, {"C5", "C6"}, {"C6", "C7"}, {"C7", "C8"}, {"C8", "C5"}};
        for (String[] e : deps) {
            com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency dp =
                U.dep(U.get("cap:" + e[0]), U.get("cap:" + e[1]), sr, null);
            ds.link(dp, U.get("cap:" + e[0]), U.get("cap:" + e[1]));
        }
        U.log("B1 完成: 能力 " + CAPS.length + " 个，词条 " + TERMS.length + " 个");
    }
}
