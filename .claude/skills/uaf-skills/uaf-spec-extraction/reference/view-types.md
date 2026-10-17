# UAF 1.2 视图类型名（`ModelElementsManager.createDiagram(type, owner)` 的 type 参数）

以下名称取自官方示例工程里 `DiagramRepresentationObject` 的 `type` 属性。创建后会自动带上对应的 `UAF_Products::*` 构造型。

## 按视点

- 概述：`Summary and Overview`；词典：`Dictionary`
- 战略 St：`Strategic Taxonomy`、`Strategic Taxonomy Table`、`Strategic Structure`、`Strategic Connectivity`、`Strategic Connectivity Matrix`、`Strategic Constraints Definition`、`Strategic Information`、`Strategic Phasing`、`Strategic States`、`Strategic Actual Deployment`、`Actual Strategic Phases Gantt Chart`、`Actual Strategic Phases to Capabilities Mapping Matrix`
- 作战 Op：`Operational Taxonomy`、`Operational Taxonomy Table`、`Operational High Level Taxonomy`、`Operational Structure`、`Operational Connectivity`、`Operational Connectivity Table`、`Operational Role-based Connectivity Table`、`Operational Internal Connectivity`（组合结构图）、`Operational Processes`、`Operational Process Flow`（活动图）、`Operational Sequences`（顺序图）、`Operational States`（状态机图）、`Operational Constraints`、`Operational Information`、`Operational Activities to Capabilities Mapping Matrix`、`Operational Performers to Capabilities Mapping Matrix`
- 服务 Sv：`Services Taxonomy`、`Services Taxonomy Table`、`Services Structure`、`Services Connectivity`、`Services Internal Connectivity`、`Services Processes`、`Services Process Flow`、`Services States`、`Services Constraints`、`Services to Capabilities Mapping Matrix`、`Services to Operational Activities Mapping Matrix`
- 人员 Ps：`Personnel Taxonomy`、`Personnel Structure`、`Personnel Connectivity`、`Personnel Processes`、`Personnel Process Flow`、`Personnel States`、`Personnel Sequences`、`Personnel Constraints`、`Personnel Forecast`、`Personnel Evolution`
- 资源 Rs：`Resources Taxonomy`、`Resources Structure`、`Resources Connectivity`、`Resources Internal Connectivity`、`Resources Processes`、`Resources Process Flow`、`Resources States`、`Resources Sequences`、`Resources Constraints`、`Resources Forecast`、`Resources Evolution`、`Resources to Operational Activities Mapping Matrix`、`Functions to Operational Activities Mapping Matrix`
- 实际资源 Ar：`Actual Resources Structure`、`Actual Resources Connectivity`
- 安全 Sc：`Security Taxonomy`、`Security Structure`、`Security Connectivity`、`Security Processes`、`Risks to Assets Mapping Matrix`、`Security Controls to Risks Mapping Matrix`
- 项目 Pj：`Projects Taxonomy`、`Projects Structure`、`Projects Connectivity`、`Projects Processes`、`Projects Roadmap`、`Actual Projects to Capabilities Mapping Matrix`、`Project Activities to Capabilities Mapping Matrix`
- 标准 Sd：`Standards Taxonomy`、`Standards Structure`、`Standards Roadmap`、`Standards Traceability`

名称不在表内时：去 `samples/UAF/UAF sample.mdzip` 的主模型里搜 `type='...' umlType=` 核对；也可从 `profiles/UAF_Customization.mdzip` 提取。

## 内容性质（决定要不要额外配置）

- **图形类**（放形状与连线）：Taxonomy、Structure、Connectivity、Processes、Process Flow、Sequences、States、Information。创建后用 `D.at/add/link` 放元素；建了不放元素就是空图。
- **依赖矩阵类**：`* Mapping Matrix`、`* Connectivity Matrix`。不放形状，但**必须**：
  1. `MatrixFilter` 的 `rowScopeDefined`/`columnScopeDefined` 为 true（用 PostMatrix 设置）；
  2. 模型里存在匹配其准则的关系（方向、构造型、所在包都要对）。
  任何一项缺失，打开都是空矩阵。
- **表格类**（`DiagramTable`）：`Dictionary`、`* Table`、`Operational Constraints`、`Services Constraints` 等。不放形状，但**必须**写 `rowElements` 和 `scope`/`additionalElements`，否则是空表。
- 无界面模式下无法计算矩阵和表格的内容，只能核对配置与数据，见 uaf-mdzip-verify。

## 不适用的默认视图类型

有些 UAF 视图类型的默认配置与你想表达的内容不符，创建后要重配：
- `Actual Strategic Phases to Capabilities Mapping Matrix`：默认 阶段→能力（`Phases`）。若要"能力×需求追溯"，需改为 行 Capability、列 Requirement、准则 `Satisfy`。

## 视点包（模板里已有，按名称取）

`Strategy`、`Operational`、`Services`、`Personnel`、`Resources`、`Security`、`Projects`、`Actual Resources`、`Standards`、`Summary & Overview`、`Architecture Management`。每个下面有 `* Taxonomy / Structure / Connectivity / Processes / States / Sequences / Constraints / Traceability / Information / Parameters / Roadmap / Motivation` 子包，用 `U.pkg("Operational", "Operational Structure")` 取。`Requirements` 包模板里没有，需自建。
