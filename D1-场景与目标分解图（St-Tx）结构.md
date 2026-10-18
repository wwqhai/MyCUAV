# D1 场景与目标分解图（St-Tx）

> 状态：已落地。模型中的图名为“场景与目标分解（St-Tx）”，位于 `Model/Strategy/Strategic Taxonomy/`，类型 Strategic Taxonomy。

## 1 作用

St-Tx 在任务工程中做两件事：呈现场景目标；展示场景 → 想定片段 → 目标 → 任务线程 → 任务工程线程的分解与追溯。MT、MET 在这里只做引出，活动序列和资源交互分别在 Op-Pr、Rs-Pr 中展开。

## 2 结构

```
                SC1 重点区域反无人机防护  «StrategicPhase»
                          │ 组合
        ┌─────────────────┼─────────────────┐
   V1 单架次侦察渗透   V2 多批次集群突防   V3 伴随干扰的饱和攻击(扩展)
        │ trace            │ trace              │ trace
        ▼                  ▼                    ▼
   G1 探测告警时效性   G2 识别正确性      G3 拦截有效性与安全性   «EnterpriseGoal»
        ▲                  ▲                    ▲
        │ trace            │ trace              │ trace
   MT1 预警探测  MT2 目标识别与分类  MT3 威胁评估与决策授权  MT4 拦截/驱离行动  MT5 效果评估与再交战决策
        ▲
        │ trace（MET1 → MT1~MT5）
   MET1 基线反无人机方案  «MissionEngineeringThread»
```

## 3 元素与追溯

| 层级 | 编号 | 内容 | UAF 1.2 元素 |
| ---- | ---- | ---- | ------------ |
| 场景 | SC1 | 重点区域（武器仓库）反无人机防护 | StrategicPhase |
| 想定片段 | V1 | 单架次侦察渗透：FPV 1 架，低速低空，以侦察为主 | StrategicPhase |
| | V2 | 多批次集群突防：巡飞弹 3 枚，雷达难以探测 | StrategicPhase |
| | V3 | 伴随干扰的饱和攻击：多方向、强电磁干扰（扩展，原场景未涉及） | StrategicPhase |
| 目标 | G1 | 首次探测到生成告警 ≤ 30 s | EnterpriseGoal |
| | G2 | 目标类型识别正确率 ≥ 90% | EnterpriseGoal |
| | G3 | 拦截成功率 ≥ 85%，非目标损失事件 = 0 | EnterpriseGoal |
| 任务 | MT1~MT5 | 预警探测；目标识别与分类；威胁评估与决策授权；拦截/驱离行动；效果评估与再交战决策 | MissionTask（自定义） |
| 线程 | TH1 | 反无人机交战线程，组合 MT1~MT5，解决方案无关 | MissionThread（自定义） |
| 任务工程线程 | MET1 | 基线方案：探测资源、决策节点、拦截资源、评估节点 | MissionEngineeringThread（自定义） |

追溯（Trace，下层为 client）：V1→G1、G2；V2→G2、G3；V3→G1、G2、G3；MT1→G1；MT2→G2；MT3→G1、G3；MT4→G3；MT5→G3；MET1→MT1~MT5。

## 4 待确认

- G1~G3 的阈值来自本稿，原场景输入没有，记为报告的 A12。
- V3 及其电磁干扰条件不在原场景输入里。
- 图中未画 SC1 到 V1~V3 的组合线（D0 的 T2）。
- MET1 的“拦截资源”属性已更新为干扰机 → 拦截无人机 → 发射枪的升级链（A14）。
- MT1~MT5 各带一条注释，写明触发、输入、输出和条令规则（D2 的规格）。
