import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationFlow;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationItem;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import java.util.*;

/** 第二部分：作战视点(Op-Tx / Op-Sr / Op-Cn / Op-Tr)。 */
public class B2 {
    /** 作战执行者：key, 名称, 构造型, 职责, 数量 */
    static final String[][] PERF = {
        {"radar", "雷达探测节点", "OperationalPerformer", "低空搜索探测、持续跟踪", "5"},
        {"esm", "电侦探测节点", "OperationalPerformer", "无线电侦测、测向、持续跟踪", "5"},
        {"eo", "光电识别节点", "OperationalPerformer", "光学识别与跟踪", "5"},
        {"aco", "声学探测节点", "OperationalPerformer", "近距声学定向与识别", "10"},
        {"edge", "边缘融合节点", "OperationalPerformer", "本地处理、融合、判别、航迹上报、移交", "25"},
        {"cp", "指挥所", "OperationalPerformer", "态势显示、威胁评估、方案生成、任务分发", "1"},
        {"cmdr", "指挥员", "Post", "作战决策", "1"},
        {"sol", "单兵拦截节点", "OperationalPerformer", "自动部署发射枪、发射拦截", "20"},
        {"jam", "干扰节点", "OperationalPerformer", "对目标射频链路实施干扰、驱离", "5"},
        {"air", "空中拦截节点", "OperationalPerformer", "拦截无人机升空接近并拦截目标", "10"},
        {"fpv", "敌方FPV无人机", "OperationalPerformer", "侦察(外部目标)", "1"},
        {"lm", "敌方巡飞弹", "OperationalPerformer", "攻击(外部目标)", "3"}};

    /** 信息交换：编号, 发送方, 接收方, 内容。同一编号带字母后缀表示同一交换的不同收发对 */
    static final String[][] IE = {
        {"IE1a", "fpv", "radar", "雷达回波(物理环境交互)"},
        {"IE1b", "fpv", "esm", "无线电辐射(物理环境交互)"},
        {"IE1c", "fpv", "eo", "光学特征(物理环境交互)"},
        {"IE1d", "lm", "esm", "无线电辐射(物理环境交互)"},
        {"IE1e", "lm", "eo", "光学特征(物理环境交互)"},
        {"IE1f", "lm", "aco", "声学特征(物理环境交互)"},
        {"IE2", "radar", "edge", "点迹、航迹"},
        {"IE3", "esm", "edge", "信号特征、方位线"},
        {"IE4", "eo", "edge", "识别结果(类型、位置)"},
        {"IE5", "aco", "edge", "方位角、速度、声纹识别结果"},
        {"IE6", "edge", "edge", "跨节点航迹交换、融合结果、跟踪移交(接续)"},
        {"IE7", "edge", "cp", "融合航迹、目标属性、类型、意图、置信度、时间戳"},
        {"IE8a", "cp", "edge", "跟踪监视任务指令(含移交)"},
        {"IE8b", "cp", "eo", "跟踪监视任务指令(含移交)"},
        {"IE9", "cp", "cmdr", "态势、威胁等级、作战方案建议"},
        {"IE10", "cmdr", "cp", "决策指令"},
        {"IE11", "cp", "sol", "打击任务(目标航迹、射击诸元、弹种建议、部署指令)"},
        {"IE12", "sol", "cp", "部署状态、发射报告、捕获或毁伤结果"},
        {"IE13a", "sol", "fpv", "捕获网/近炸弹(物理作用)"},
        {"IE13b", "sol", "lm", "捕获网/近炸弹(物理作用)"},
        {"IE14", "cp", "jam", "干扰任务(目标航迹、信号特征、干扰样式建议)"},
        {"IE15", "jam", "cp", "干扰状态与效果"},
        {"IE16", "cp", "air", "拦截任务(目标航迹、引导信息)"},
        {"IE17", "air", "cp", "起飞与飞行状态、拦截结果"},
        {"IE18a", "jam", "fpv", "射频干扰(物理作用)"},
        {"IE18b", "jam", "lm", "射频干扰(物理作用)"},
        {"IE19a", "air", "fpv", "空中拦截(物理作用)"},
        {"IE19b", "air", "lm", "空中拦截(物理作用)"}};

    /** 执行者展现能力 (Exhibits) */
    static final String[][] EXH = {
        {"radar", "C1"}, {"radar", "C6"}, {"esm", "C2"}, {"esm", "C6"}, {"eo", "C3"}, {"eo", "C6"}, {"aco", "C4"},
        {"edge", "C5"}, {"edge", "C6"}, {"cp", "C7"}, {"cp", "C8"}, {"cmdr", "C8"}, {"sol", "C9"}, {"jam", "C10"}, {"air", "C11"}};

    static final Map<String, InformationItem> ITEMS = new LinkedHashMap<>();

    static void run() {
        Package tx = U.pkg("Operational", "Operational Taxonomy");
        Package sr = U.pkg("Operational", "Operational Structure");
        Package cn = U.pkg("Operational", "Operational Connectivity");
        Package info = U.pkg("Operational", "Operational Information");
        Package tr = U.pkg("Operational", "Operational Traceability");

        // Op-Tx：执行者分类
        D dtx = new D("Operational Taxonomy", tx, "作战执行者分类", 1500);
        Class root = U.uaf(tx, "op:root", "重点区域反无人机作战体系", "OperationalArchitecture");
        for (String[] p : PERF) {
            Class c = U.uaf(tx, "op:" + p[0], p[1], p[2]);
            U.note(tx, c, p[3]);
        }
        dtx.at(root, 640, 30, 240, 60);
        int x = 30;
        for (String[] p : PERF) {
            dtx.at(U.get("op:" + p[0]), x, 200, 120, 60);
            x += 128;
        }

        // Op-Sr：作战结构（体系组成及数量）
        D dsr = new D("Operational Structure", sr, "作战执行者结构", 1500);
        dsr.at(root, 640, 30, 240, 60);
        x = 30;
        for (String[] p : PERF) {
            dsr.at(U.get("op:" + p[0]), x, 200, 120, 60);
            x += 128;
        }
        for (String[] p : PERF) U.comp(root, U.get("op:" + p[0]), p[0], p[4]);

        // 信息项（同内容复用）
        for (String[] ie : IE) {
            if (ITEMS.containsKey(ie[3])) continue;
            InformationItem it = U.put(U.F.createInformationItemInstance(), info);
            it.setName(ie[3]);
            ITEMS.put(ie[3], it);
        }

        // Op-Cn：作战连通
        D dcn = new D("Operational Connectivity", cn, "作战连通", 1500);
        String[] order = {"fpv", "lm", "radar", "esm", "eo", "aco", "edge", "cp", "cmdr", "sol", "jam", "air"};
        int[][] pos = {{30, 60}, {30, 520}, {330, 30}, {330, 150}, {330, 270}, {330, 390}, {650, 210},
            {960, 210}, {1250, 60}, {960, 450}, {1250, 330}, {1250, 450}};
        for (int i = 0; i < order.length; i++) dcn.at(U.get("op:" + order[i]), pos[i][0], pos[i][1], 190, 70);
        Set<String> drawn = new HashSet<>();
        for (String[] ie : IE) {
            Class s = U.get("op:" + ie[1]);
            Class t = U.get("op:" + ie[2]);
            InformationFlow f = U.put(U.F.createInformationFlowInstance(), cn);
            f.getInformationSource().add(s);
            f.getInformationTarget().add(t);
            f.getConveyed().add(ITEMS.get(ie[3]));
            f.setName(ie[0] + " " + ie[3]);
            U.apply(f, "UAF", "OperationalExchange");
            U.reg("ie:" + ie[0], f);
            // 自环(边缘节点之间)不画线，信息交换元素仍在模型中
            if (s != t && drawn.add(ie[1] + ">" + ie[2])) dcn.link(f, s, t);
        }

        // Op-Tr：执行者 → 能力 (Exhibits)
        D dtr = new D("Operational Performers to Capabilities Mapping Matrix", tr, "作战执行者与能力映射", 1500);
        for (String[] e : EXH) U.abs(U.get("op:" + e[0]), U.get("cap:" + e[1]), tr, "UAF", "Exhibits");
        U.log("B2 完成: 执行者 " + PERF.length + "，信息交换 " + IE.length + "，Exhibits " + EXH.length);
    }
}

