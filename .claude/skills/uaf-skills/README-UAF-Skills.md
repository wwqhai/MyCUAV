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

## 重要经验

- 表格、矩阵类视图**不是天然为空**：必须配置范围/行元素，且模型里要有匹配其准则的关系，否则在 MagicDraw 里打开就是空的。
- 依赖矩阵插件 jar 不能放进生成器的类路径，否则 UAF 视图类型注册失败；相关逻辑放在独立进程 `PostMatrix`。
- 无界面模式无法计算矩阵内容（官方示例同样如此），只能核对配置与数据；矩阵在界面中的显示需要在 GUI 里确认。

## 已知限制

- 仅在 MagicDraw 2022x + UAF 1.2 上验证；其他版本的构造型名称可能不同。
- 矩阵、表格在界面中的实际显示、版面美观度未在 GUI 中确认。
- 未实践的部分：Internal Connectivity（内部连通）、Security、Projects、Standards 视点。

## 更新记录

- 修正早期版本的误判：把"表格/矩阵类视图为空"当成正常现象。
- 新增 `post/PostMatrix.java`、`run.ps1`/`go.ps1`/`post.ps1`，取代 `.bat`/`.sh`。
- 补充 Rs-Tr 三段链、Satisfy 端点、表格视图 `rowElements`/`scope` 配置、矩阵准则对应表。
