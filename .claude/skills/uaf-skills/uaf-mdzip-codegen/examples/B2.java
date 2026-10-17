import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationFlow;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationItem;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import java.util.*;

/** 第二部分：作战视点(Op-Tx / Op-Sr / Op-Cn / Op-Tr)。 */
public class B2 {
    /** 作战执行者：key, 名称, 构造型, 职责 */
    static final String[][] PERF = {
        {"radar", "雷达探测节点", "OperationalPerformer", "搜索、跟踪、引导支持、毁伤观测"},
        {"esm", "电侦节点", "OperationalPerformer", "辐射源侦收、定位、识别"},
        {"edge", "边缘处理节点", "OperationalPerformer", "本地融合、初判、航迹上报"},
        {"cp", "指挥所", "OperationalPerformer", "汇聚、研判、方案、任务下达、态势显示"},
        {"cmdr", "指挥员", "Post", "方案决策"},
        {"sam", "地空导弹单元", "OperationalPerformer", "接收任务、发射、持续引导、拦截"},
        {"enemy", "敌机", "OperationalPerformer", "入侵目标（外部执行者）"}};

    /** 信息交换：编号, 发送方, 接收方, 内容 */
    static final String[][] IE = {
        {"IE1", "enemy", "radar", "雷达回波、电磁辐射（物理环境交互）"},
        {"IE1b", "enemy", "esm", "电磁辐射（物理环境交互）"},
        {"IE2a", "radar", "edge", "原始点迹、融合结果"},
        {"IE2b", "esm", "edge", "侦察报告"},
        {"IE2c", "edge", "radar", "融合结果回传"},
        {"IE2d", "edge", "esm", "融合结果回传"},
        {"IE3", "edge", "cp", "融合航迹、目标属性、识别结果"},
        {"IE4", "cp", "cmdr", "态势、威胁判断、作战方案"},
        {"IE5", "cmdr", "cp", "决策结果"},
        {"IE6", "cp", "sam", "打击任务、目标指示"},
        {"IE7a", "cp", "sam", "持续引导数据、航迹更新"},
        {"IE7b", "edge", "sam", "持续引导数据、航迹更新"},
        {"IE8a", "radar", "cp", "毁伤评估结果"},
        {"IE8b", "esm", "cp", "毁伤评估结果"}};

    static final Map<String, InformationItem> ITEMS = new LinkedHashMap<>();

    static void run() {
        Package tx = U.pkg("Operational", "Operational Taxonomy");
        Package sr = U.pkg("Operational", "Operational Structure");
        Package cn = U.pkg("Operational", "Operational Connectivity");
        Package info = U.pkg("Operational", "Operational Information");
        Package tr = U.pkg("Operational", "Operational Traceability");

        // Op-Tx：执行者分类
        D dtx = new D("Operational Taxonomy", tx, "作战执行者分类", 1500);
        Class root = U.uaf(tx, "op:root", "区域防空作战体系", "OperationalArchitecture");
        for (String[] p : PERF) {
            Class c = U.uaf(tx, "op:" + p[0], p[1], p[2]);
            U.note(tx, c, p[3]);
        }
        dtx.at(root, 560, 30, 220, 60);
        int x = 30;
        for (String[] p : PERF) {
            Class c = U.get("op:" + p[0]);
            dtx.at(c, x, 200, 170, 60);
            x += 190;
        }

        // Op-Sr：作战结构（体系组成）
        D dsr = new D("Operational Structure", sr, "作战执行者结构", 1500);
        dsr.at(root, 560, 30, 220, 60);
        x = 30;
        for (String[] p : PERF) {
            Class c = U.get("op:" + p[0]);
            dsr.at(c, x, 200, 170, 60);
            x += 190;
        }
        // 数量约束：3 雷达、2 ESM、2 地空导弹、1 指挥所、5 边缘处理
        String[][] comp = {{"radar", "3"}, {"esm", "2"}, {"edge", "5"}, {"cp", "1"}, {"cmdr", "1"}, {"sam", "2"}, {"enemy", "1"}};
        for (String[] c : comp) {
            U.comp(root, U.get("op:" + c[0]), c[0], c[1]);
        }

        // 信息项
        for (String[] ie : IE) {
            if (ITEMS.containsKey(ie[3])) continue;
            InformationItem it = U.put(U.F.createInformationItemInstance(), info);
            it.setName(ie[3]);
            ITEMS.put(ie[3], it);
        }

        // Op-Cn：作战连通（执行者之间的信息交换）
        D dcn = new D("Operational Connectivity", cn, "作战连通", 1500);
        int[][] pos = {{30, 80}, {30, 330}, {400, 200}, {750, 200}, {1100, 80}, {1100, 330}, {750, 450}};
        String[] order = {"radar", "esm", "edge", "cp", "cmdr", "sam", "enemy"};
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
            String key = ie[1] + ">" + ie[2];
            if (drawn.add(key)) dcn.link(f, s, t);
        }

        // Op-Tr：执行者 -> 能力（Exhibits）
        D dtr = new D("Operational Performers to Capabilities Mapping Matrix", tr, "作战执行者与能力映射", 1500);
        String[][] exh = {{"radar", "C1"}, {"radar", "C7"}, {"esm", "C2"}, {"esm", "C7"}, {"edge", "C3"}, {"edge", "C4"},
            {"cp", "C3"}, {"cp", "C4"}, {"cp", "C5"}, {"cp", "C7"}, {"cp", "C8"}, {"cmdr", "C5"}, {"sam", "C6"}};
        for (String[] e : exh) {
            U.abs(U.get("op:" + e[0]), U.get("cap:" + e[1]), tr, "UAF", "Exhibits");
        }
        U.log("B2 完成: 执行者 " + PERF.length + "，信息交换 " + IE.length + "，Exhibits " + exh.length);
    }
}
