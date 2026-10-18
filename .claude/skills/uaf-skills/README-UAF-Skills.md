# UAF 1.2 建模 Skills 套件

把设计文档转成 MagicDraw 2022x（UAF 1.2 插件）可直接打开的 `.mdzip` 工程。

## 包含的 Skills

| 目录 | 作用 |
|---|---|
| `uaf-model-from-doc` | 总流程入口（读文档 → 抽取规格 → 生成 → 后处理 → 验证 → 交付） |
| `uaf-spec-extraction` | 文档内容映射为 UAF 元素/构造型/视图，附关系方向（含 Rs-Tr 三段链）与视图类型名参考 |
| `uaf-mdzip-codegen` | 用 MagicDraw OpenAPI 无界面批量生成工程；含 Java 模板、矩阵后处理、PowerShell 启动脚本和案例 |
| `uaf-mdzip-verify` | 在全新进程中重新打开工程；对依赖矩阵和表格视图做配置与数据一致性核对 |

## 安装

把四个 `uaf-*` 目录复制到 Claude Code 的 skills 目录：

- 所有项目可用：`%USERPROFILE%\.claude\skills\`
- 仅某项目可用：`<项目>\.claude\skills\`

重启 Claude Code 后生效。使用时直接说：“根据这份文档生成 UAF 模型”。

## 使用前提

1. 已安装 MagicDraw 2022x，带 UAF 1.2 插件和 `templates\UAF\UAF template.mdzip`。
2. MagicDraw 许可有效（许可失败时无界面模式无法启动）。
3. 系统有 JDK 11 或更高版本、Python 3（读取 docx）。
4. Windows + PowerShell（脚本为 `.ps1`，必须是 UTF-8 带 BOM）。
5. MagicDraw 不在 `D:\Program Files\MagicDraw` 时，先设置环境变量 `MD_HOME`。

## 生成流程（三步）

```
go.ps1                   编译并生成 out.mdzip
post.ps1                 设置依赖矩阵范围（独立进程，必须单独做）
run.ps1 -Class Verify    全新进程验证
```

## 配套 Skill

`uaf12-scenario-analysis`(与本套件同级目录)负责写分析报告和建模方案,含任务工程扩展(`references/mission-engineering.md`)。典型分工:先用它写报告并与用户确认假设,再用本套件把报告落成 `.mdzip`。

## 重要经验

- 表格、矩阵类视图**不是天然为空**：必须配置范围/行元素，且模型里要有匹配其准则的关系，否则在 MagicDraw 里打开就是空的。
- 依赖矩阵插件 jar 不能放进生成器的类路径，否则 UAF 视图类型注册失败；相关逻辑放在独立进程 `PostMatrix`。
- 无界面模式无法计算矩阵内容（官方示例同样如此），只能核对配置与数据；矩阵在界面中的显示需要在 GUI 里确认。

- UAF 1.2 没有 Mission*、ForceDesignation 等 1.3 元素;用自建 Profile 的构造型(`U.customStereotype`),必须挂在自建 Profile 下才会保存。
- 在已有项目上加内容先做“增量规格”:列出已有 / 新增 / 重复,别另造同义元素。派生视图(端到端连接、覆盖表)复用已有元素。
- 缺失的装备性能用参数代号表示,不编数值。
- 模型不手改,改常量表后整体重跑;文档领先模型时写明“模型待同步”。
- 含中文的脚本写成 UTF-8 文件再运行,别在 bash 里用 heredoc 传给 Python。
- 视图类型名、构造型是否存在,用探针实测,不要凭插件字符串猜(带 `Pm-Me` 的显示名不能用于 `createDiagram`)。

## 已知限制

- 仅在 MagicDraw 2022x + UAF 1.2 上验证；其他版本的构造型名称可能不同。
- 组合关联无法用 OpenAPI 在图里连线(三种方式均失败),只能留在模型里,需在界面里拖入。
- 矩阵、表格在界面中的实际显示、版面美观度未在 GUI 中确认。度量表(`Typical Measurements Table`)和自定义追溯矩阵同样未在界面中确认。
- 未实践的部分：Internal Connectivity（内部连通）、Security、Projects、Standards 视点。

## 更新记录

- 修正早期版本的误判：把"表格/矩阵类视图为空"当成正常现象。
- 新增 `post/PostMatrix.java`、`run.ps1`/`go.ps1`/`post.ps1`，取代 `.bat`/`.sh`。
- 补充 Rs-Tr 三段链、Satisfy 端点、表格视图 `rowElements`/`scope` 配置、矩阵准则对应表。
- 新增任务工程链支持:`examples/B6.java`(OA→MT 追溯、资源泳道流程、端到端连接图、度量)。
- `PostMatrix` 改为按视图名登记的 `CUSTOM` 表,支持任意“行类型 × 列类型 + 关系”的自定义矩阵。
- 模板同步:`D.java` 补 `dropAuto`;`U.java` 补 `customStereotype`;`Gen.java` 登记 B6。
- 补充经验:自定义构造型、资源流程图、度量、探针查 API、Windows 编码、迭代重跑、图里画不出的组合关联。
