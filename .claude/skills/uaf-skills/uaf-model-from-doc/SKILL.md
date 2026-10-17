---
name: uaf-model-from-doc
description: 把一份设计/分析文档（docx、md 等）转成符合 MagicDraw 2022x UAF 1.2 规范、可直接打开的 .mdzip 工程。当用户要求"根据设计文档生成 UAF 模型/视点/视图/MagicDraw 工程"时使用。本 Skill 是总流程，会依次调用 uaf-spec-extraction、uaf-mdzip-codegen、uaf-mdzip-verify。
---

# 从设计文档生成 UAF 1.2 的 MagicDraw 工程

## 适用场景与前置条件

- 输入：一份描述体系/场景的设计文档，里面应有能力、作战活动、执行者、服务、资源、需求等内容，最好带有 UAF 视图规划。
- 输出：一个 `.mdzip`，MagicDraw 2022x 加载 UAF 1.2 插件后可直接打开，视点包、视图、构造型都正确。
- 环境检查（先做，缺任何一项就停下来告诉用户）：
  1. MagicDraw 2022x 已安装，目录下有 `plugins/com.nomagic.uaf`、`templates/UAF/UAF template.mdzip`。
  2. 许可可用。判断方法：先按 `uaf-mdzip-verify` 的探针启动一次无界面进程；日志出现 `License validation failed` 说明许可有问题，让用户自己解决，不要尝试绕过。
  3. 系统有 JDK（任意 11 及以上，编译时用 `--release 11`），Windows + PowerShell 5 及以上。
  4. 若 MagicDraw 不在 `D:\Program Files\MagicDraw`，先设环境变量 `MD_HOME`。

## 工作流（按顺序执行，不要跳步）

1. **读文档**：运行本目录的 `read_docx.py`（`PYTHONUTF8=1 python read_docx.py 文档.docx 临时.txt`），再读临时文本。表格单元格是逐行输出的，按列数还原成表。临时文本用完删除。
2. **抽取规格** → 使用 `uaf-spec-extraction`：产出一份"建模规格表"，把文档中的每个对象映射到具体 UAF 元素类型与目标视图。
3. **和用户确认关键假设**：规格表里凡是文档没写清、需要你替用户做决定的点（数量、方向、哪个构造型），列出来，说明你采取的默认读法。能合理默认就直接做，并在最终回复里列出，不要停下来逐项询问。
4. **生成代码并运行** → 使用 `uaf-mdzip-codegen`：复制模板，按规格写 `B1..Bn`，运行 `go.ps1` 生成，再运行 `post.ps1` 做矩阵后处理，读 `gen.log` 与 `post.log`。
5. **独立验证** → 使用 `uaf-mdzip-verify`：全新进程重新打开工程，逐个打开视图，统计构造型；对依赖矩阵和表格视图核对"配置 + 关系数据"，对照规格表核对数量。
6. **交付**：把 `.mdzip` 复制到用户项目根目录；保留生成器源码目录（便于重跑）；写/更新项目 `CLAUDE.md`；清理临时目录。最终回复要如实说明：已验证什么、没有验证什么（例如没有在 GUI 里看版面，矩阵内容未在界面中确认）、哪些视图/视点未建。
7. **用户反馈"某些图是空的"时**：先别当成正常现象。对照 `uaf-mdzip-codegen` 的"表格与矩阵类视图"一节排查：范围标志、行元素、模型里是否有匹配准则的关系、视图类型是否合用。

## 总原则

- **用官方 OpenAPI 生成，不要手写 mdzip 内部 XML**。手写 XML 极易因构造型 ID、快照、二进制图内容不一致而无法打开；OpenAPI 保存出来的文件一定能被同版本 MagicDraw 打开。
- **以官方 UAF 模板为底板**，不要从空白工程起步。模板已带全部视点包、Profile 引用与项目依赖。
- **表格、矩阵类视图不是"天然为空"**：它们必须配置范围/行元素，并且模型里要有匹配其准则的关系，否则在 MagicDraw 里打开就是空的。
- **所有建模放进同一个 Session**，失败就 `cancelSession`，避免写出半成品。
- **每一步都要以日志为准**：构造型找不到只会记"警告"不会抛错，必须检查 `gen.log`。
- 文档写的与 UAF 规范冲突时（如把"需求"当视点），按规范处理并告诉用户；文档写得含糊时，选最可能的读法并在结果里说明。
- 用户全局偏好为中文：对话、注释、日志、元素名称都用中文。

## 子 Skill

- `uaf-spec-extraction`：文档 → 建模规格表（含视点/视图映射）。
- `uaf-mdzip-codegen`：规格表 → Java 生成器 → `.mdzip`。
- `uaf-mdzip-verify`：独立验证与故障排查。
