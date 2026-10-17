import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationFlow;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationItem;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import java.util.*;

/** 第四部分：服务视点(Sv)、人员视点(Pr)、资源视点(Rs)。 */
public class B4 {
    /** 服务：key, 名称, 提供方(op key), 使用方描述, 对应作战活动 */
    static final String[][] SV = {
        {"s1", "空情探测服务", "radar", "边缘处理、指挥所", "OA1,OA4"},
        {"s2", "电磁侦察服务", "esm", "边缘处理、指挥所", "OA2,OA4"},
        {"s3", "融合航迹服务", "edge", "指挥所、地空导弹单元", "OA3,OA5"},
        {"s4", "决策支持服务", "cp", "指挥员", "OA6,OA7"},
        {"s5", "火力打击服务", "sam", "指挥所", "OA9,OA10"},
        {"s6", "毁伤评估服务", "radar", "指挥所", "OA11"},
        {"s7", "态势显示服务", "cp", "指挥员", "OA12"}};

    /** 服务调用关系：调用方服务 → 被调用服务 */
    static final String[][] SV_CALL = {
        {"s3", "s1"}, {"s3", "s2"}, {"s4", "s3"}, {"s5", "s3"}, {"s6", "s1"}, {"s6", "s2"}, {"s7", "s3"}, {"s7", "s6"}};

    /** 资源：key, 名称, 数量, 内部功能 */
    static final String[][] RS = {
        {"r_radar", "三坐标相控阵雷达", "3", "空域搜索、目标跟踪、制导信息提供、毁伤观测"},
        {"r_esm", "电子侦察设备 ESM", "2", "信号截获、测向定位、辐射源识别"},
        {"r_edge", "边缘信息处理装备", "5", "数据关联、融合、初步识别、航迹上报"},
        {"r_cp", "指挥所系统", "1", "信息汇聚、AI 辅助决策、任务下达、态势显示"},
        {"r_sam", "地空导弹系统", "2", "接收任务、发射、制导、拦截"}};

    /** 资源 ↔ 作战执行者 */
    static final String[][] RS_OP = {
        {"r_radar", "radar"}, {"r_esm", "esm"}, {"r_edge", "edge"}, {"r_cp", "cp"}, {"r_sam", "sam"}};

    /** 资源接口：编号, 源, 目标, 内容 */
    static final String[][] RS_FLOW = {
        {"RF1", "r_radar", "r_edge", "原始点迹、融合结果"},
        {"RF2", "r_esm", "r_edge", "侦察报告"},
        {"RF3", "r_edge", "r_cp", "航迹、识别结果"},
        {"RF4", "r_cp", "r_sam", "打击任务"},
        {"RF5", "r_cp", "r_sam", "引导数据"},
        {"RF6", "r_radar", "r_sam", "引导数据"}};

    static void run() {
        services();
        personnel();
        resources();
    }

    static void services() {
        Package tx = U.pkg("Services", "Services Taxonomy");
        Package sr = U.pkg("Services", "Services Structure");
        Package cn = U.pkg("Services", "Services Connectivity");
        Package tr = U.pkg("Services", "Services Traceability");

        D dtx = new D("Services Taxonomy", tx, "服务分类", 1500);
        for (String[] s : SV) {
            Class c = U.uaf(tx, "sv:" + s[0], s[1], "Service");
            U.note(tx, c, "提供方：" + ((Class) U.get("op:" + s[2])).getName() + "；使用方：" + s[3]);
            dtx.add(c, 190, 60);
        }
        D dsr = new D("Services Structure", sr, "服务结构", 1500);
        for (String[] s : SV) dsr.add(U.get("sv:" + s[0]), 190, 60);

        D dcn = new D("Services Connectivity", cn, "服务连通", 1500);
        int[][] pos = {{30, 300}, {30, 450}, {330, 375}, {630, 220}, {630, 520}, {330, 100}, {930, 370}};
        String[] ord = {"s1", "s2", "s3", "s4", "s5", "s6", "s7"};
        for (int i = 0; i < ord.length; i++) dcn.at(U.get("sv:" + ord[i]), pos[i][0], pos[i][1], 190, 60);
        InformationItem req = U.put(U.F.createInformationItemInstance(), cn);
        req.setName("服务请求/响应");
        for (String[] c : SV_CALL) {
            InformationFlow f = U.put(U.F.createInformationFlowInstance(), cn);
            f.getInformationSource().add(U.get("sv:" + c[0]));
            f.getInformationTarget().add(U.get("sv:" + c[1]));
            f.getConveyed().add(req);
            f.setName(((Class) U.get("sv:" + c[0])).getName() + "→" + ((Class) U.get("sv:" + c[1])).getName());
            U.apply(f, "UAF", "ServiceExchange");
            dcn.link(f, U.get("sv:" + c[0]), U.get("sv:" + c[1]));
        }

        // Sv-Tr：服务→能力 (Exhibits)，服务→作战活动 (Supports)
        D dt1 = new D("Services to Capabilities Mapping Matrix", tr, "服务与能力映射", 1500);
        D dt2 = new D("Services to Operational Activities Mapping Matrix", tr, "服务与作战活动映射", 1500);
        for (String[] s : SV) {
            for (String oa : s[4].split(",")) {
                U.abs(U.get("sv:" + s[0]), U.get("oa:" + oa), tr, "UAF", "Supports");
            }
        }
        String[][] cap = {{"s1", "C1"}, {"s2", "C2"}, {"s3", "C3"}, {"s3", "C4"}, {"s4", "C5"}, {"s5", "C6"}, {"s6", "C7"}, {"s7", "C8"}};
        for (String[] c : cap) U.abs(U.get("sv:" + c[0]), U.get("cap:" + c[1]), tr, "UAF", "Exhibits");
        U.log("B4.services 完成: 服务 " + SV.length);
    }

    static void personnel() {
        Package tx = U.pkg("Personnel", "Personnel Taxonomy");
        Package sr = U.pkg("Personnel", "Personnel Structure");
        D dtx = new D("Personnel Taxonomy", tx, "人员角色分类", 1200);
        Class org = U.uaf(tx, "pr:cp", "指挥所组织", "Organization");
        Class cmdr = U.uaf(tx, "pr:cmdr", "指挥员", "Post");
        Class duty = U.uaf(tx, "pr:duty", "值班员", "Post");
        U.note(tx, cmdr, "方案决策");
        U.note(tx, duty, "监视态势、协助指挥员、保障人机交互");
        dtx.at(org, 300, 40, 200, 60);
        dtx.at(cmdr, 100, 220, 180, 60);
        dtx.at(duty, 520, 220, 180, 60);
        D dsr = new D("Personnel Structure", sr, "人员结构", 1200);
        dsr.at(org, 300, 40, 200, 60);
        dsr.at(cmdr, 100, 220, 180, 60);
        dsr.at(duty, 520, 220, 180, 60);
        U.comp(org, cmdr, "指挥员", "1");
        U.comp(org, duty, "值班员", "1..*");
        U.log("B4.personnel 完成");
    }

    static void resources() {
        Package tx = U.pkg("Resources", "Resources Taxonomy");
        Package sr = U.pkg("Resources", "Resources Structure");
        Package cn = U.pkg("Resources", "Resources Connectivity");
        Package pr = U.pkg("Resources", "Resources Processes");
        Package tr = U.pkg("Resources", "Resources Traceability");

        D dtx = new D("Resources Taxonomy", tx, "资源分类", 1500);
        for (String[] r : RS) {
            Class c = U.uaf(tx, "rs:" + r[0], r[1], "ResourceArtifact");
            U.note(tx, c, "数量：" + r[2] + "；内部功能：" + r[3]);
            dtx.add(c, 200, 60);
        }
        // Rs-Sr 结构：体系
        D dsr = new D("Resources Structure", sr, "资源结构", 1500);
        Class arch = U.uaf(sr, "rs:arch", "区域防空资源体系", "ResourceArchitecture");
        dsr.at(arch, 560, 30, 240, 60);
        int x = 30;
        for (String[] r : RS) {
            dsr.at(U.get("rs:" + r[0]), x, 220, 200, 60);
            x += 240;
        }
        for (String[] r : RS) U.comp(arch, U.get("rs:" + r[0]), r[0].substring(2), r[2]);

        // Rs-Cn 连通
        D dcn = new D("Resources Connectivity", cn, "资源连通", 1500);
        int[][] pos = {{30, 80}, {30, 330}, {400, 200}, {770, 200}, {1100, 200}};
        for (int i = 0; i < RS.length; i++) dcn.at(U.get("rs:" + RS[i][0]), pos[i][0], pos[i][1], 220, 70);
        Map<String, InformationItem> items = new HashMap<>();
        for (String[] f : RS_FLOW) {
            InformationItem it = items.computeIfAbsent(f[3], k -> {
                InformationItem ii = U.put(U.F.createInformationItemInstance(), cn);
                ii.setName(k);
                return ii;
            });
            InformationFlow fl = U.put(U.F.createInformationFlowInstance(), cn);
            fl.getInformationSource().add(U.get("rs:" + f[1]));
            fl.getInformationTarget().add(U.get("rs:" + f[2]));
            fl.getConveyed().add(it);
            fl.setName(f[0] + " " + f[3]);
            U.apply(fl, "UAF", "ResourceExchange");
            dcn.link(fl, U.get("rs:" + f[1]), U.get("rs:" + f[2]));
        }

        // Rs-Pr：各资源的系统功能 (Function)
        D dpr = new D("Resources Processes", pr, "系统功能", 1500);
        for (String[] r : RS) {
            int n = 0;
            for (String fn : r[3].split("、")) {
                com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity a =
                    U.put(U.F.createActivityInstance(), pr);
                a.setName(r[1] + "：" + fn);
                U.apply(a, "UAF", "Function");
                U.reg("fn:" + r[0] + ":" + (n++), a);
                dpr.add(a, 230, 60);
            }
        }

        // Rs-Tr：资源 -IsCapableToPerform-> 功能 -Implements-> 作战活动（与官方 UAF 示例一致）
        D dt1 = new D("Resources to Operational Activities Mapping Matrix", tr, "资源与作战活动映射", 1500);
        D dt2 = new D("Functions to Operational Activities Mapping Matrix", tr, "系统功能与作战活动映射", 1500);
        // 功能名 -> 实现的作战活动
        Map<String, String> fnOa = new LinkedHashMap<>();
        fnOa.put("空域搜索", "OA1");
        fnOa.put("目标跟踪", "OA4");
        fnOa.put("制导信息提供", "OA9");
        fnOa.put("毁伤观测", "OA11");
        fnOa.put("信号截获", "OA2");
        fnOa.put("测向定位", "OA2");
        fnOa.put("辐射源识别", "OA2");
        fnOa.put("数据关联", "OA3");
        fnOa.put("融合", "OA3");
        fnOa.put("初步识别", "OA5");
        fnOa.put("航迹上报", "OA4");
        fnOa.put("信息汇聚", "OA3");
        fnOa.put("AI 辅助决策", "OA6");
        fnOa.put("任务下达", "OA8");
        fnOa.put("态势显示", "OA12");
        fnOa.put("接收任务", "OA8");
        fnOa.put("发射", "OA9");
        fnOa.put("制导", "OA9");
        fnOa.put("拦截", "OA10");
        int nImpl = 0, nCap = 0;
        for (String[] r : RS) {
            int n = 0;
            for (String fn : r[3].split("、")) {
                Object fo = U.get("fn:" + r[0] + ":" + (n++));
                String oa = fnOa.get(fn);
                if (oa == null) {
                    U.log("警告: 功能未映射到作战活动 " + fn);
                    continue;
                }
                U.abs(U.get("rs:" + r[0]), (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element) fo, tr, "UAF", "IsCapableToPerform");
                U.abs((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element) fo, U.get("oa:" + oa), tr, "UAF", "Implements");
                nCap++;
                nImpl++;
            }
            // 资源展现能力
        }
        String[][] rcap = {{"r_radar", "C1"}, {"r_radar", "C7"}, {"r_esm", "C2"}, {"r_esm", "C7"}, {"r_edge", "C3"}, {"r_edge", "C4"},
            {"r_cp", "C5"}, {"r_cp", "C7"}, {"r_cp", "C8"}, {"r_sam", "C6"}};
        for (String[] c : rcap) U.abs(U.get("rs:" + c[0]), U.get("cap:" + c[1]), tr, "UAF", "Exhibits");
        U.log("B4 Rs-Tr: IsCapableToPerform " + nCap + "，Implements " + nImpl);
        U.log("B4.resources 完成: 资源 " + RS.length + "，接口 " + RS_FLOW.length);
    }
}
