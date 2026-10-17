---
name: uaf-spec-extraction
description: 把设计文档的内容映射成 UAF 1.2 建模规格表（元素类型、构造型、关系方向、目标视点包与视图类型）。在生成 MagicDraw UAF 模型前使用，或需要判断"文档里这类内容在 UAF 里该建成什么"时使用。
---

# 文档 → UAF 建模规格表

目标：产出一份可直接指导写代码的规格，每一行是"文档中的某对象 → UAF 元素 + 构造型 + 所在包 + 出现在哪些视图"。

## 步骤

1. 通读文档，按下表的"识别线索"把内容归到视点。文档里若已有"视图规划"章节（如 Sm-Ov、Op-Sr…），以它为准；没有就按本表默认。
2. 为每类对象定 **key**（英文短标识，如 `cap:C1`、`op:radar`、`oa:OA3`、`rs:r_edge`、`sv:s1`、`req:R1`）。key 是后续各生成块互相引用元素的唯一手段，命名要稳定。
3. 为每个关系定方向与构造型（见 `reference/relations.md`）。
4. 列出每个视图要放哪些元素、怎么连线。
5. 单独列"假设与待确认"：数量、方向、缺失参数。能默认就默认，别阻塞。

## 视点与元素映射（UAF 1.2）

| 文档内容的线索 | 视点/包 | 元素与构造型 | 典型视图（createDiagram 的类型名） |
|---|---|---|---|
| 目的、范围、术语 | Summary & Overview；Architecture Management | 注释 + `Definition` 构造型的注释 | `Summary and Overview`；`Dictionary` |
| 能力表、能力依赖 | Strategy | 类 + `Capability`；依赖 | `Strategic Taxonomy`、`Strategic Structure` |
| 作战执行者/节点/角色 | Operational | 类 + `OperationalPerformer`（人员角色用 `Post`）；体系整体用 `OperationalArchitecture` | `Operational Taxonomy`、`Operational Structure` |
| 执行者间信息交换 | Operational | `InformationFlow` + `OperationalExchange`，携带 `InformationItem` | `Operational Connectivity` |
| 作战活动、流程、泳道 | Operational | 活动 + `OperationalActivity`；流程内用 `CallBehaviorAction` + `OperationalActivityAction` | `Operational Processes`、`Operational Process Flow` |
| 交互时序 | Operational | 交互 + 生命线 + 消息 | `Operational Sequences` |
| 目标/对象状态 | Operational | 状态机 + `OperationalStateDescription` | `Operational States` |
| 时延、阈值、距离约束 | Operational | 约束 + `OperationalConstraint` | `Operational Constraints` |
| 服务及其提供方/使用方 | Services | 类 + `Service`；调用用 `InformationFlow` + `ServiceExchange` | `Services Taxonomy/Structure/Connectivity` |
| 人员、岗位 | Personnel | `Organization`、`Post` | `Personnel Taxonomy/Structure` |
| 装备类型、数量、接口、系统功能 | Resources | 类 + `ResourceArtifact`（体系用 `ResourceArchitecture`）；接口流 `ResourceExchange`；功能 = 活动 + `Function` | `Resources Taxonomy/Structure/Connectivity/Processes` |
| 具体实例（3 部雷达…） | Actual Resources | 实例规格 + `ActualResource`，classifier 指向资源类型 | `Actual Resources Structure` |
| 需求 | 新建 `Requirements` 包（应用 `UAF Packages::Requirements`） | 类 + `SysML::Requirement`，设 `Id`、`Text` | 无独立视点，见追溯 |
| 追溯 | 各视点的 `*Traceability` 包 | 见 relations.md（尤其 Rs-Tr 三段链） | 各 `*Mapping Matrix`；矩阵需配置范围，见 codegen |

不在文档范围内的视点（Security、Projects、Standards 等）不要建，但要在最终回复里点明"未展开"。

## 产出格式（写成表格给后续步骤用）

```
## 元素
| key | 名称 | 元素类型 | 构造型(Profile::名) | 所在包 | 备注 |
## 关系
| 类型 | 源 key | 目标 key | 构造型 | 所在包 |
## 视图
| 视图类型名 | 图名 | 所在包 | 放置的 key | 连线 |
## 假设与待确认
```

## 判断要点

- 数量要体现在两处：结构视图的组合关联多重性（如 3、5），和实际资源的实例个数。
- "A 与 B 双向交换"建成两条方向相反的 InformationFlow，不要建一条。
- 同一信息内容被多条流携带时，复用同一个 `InformationItem`。
- 带分支/循环/并行的流程写清控制流与守卫（"否：继续跟踪"）。
- 阈值、坐标、时延上限这类未给出的量，建模为带"待确认"字样的约束或注释，不要编造数值。
- **每个要出现的矩阵，规格表里都要写明它的准则关系**（行类型 → 列类型、构造型、所在包），否则生成出来是空矩阵。表格视图要写明行元素从哪来。
- 视图类型名必须取自 UAF 插件实际支持的集合，见 `reference/view-types.md`；不确定时去示例工程核对（`samples/UAF/UAF sample.mdzip`）。

详细关系方向见 `reference/relations.md`，视图类型名见 `reference/view-types.md`。
