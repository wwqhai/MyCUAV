# 重点区域反无人机 UAF 1.2 建模项目

本项目用自制的两套 Claude Code Skills，把一段作战场景描述变成两样成果：

1. 符合 UAF 1.2 标准的《作战场景设计分析报告》（Markdown）。
2. 可在 MagicDraw 2022x（UAF 1.2 插件）中直接打开的 `.mdzip` 工程。

示例场景为“武器仓库重点区域反无人机”。这两套 Skills 与场景无关，可复用到其他业务场景的分析文档编制和 UAF 模型创建。

## 项目背景

- 场景：保护 500 m × 500 m 的武器仓库，防护圈由内向外布置雷达与电侦、光电、声学阵列和拦截处置手段（单兵发射枪、干扰机、拦截无人机），另设 1 处指挥所；敌方为 1 架 FPV 无人机和 3 枚巡飞弹。
- 方法：UAF 1.2（统一架构框架），从能力、作战、服务、资源到实际资源建立可追溯的架构链。
- 环境：Claude Code + MagicDraw 2022x。换用其他环境或工具时，Skills 可能需要做些微调。

## 目录结构

```
MyCUAV/
├── CLAUDE.md                              项目约定（Claude Code 自动加载）
├── CUAV提示词示例.md                       场景输入（装备参数、敌方、作战流程）
├── 重点区域反无人机作战场景设计分析报告.md     UAF 1.2 分析报告（草案 V0.2）
├── D0-核心思路.md ~ D7-…md                  任务工程（ME）设计说明，开头写明状态
├── 重点区域反无人机.mdzip                   生成的 MagicDraw 工程（V0.2）
├── 生成器/                                 模型生成器
│   ├── src/                               Java 源码（Gen、D、U、Verify、B1~B6）
│   ├── post/PostMatrix.java               依赖矩阵后处理
│   └── go.ps1 / post.ps1 / run.ps1        编译、后处理、运行脚本
└── .claude/skills/
    ├── uaf12-scenario-analysis/           Skill 1：场景分析与报告
    └── uaf-skills/                        Skill 2：文档到 .mdzip 工程
```

生成器源码按 UAF 视点分块：

| 文件 | 内容 |
| ---- | ---- |
| `B1` | 概述、能力 |
| `B2` | 作战执行者、信息交换 |
| `B3` | 作战活动、流程 |
| `B4` | 服务、人员、资源 |
| `B5` | 需求、实际资源、约束、状态、时序 |
| `B6` | 任务工程链：OA→MT 追溯、MET 资源流程、端到端连接图、度量（MOE/MOP/TPM） |
| `D` / `U` | 公共辅助（含 `dropAuto()`） |
| `Gen` / `Verify` | 生成入口 / 验证入口 |

## 两套 Skills

### uaf12-scenario-analysis

输入装备参数、敌我双方和作战流程，输出结构化的分析报告和 MagicDraw 建模方案，并含任务工程（MT/MET、MOE/MOP）扩展和变更管理。流程分阶段推进，每个阶段有完成标准，保证追溯链不断。`references/` 下有场景采集、视图命名、报告模板和 MagicDraw 操作流程。`example-regional-air-defense.md` 是区域防空的示例。

### uaf-skills

由四个子 Skill 组成：

| 子 Skill | 作用 |
| -------- | ---- |
| `uaf-model-from-doc` | 总流程入口：读文档、抽取规格、生成、后处理、验证、交付 |
| `uaf-spec-extraction` | 把文档内容映射为 UAF 元素、构造型和视图，含关系方向与视图类型名参考 |
| `uaf-mdzip-codegen` | 用 MagicDraw OpenAPI 无界面批量生成工程，含 Java 模板和启动脚本 |
| `uaf-mdzip-verify` | 在全新进程中重新打开工程，核对矩阵和表格视图的配置与数据 |

更多说明见 `.claude/skills/uaf-skills/README-UAF-Skills.md`。

### 安装

把 Skills 目录复制到 Claude Code 的 skills 目录，重启后生效：

- 所有项目可用：`%USERPROFILE%\.claude\skills\`
- 仅当前项目可用：`<项目>\.claude\skills\`（本项目已是这种方式）

## 使用前提

- Windows + PowerShell。`.ps1` 必须保存为 UTF-8 带 BOM。
- MagicDraw 2022x，带 UAF 1.2 插件和 `templates\UAF\UAF template.mdzip`，许可有效（许可失败时无界面模式无法启动）。
- JDK 11 或更高版本；读取 docx 需要 Python 3。
- MagicDraw 不在 `D:\Program Files\MagicDraw` 时，先设置环境变量 `MD_HOME`。

## 使用方法

### 一、生成分析报告和模型

在 Claude Code 中打开本目录，直接描述需求，例如：

```
根据 CUAV提示词示例.md 做 UAF 1.2 场景分析，并生成 MagicDraw 模型
```

也可以分两步：先让 `uaf12-scenario-analysis` 出报告，再让 `uaf-model-from-doc` 依据报告生成模型。

### 二、手动重新生成模型

在 `生成器/` 下依次执行，每步约 1~3 分钟：

```powershell
# 1. 编译并生成 out.mdzip
powershell -NoProfile -ExecutionPolicy Bypass -File go.ps1

# 2. 后处理：设置依赖矩阵范围（需独立进程）
powershell -NoProfile -ExecutionPolicy Bypass -File post.ps1 -In "<绝对路径>\out.mdzip" -Out "<绝对路径>\重点区域反无人机.mdzip"

# 3. 在全新进程中验证
powershell -NoProfile -ExecutionPolicy Bypass -File run.ps1 -Class Verify -In "<绝对路径>\重点区域反无人机.mdzip" -Log ver.log
```

修改报告中的数量或假设后，要同步修改对应 B 块的常量表，再重新运行。

## 建模约定

报告与模型保持一致：

| 类别 | 编号 |
| ---- | ---- |
| 能力 | C1~C11 |
| 作战执行者 | AC1~AC12 |
| 作战活动 | OA1~OA19 |
| 信息交换 | IE1~IE19 |
| 服务 | S1~S12 |
| 资源 | RS1~RS11 |
| 资源交换 | RF1~RF12 |
| 需求 | R1~R16 |
| 场景、想定、目标 | SC1、V1~V3、G1~G3 |
| 任务线程、任务 | TH1、MT1~MT5、MET1 |
| 度量 | MOE1~4、MOP1~10、TPM1~17 |

报告 V0.2 增补了干扰机 5 部、拦截无人机 10 架，其性能参数用代号 P_JAM_*、P_INT_* 表示，不设数值。任务工程链的设计见 `D0-核心思路.md` ~ `D7-追溯关系矩阵：战略—运营—资源.md`。

- 新增活动图、状态图或顺序图时，需调用 `D.java` 的 `dropAuto()`，清理创建行为类视图时自动产生的空元素。
- 视图名 `能力与需求追溯`、`作战活动与任务线程追溯`、`度量与目标追溯`、`度量与作战活动追溯`、`度量与资源追溯` 不能改，`PostMatrix` 按名称重配矩阵。

## 经验与已知限制

- 表格、矩阵类视图不会自动有内容。必须配置范围和行元素，且模型里要有匹配准则的关系，否则在 MagicDraw 中打开是空的。
- 依赖矩阵插件的 jar 不能放进生成器的类路径，否则 UAF 视图类型注册失败，所以矩阵逻辑放在独立进程 `PostMatrix` 中。
- 无界面模式无法计算矩阵内容，只能核对配置和数据。矩阵和表格在界面中的实际显示、版面效果，需要在 MagicDraw 里人工确认。
- 仅在 MagicDraw 2022x + UAF 1.2 上验证，其他版本的构造型名称可能不同。
- 尚未实践的视点：Internal Connectivity（内部连通）、Security、Projects、Standards。

## 迁移到其他场景

1. 用新的场景描述替换 `CUAV提示词示例.md`。
2. 用 `uaf12-scenario-analysis` 生成新报告。
3. 按新报告修改 `生成器/src/B1~B6.java` 中的常量表和编号。
4. 重新运行三步生成流程，并在 MagicDraw 中人工检查视图。
