import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationFlow;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationItem;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import java.util.*;

/** 第四部分：服务视点(Sv)、人员视点、资源视点(Rs)。 */
public class B4 {
    /** 服务：key, 名称, 提供方(op key), 使用方描述, 支持的作战活动 */
    static final String[][] SV = {
        {"s1", "低空雷达探测服务", "radar", "边缘融合节点", "OA1,OA5"},
        {"s2", "无源电侦服务", "esm", "边缘融合节点", "OA2,OA5"},
        {"s3", "光电识别跟踪服务", "eo", "边缘融合节点、指挥所", "OA7"},
        {"s4", "声学探测定向服务", "aco", "边缘融合节点", "OA14"},
        {"s5", "多源融合与目标判别服务", "edge", "指挥所、其他边缘融合节点", "OA3,OA4,OA6,OA8"},
        {"s6", "跟踪移交服务", "edge", "探测节点、指挥所", "OA16"},
        {"s7", "作战方案生成服务", "cp", "指挥员", "OA9,OA10"},
        {"s8", "态势显示服务", "cp", "指挥员", "OA15,OA17"},
        {"s9", "打击任务分发服务", "cp", "单兵拦截节点、干扰节点、空中拦截节点", "OA11"},
        {"s10", "拦截执行服务", "sol", "指挥所", "OA12,OA13"},
        {"s11", "电子干扰服务", "jam", "指挥所", "OA18"},
        {"s12", "空中拦截服务", "air", "指挥所", "OA19"}};

    /** 服务调用：调用方 → 被调用方 */
    static final String[][] SV_CALL = {
        {"s5", "s1"}, {"s5", "s2"}, {"s5", "s3"}, {"s5", "s4"}, {"s6", "s5"}, {"s7", "s5"},
        {"s8", "s5"}, {"s9", "s7"}, {"s10", "s9"}, {"s8", "s10"},
        {"s11", "s9"}, {"s12", "s9"}, {"s8", "s11"}, {"s8", "s12"}};

    /** 服务展现能力 */
    static final String[][] SV_CAP = {
        {"s1", "C1"}, {"s2", "C2"}, {"s3", "C3"}, {"s4", "C4"}, {"s5", "C5"}, {"s6", "C6"},
        {"s7", "C8"}, {"s8", "C7"}, {"s9", "C7"}, {"s10", "C9"}, {"s11", "C10"}, {"s12", "C11"}};

    /** 资源：key, 名称, 数量, 功能表(功能=实现的作战活动，竖线分隔) */
    static final String[][] RS = {
        {"r_radar", "低空探测雷达(平板相控阵)", "5", "空域搜索=OA1|目标检测=OA1|航迹起始=OA1|持续跟踪=OA5"},
        {"r_esm", "无源电侦设备(ESM)", "5", "信号截获=OA2|测向=OA2|信号特征提取=OA2|持续跟踪=OA5"},
        {"r_eo", "光电识别设备", "5", "目标捕获成像=OA7|类型识别=OA7|光电跟踪=OA7"},
        {"r_aco", "声学阵列设备", "10", "声信号采集=OA14|声源定向=OA14|声纹识别=OA14|速度估计=OA14"},
        {"r_edge", "边缘AI计算盒子", "25", "本地处理融合=OA3|航迹关联=OA3|目标判别=OA4|类型意图识别=OA8|航迹上报=OA6|跟踪移交=OA16"},
        {"r_gun", "反无人机发射枪", "20", "任务接收与自动部署=OA12|瞄准解算与发射=OA13"},
        {"r_c2", "指挥系统(含AI)", "1", "态势生成显示=OA15|威胁评估=OA15|方案生成=OA9|任务分发=OA11|效果评估=OA17"},
        {"r_cp", "指挥所", "1", "决策席位支撑=OA10"},
        {"r_net", "战场通信网络", "1", "信息传输=OA6"},
        {"r_jam", "干扰机", "5", "任务接收=OA18|信号分析与干扰样式选择=OA18|射频干扰发射=OA18|干扰效果监测=OA18"},
        {"r_uav", "拦截无人机", "10", "任务接收与起飞=OA19|飞向目标与末端制导=OA19|拦截执行=OA19|状态回传=OA19"}};

    /** 资源接口流：编号, 源, 目标, 内容 */
    static final String[][] RS_FLOW = {
        {"RF1", "r_radar", "r_edge", "点迹、航迹"}, {"RF2", "r_esm", "r_edge", "信号特征、方位线"},
        {"RF3", "r_eo", "r_edge", "识别结果"}, {"RF4", "r_aco", "r_edge", "方位角、速度、声纹识别结果"},
        {"RF5", "r_edge", "r_c2", "融合航迹、目标属性、意图、置信度"}, {"RF6", "r_c2", "r_edge", "跟踪监视任务指令"},
        {"RF7", "r_c2", "r_gun", "打击任务"}, {"RF8", "r_gun", "r_c2", "部署状态、发射与捕获结果"},
        {"RF9", "r_c2", "r_jam", "干扰任务：目标航迹、信号特征、干扰样式建议"}, {"RF10", "r_jam", "r_c2", "干扰状态与效果"},
        {"RF11", "r_c2", "r_uav", "拦截任务：目标航迹、引导信息"}, {"RF12", "r_uav", "r_c2", "起飞与飞行状态、拦截结果"}};

    /** 资源展现能力 */
    static final String[][] RS_CAP = {
        {"r_radar", "C1"}, {"r_radar", "C6"}, {"r_esm", "C2"}, {"r_esm", "C6"}, {"r_eo", "C3"}, {"r_eo", "C6"},
        {"r_aco", "C4"}, {"r_edge", "C5"}, {"r_edge", "C6"}, {"r_c2", "C7"}, {"r_c2", "C8"}, {"r_cp", "C8"},
        {"r_gun", "C9"}, {"r_net", "C7"}, {"r_jam", "C10"}, {"r_uav", "C11"}};

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
            dtx.add(c, 220, 60);
        }
        D dsr = new D("Services Structure", sr, "服务结构", 1500);
        for (String[] s : SV) dsr.add(U.get("sv:" + s[0]), 220, 60);

        D dcn = new D("Services Connectivity", cn, "服务连通", 1500);
        int[][] pos = {{30, 60}, {30, 170}, {30, 280}, {30, 390}, {330, 225}, {630, 60}, {630, 225}, {930, 225},
            {630, 390}, {930, 390}, {630, 500}, {930, 500}};
        for (int i = 0; i < SV.length; i++) dcn.at(U.get("sv:" + SV[i][0]), pos[i][0], pos[i][1], 220, 60);
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

        // Sv-Tr：服务 → 能力 (Exhibits)，服务 → 作战活动 (Supports)
        D dt1 = new D("Services to Capabilities Mapping Matrix", tr, "服务与能力映射", 1500);
        D dt2 = new D("Services to Operational Activities Mapping Matrix", tr, "服务与作战活动映射", 1500);
        for (String[] s : SV)
            for (String oa : s[4].split(","))
                U.abs(U.get("sv:" + s[0]), U.get("oa:" + oa), tr, "UAF", "Supports");
        for (String[] c : SV_CAP) U.abs(U.get("sv:" + c[0]), U.get("cap:" + c[1]), tr, "UAF", "Exhibits");
        U.log("B4.services 完成: 服务 " + SV.length + "，调用 " + SV_CALL.length);
    }

    static void personnel() {
        Package tx = U.pkg("Personnel", "Personnel Taxonomy");
        Package sr = U.pkg("Personnel", "Personnel Structure");
        Class org = U.uaf(tx, "pr:cp", "指挥所组织", "Organization");
        Class cmdr = U.uaf(tx, "pr:cmdr", "指挥员", "Post");
        Class sol = U.uaf(tx, "pr:sol", "单兵", "Post");
        U.note(tx, cmdr, "作战决策，人在回路(假设 A4)");
        U.note(tx, sol, "携带反无人机发射枪，共 20 名(假设 A3)");
        for (String[] spec : new String[][]{{"Personnel Taxonomy", "人员角色分类"}, {"Personnel Structure", "人员结构"}}) {
            D d = new D(spec[0], spec[0].endsWith("Taxonomy") ? tx : sr, spec[1], 1200);
            d.at(org, 300, 40, 200, 60);
            d.at(cmdr, 100, 220, 180, 60);
            d.at(sol, 520, 220, 180, 60);
        }
        U.comp(org, cmdr, "指挥员", "1");
        U.comp(org, sol, "单兵", "20");
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
            U.note(tx, c, "数量：" + r[2]);
            dtx.add(c, 220, 60);
        }
        // Rs-Sr 结构：体系及数量
        D dsr = new D("Resources Structure", sr, "资源结构", 1700);
        Class arch = U.uaf(sr, "rs:arch", "重点区域反无人机资源体系", "ResourceArchitecture");
        dsr.at(arch, 700, 30, 260, 60);
        int x = 30;
        for (String[] r : RS) {
            dsr.at(U.get("rs:" + r[0]), x, 220, 170, 60);
            x += 185;
        }
        for (String[] r : RS) U.comp(arch, U.get("rs:" + r[0]), r[0].substring(2), r[2]);

        // Rs-Cn 连通
        D dcn = new D("Resources Connectivity", cn, "资源连通", 1500);
        String[] ord = {"r_radar", "r_esm", "r_eo", "r_aco", "r_edge", "r_c2", "r_gun"};
        int[][] pos = {{30, 30}, {30, 150}, {30, 270}, {30, 390}, {400, 210}, {770, 210}, {1100, 210}};
        for (int i = 0; i < ord.length; i++) dcn.at(U.get("rs:" + ord[i]), pos[i][0], pos[i][1], 220, 70);
        dcn.at(U.get("rs:r_cp"), 770, 420, 220, 70);
        dcn.at(U.get("rs:r_net"), 400, 420, 220, 70);
        dcn.at(U.get("rs:r_jam"), 1100, 330, 220, 70);
        dcn.at(U.get("rs:r_uav"), 1100, 450, 220, 70);
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
            U.reg("rf:" + f[0], fl);
            U.apply(fl, "UAF", "ResourceExchange");
            dcn.link(fl, U.get("rs:" + f[1]), U.get("rs:" + f[2]));
        }

        // Rs-Pr：各资源的系统功能 (Function)，同时记录功能 → 作战活动
        D dpr = new D("Resources Processes", pr, "系统功能", 1700);
        Map<String, String> fnOa = new LinkedHashMap<>();
        for (String[] r : RS) {
            int n = 0;
            for (String fo : r[3].split("\\|")) {
                String[] kv = fo.split("=");
                com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity a =
                    U.put(U.F.createActivityInstance(), pr);
                a.setName(r[1] + "：" + kv[0]);
                U.apply(a, "UAF", "Function");
                String fk = "fn:" + r[0] + ":" + (n++);
                U.reg(fk, a);
                fnOa.put(fk, kv[1]);
                dpr.add(a, 230, 60);
            }
        }

        // Rs-Tr：资源 -IsCapableToPerform-> 功能 -Implements-> 作战活动；资源 -Exhibits-> 能力
        D dt1 = new D("Resources to Operational Activities Mapping Matrix", tr, "资源与作战活动映射", 1500);
        D dt2 = new D("Functions to Operational Activities Mapping Matrix", tr, "系统功能与作战活动映射", 1500);
        int nImpl = 0;
        for (Map.Entry<String, String> en : fnOa.entrySet()) {
            String rk = en.getKey().split(":")[1];
            Element fn = U.get(en.getKey());
            if (en.getValue() == null) {
                U.log("警告: 功能未映射到作战活动 " + en.getKey());
                continue;
            }
            U.abs(U.get("rs:" + rk), fn, tr, "UAF", "IsCapableToPerform");
            U.abs(fn, U.get("oa:" + en.getValue()), tr, "UAF", "Implements");
            nImpl++;
        }
        for (String[] c : RS_CAP) U.abs(U.get("rs:" + c[0]), U.get("cap:" + c[1]), tr, "UAF", "Exhibits");
        U.log("B4 Rs-Tr: Implements " + nImpl + "，资源 Exhibits " + RS_CAP.length);
        U.log("B4.resources 完成: 资源 " + RS.length + "，接口 " + RS_FLOW.length);
    }
}



