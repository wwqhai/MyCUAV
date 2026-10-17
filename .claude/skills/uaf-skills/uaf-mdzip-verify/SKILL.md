---
name: uaf-mdzip-verify
description: 验证生成的 UAF .mdzip 工程：在全新 MagicDraw 进程里打开、逐个打开视图、统计构造型，并对依赖矩阵和表格视图做配置与数据一致性核对；也用于排查"打不开/许可失败/视图为空"。生成工程后、交付前必须使用。
---

# 独立验证 UAF .mdzip

生成器自带的"保存后重开"只能证明同一进程能读回。交付前必须再用**全新进程**打开一次，并且对表格、矩阵类视图单独核对，因为它们在无界面模式下无法直接显示内容。

## 一、打开与统计

1. 复制 `templates/Verify.java` 到生成器 `src/`，用 `go.ps1` 一并编译。
2. 复制 `scripts/run.ps1`（与 codegen 的 `run.ps1` 相同）到生成器目录。
3. 运行：

```
powershell -NoProfile -ExecutionPolicy Bypass -File run.ps1 -Class Verify -In "D:\path\工程.mdzip" -Log ver.log
```

4. 读 `ver.log`：
   - `PROJECT <名>`、`ELEMENTS <n>`：打开成功。
   - `ST <构造型> <数量>`：与规格表逐项对比（Capability、OperationalActivity、Service、ActualResource、Requirement、Satisfy、Exhibits 等）。
   - `DIAGRAM <类型> | <名称> | shapes=<n>`。
   - 末行 `DIAGRAMS <n> BAD <m>`：BAD 必须为 0。

## 二、判读 shapes

`shapes` 只对**图形类**视图有意义。

| 视图类别 | shapes 应为 | 如何确认内容 |
|---|---|---|
| 图形类：Taxonomy、Structure、Connectivity、Processes、Process Flow、Sequences、States | > 0 | 看 shapes；为 0 回看 `gen.log` 的"无法在图中放置" |
| 依赖矩阵：`*Mapping Matrix` | 0 属正常，但**不代表内容正常** | 见第三节 |
| 表格：Dictionary、Constraints | 0 属正常，但**不代表内容正常** | 见第四节 |

**不要再说"表格/矩阵为空属正常"。** 那是早期版本的误判：这类图不放形状，但必须配置范围、行元素和匹配的关系，否则 GUI 里打开就是空的。

## 三、依赖矩阵的核对（无法在无界面里计算，只能核对配置 + 数据）

无界面模式下 `MatrixDataHelper.buildMatrix` 恒抛 `NullPointerException`（用官方示例工程测过，同样如此），所以不能用它判断矩阵是否有内容。改用静态核对：

1. 解开 mdzip，读主模型 `com.nomagic.magicdraw.uml_model.model`。
2. 对每个矩阵的 `<Dependency_Matrix_Profile:MatrixFilter>`：`rowScopeDefined='true'`、`columnScopeDefined='true'`；`<Dependency_Matrix_Profile:DependencyMatrix>`：`takeWholeModelAsScope='true'`。与官方示例的同类矩阵逐项一致。
3. 取矩阵的准则（`dependencyCriteria` 里的 `stereotype=` ID，或内联表达式名），统计模型里"源属于行类型且目标属于列类型"的该构造型关系数。**应有勾数必须大于 0。**
4. 内联表达式（如"资源与作战活动映射"）无法静态推算，改核对其依赖链是否存在：资源 `IsCapableToPerform` Function，Function `Implements` 活动。

没有 GUI 确认时，交付说明里要写明"矩阵内容未在界面中确认"。

## 四、表格视图的核对

对每个 `<MagicDraw_Profile:DiagramTable>`：必须有 `scope` 或 `additionalElements`，且 `rowElements` 数量等于预期行数。约束行还要检查 `constrainedElement` 非空。与官方示例对应类型（`Dictionary`、`Operational Constraints`）的属性集合对照。

## 五、其它静态检查（不需要启动 MagicDraw）

- 每个 UAF 视图都带正确的 `<UAF_Products:xxx … base_Diagram=…>`。
- 视图的 `streamContentID` 指向的 BINARY 文件中 `<mdElement elementClass='…'>` 的数量即形状与连线数。
- 关系元素有对应构造型应用：`<UAF:Exhibits …>`、`<sysml:Satisfy …>` 等。
- zip 完整性：`zipfile.ZipFile(...).testzip()` 为 None。

## 六、故障判读

| 现象 | 结论 |
|---|---|
| 图形类视图 shapes=0 | 元素类型不被该视图接受，或放置失败 |
| `ST` 缺某个预期构造型 | Profile 名写错，`gen.log` 应有"未找到构造型"警告 |
| BAD>0 | 该视图没有展示元素 |
| 启动即失败，`License validation failed` | 许可问题，让用户处理，不要绕过 |
| `sax parsing fail … must start and end within the same entity` | 被打开的 mdzip 损坏（例如拷贝不完整），换文件对比 |
| 矩阵 `buildMatrix` 抛 NPE | 无界面环境限制，不是模型问题 |

## 七、不能验证的部分（交付时必须如实说明）

- 版面美观度、连线走向：只能在 GUI 里看。
- 依赖矩阵和表格在界面中实际显示的内容。
- 报表（词典、约束）导出效果。

## 诊断小技巧

- MagicDraw 日志：`%LOCALAPPDATA%\.magicdraw\2022x\magicdraw.log`，用 `grep -a`。运行前记下行数，只看新增部分。
- 结束进程前**先确认是不是用户自己开着的 MagicDraw**：用 `Get-CimInstance Win32_Process | Where Name -match 'javaw|java'` 看启动时间和命令行，只结束自己启动的 PID，不要按进程名批量结束。
- 无界面进程若无输出，先看进程是否还在跑，再看日志末尾。
