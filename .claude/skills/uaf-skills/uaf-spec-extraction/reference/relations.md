# UAF 1.2 关系方向速查（已在官方示例工程 UAF sample.mdzip 中核对）

Abstraction 的 client = 源（被指的那一方），supplier = 目标。`U.abs(client, supplier, 包, "UAF", 构造型名)`。

| 构造型 | UML 基类 | client（源） | supplier（目标） | 用于 |
|---|---|---|---|---|
| `Exhibits` | Abstraction | OperationalPerformer / Service / Organization / ActualResource / ResourceArchitecture | Capability | 执行者、服务展现能力 |
| `MapsToCapability` | Abstraction | OperationalActivity | Capability | 活动支撑能力 |
| `IsCapableToPerform` | Abstraction | OperationalPerformer / Post / **ResourceArtifact** / CapabilityConfiguration | OperationalActivity / **Function** | 谁能执行哪个活动或功能 |
| `Supports` | Abstraction | Service | OperationalActivity | 服务支持作战活动 |
| `Implements` | Abstraction | **Function** | **OperationalActivity** | 系统功能实现作战活动（Rs-Tr 的核心）；ResourceInterface→ServiceInterface 也用它 |
| `Phases` | Abstraction | ActualEnterprisePhase / ActualEnduringTask | Capability / Goal | 阶段与能力 |
| `Forecast` | Dependency | 同类型元素（新）→（旧） | 同类型 | 演进预测 |
| `Satisfy`（SysML） | Abstraction | 满足方（**Capability**/活动/资源，client） | **Requirement**（supplier） | 需求追溯；"能力×需求"矩阵以它为准则 |
| `OperationalExchange` | InformationFlow | source=执行者，target=执行者 | conveyed=InformationItem | 作战信息交换 |
| `ResourceExchange` | InformationFlow | 资源 | 资源 | 资源接口流 |
| `ServiceExchange` | InformationFlow | 服务 | 服务 | 服务调用 |

## 其它写法要点

- 能力依赖没有专用构造型，用普通 `Dependency` 即可。
- 组合/数量：`U.comp(整体, 部件, 角色名, 多重性)`，多重性文本如 `"3"`、`"1..*"`。
- 角色类构造型（`OperationalRole`、`ResourceRole`、`ServiceRole`）用于部件属性（Part），内部连通图（Internal Connectivity）才需要；仅做类级连通时用 InformationFlow 即可。
- 实际资源：`InstanceSpecification` + `ActualResource`，`getClassifier().add(资源类型)`。
- 需求的 Id/Text 是 `SysML::Requirement` 的标记值，用 `U.tag(q, "SysML", "Requirement", "Id", ...)` 设置。

## 资源追溯链（Rs-Tr，必须按此建，否则相关矩阵为空）

官方 UAF 示例的做法是三段链，不要把 `Implements` 建成"资源→执行者"：

```
ResourceArtifact --IsCapableToPerform--> Function --Implements--> OperationalActivity
ResourceArtifact --Exhibits--> Capability
```

- "系统功能与作战活动映射"矩阵：行 Function、列 OperationalActivity、准则 `Implements`。
- "资源与作战活动映射"矩阵：行 Resource、列 OperationalActivity，准则是内联表达式，靠上面这条链推导。
- 每个 Function 必须映射到一个作战活动；映射不上的功能要记警告，不要悄悄丢弃。

## 建关系前先对照矩阵

每张要出现的 UAF 矩阵都有固定的"行类型、列类型、准则"。规划规格表时先查 `uaf-mdzip-codegen` 里的矩阵对应表，确保模型里有匹配的关系；关系方向建反或构造型建错，矩阵就是空的。
