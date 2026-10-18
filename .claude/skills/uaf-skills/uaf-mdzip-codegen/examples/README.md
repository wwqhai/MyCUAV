# 示例：区域防空（B1~B5）

这是一次真实运行成功的案例，用来学习写法，不是通用代码。已按最终交付版本同步。

| 文件 | 内容 | 值得参考的写法 |
|---|---|---|
| B1 | 概述、词典、能力 | 分类图（泛化）、依赖图；**词典表格视图**用 `U.tableRows` + `U.tableTag("scope")` 配置行与范围 |
| B2 | 执行者、信息交换、Exhibits | 组合关联多重性、`InformationFlow` + `InformationItem`、同方向多条流只画一条线 |
| B3 | 作战活动与流程 | 泳道、Fork/Join/Decision/Merge、守卫、`MapsToCapability` |
| B4 | 服务、人员、资源 | 服务调用流、资源接口流；**Rs-Tr 三段链**（资源 IsCapableToPerform 功能、功能 Implements 活动）及未映射功能的警告 |
| B5 | 需求、实际资源、约束、状态机、顺序图 | `Requirements` 包自建、`Satisfy`；**约束表格视图**配行元素并设 `constrainedElement`；实例同时放进两张结构图 |

另见 `../templates/post/PostMatrix.java`：矩阵范围设置，以及把"能力×需求追溯"重配为 Capability × Requirement、准则 Satisfy。

写自己项目时：保持"key 登记 → 引用"的结构，把常量表（如 `CAPS`、`PERF`、`IE`）改成自己文档的内容。

## B6（反无人机项目，任务工程链）

B6 不属于区域防空案例，来自反无人机项目，依赖 B1（目标 `goal:`、任务 `mt:`）、B3（`oa:`）、B4（`rs:`、`fn:`、`rf:`）已登记的 key，必须在它们之后运行。

| 部分 | 内容 | 值得参考的写法 |
|---|---|---|
| `mtTrace` | OA→MT 的 Trace，并建一张固定名字的矩阵视图 | 矩阵视图名固定，由 PostMatrix 重配 |
| `measurements` | MOE/MOP/TPM 度量集、度量、追溯、典型度量表、三张度量矩阵 | `MeasurementSet`/`Measurement`；`Typical Measurements Table` 的行与范围 |
| `metFlows` | 资源泳道流程（`Resources Process Flow`） | 节点/边表 + `chain` + `escalation` 子图；动作引用已有功能 |
| `e2e` | 端到端连接派生图 | 复用已有 `InformationFlow`，只换布局 |
