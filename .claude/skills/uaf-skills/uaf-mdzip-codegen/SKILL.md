---
name: uaf-mdzip-codegen
description: 用 MagicDraw 2022x OpenAPI 的无界面批处理模式，以官方 UAF 模板为底板，按建模规格生成 UAF 1.2 的 .mdzip 工程（元素、构造型、视图、关系），并用后处理步骤配置依赖矩阵与表格视图，使其打开后有内容。需要把规格表落地为可打开的工程，或要扩展/重跑已有生成器时使用。
---

# 规格表 → Java 生成器 → 后处理 → .mdzip

## 目录约定

在用户项目里建 `生成器/`（或自定名）：

```
生成器/
  src/            U.java D.java Gen.java B1.java … Bn.java Verify.java
  post/           PostMatrix.java        矩阵后处理（独立进程）
  run.ps1 go.ps1 post.ps1                启动脚本（UTF-8 带 BOM）
  out/ post_out/                         编译输出（可删）
```

把本 Skill 的 `templates/` 复制过去：`U.java`（工具）、`D.java`（视图封装）、`Gen.java`（入口，按需登记块）、`run.ps1`/`go.ps1`/`post.ps1`、`post/PostMatrix.java`。`examples/B1~B5.java` 是区域防空的真实案例，照写法写自己的块，**不要原样拷贝内容**。

## 一次完整生成（顺序固定）

```
powershell -NoProfile -ExecutionPolicy Bypass -File go.ps1                              # 1. 编译并生成 out.mdzip
powershell -NoProfile -ExecutionPolicy Bypass -File post.ps1 -In out.mdzip -Out 最终.mdzip   # 2. 矩阵后处理
powershell -NoProfile -ExecutionPolicy Bypass -File run.ps1 -Class Verify -In 最终.mdzip -Log ver.log   # 3. 验证（见 uaf-mdzip-verify）
```

MagicDraw 不在默认位置时，先设置环境变量 `MD_HOME`。每步约 1~3 分钟，后台等待，不要连续轮询。

## 运行方式（已踩坑验证）

- 入口类继承 `com.nomagic.magicdraw.commandline.CommandLine`，`main` 里 `new Gen().launch(args)`（抛受检异常，`main` 声明 `throws Exception`），逻辑写在 `protected byte execute()`。
- 用 MagicDraw 自带 JRE 11 启动，类路径 `lib\classpath.jar;lib\*;<out>`，工作目录设为安装目录，参数 `-Desi.system.config=data/application.conf -DLOCALCONFIG=true --illegal-access=permit -Dfile.encoding=UTF-8`。
- **不要加 `-Djava.awt.headless=true`**，否则 `NMAction` 初始化抛 `HeadlessException`。
- **不要把依赖矩阵等插件 jar 放进 Gen 的类路径**，否则 UAF 视图类型注册失败，报 `Unknown diagram type: Dictionary`。需要用到依赖矩阵 API 的逻辑（设置矩阵范围）放进独立进程 `PostMatrix`。
- 用 `.ps1` 启动，不要用 `.bat`：目录名含中文时 `.bat` 会因代码页问题静默失败；`.ps1` 必须是 UTF-8 **带 BOM**，否则 PowerShell 5 按 GBK 解析会报语法错。
- `launch` 之后 `System.out` 被 MagicDraw 接管，写到 `%LOCALAPPDATA%\.magicdraw\2022x\magicdraw.log`，所以结果另写文件（`-Dgen.log`），`Gen` 在 finally 里写出 `U.LOG`。
- 编译用系统 JDK：`javac --release 11 -encoding UTF-8 -proc:none -cp "<MD>/lib/*" -d out src/*.java`。

## Gen 的流程

加载 `templates\UAF\UAF template.mdzip` → `U.init()` → `createSession` → 依次执行各块 → `closeSession`（异常则 `cancelSession`）→ `p.setName(...)` → `saveProject` → 关闭并重新 `loadProject` 验证能打开。

## 写生成块（B 块）的规范

- 一块对应一个视点或一组强相关视图，只做 `static void run()`。
- 元素创建统一走 `U.uaf(包, key, 名称, 构造型)` / `U.cls(...)`，立即以 key 登记；后面用 `U.get("key")` 取。块之间只通过 key 通信，顺序依赖体现在 `Gen` 的块顺序里。
- 构造型用 `U.apply(el, "UAF", "Capability")`。Profile 名是显示名：`UAF`、`SysML`、`MagicDraw Profile`、`UAF Packages`（含空格，不是 `UAF_Packages`）。
- 图形类视图：`D d = new D("<视图类型名>", 所在包, 图名, 宽度)`；`d.at(el, x, y, w, h)` 定位，`d.add(el, w, h)` 自动换行；`d.link(关系元素, 源, 目标)` 画线。先建关系元素再 `link`。
- 活动图/状态图/顺序图：先创建 Activity/StateMachine/Interaction，再 `new D(...)`，并 `d.dpe.getDiagram().setOwner(行为元素)`。
- 泳道：`U.V.createSwimlane(分区列表, new ArrayList<ActivityPartition>(), d.dpe)`，第二参数**不能传 null**。
- 流程控制节点用 Initial/Fork/Join/Decision/Merge/ActivityFinal，守卫写入 `ControlFlow.setGuard(LiteralString)`。
- 顺序图：生命线形状用 `createShapeElement`，消息用 `createSequenceMessage(msg, MessageSortEnum.SYNCHCALL, from, to, false, 0, 上一条, 25)`。
- 需求包：模板没有 `Requirements` 包，自建 `Package` 并 `apply "UAF Packages" "Requirements"`。

## 表格与矩阵类视图：必须配置，否则打开是空的

这类视图不放形状，内容由 MagicDraw 在 GUI 里计算。**不配置就是空图**，这是最容易犯的错（本套件早期版本曾误以为"空是正常的"）。

### 依赖矩阵（`*Mapping Matrix`、`*Connectivity Matrix`）

1. 创建时 UAF 已带入行/列元素类型和依赖准则，无需手写。
2. **必须**把 `MatrixFilter` 的 `rowScopeDefined`、`columnScopeDefined` 设为 true，并把范围设为整个模型。官方示例中的矩阵全部如此。这在 `post/PostMatrix.java` 里用 `PersistenceManager` 完成，运行 `post.ps1`。
3. **模型里必须有匹配准则的关系**，矩阵才有勾。关系要放在对应的 `*Traceability` 包。已核对的对应关系：

| 矩阵 | 行 | 列 | 准则（关系构造型） |
|---|---|---|---|
| 作战活动与能力映射 | OperationalActivity | Capability | `MapsToCapability` |
| 作战执行者与能力映射 | OperationalPerformer 等 | Capability | `Exhibits` |
| 服务与能力映射 | Service | Capability | `Exhibits` |
| 服务与作战活动映射 | Service | OperationalActivity | `Supports` |
| 系统功能与作战活动映射 | Function | OperationalActivity | `Implements`（Function→活动） |
| 资源与作战活动映射 | Resource | OperationalActivity | 内联表达式：资源 `IsCapableToPerform` Function，Function `Implements` 活动 |

4. 默认视图类型不合用时要重配。例：`Actual Strategic Phases to Capabilities Mapping Matrix` 的准则是 `Phases`（阶段→能力），不能用来做"能力×需求"。`PostMatrix.reconfigCapReq` 示范了怎么改：`setElementTypes` 设行列类型，`setDependencyCriteria` 写入准则 XML（stereotype 属性为 `SysML::Satisfy` 的 ID）。
5. 无界面模式下无法计算矩阵（`MatrixDataHelper.buildMatrix` 恒抛 NPE，官方示例同样），所以只能核对"配置 + 关系数据"，见 uaf-mdzip-verify。

### 表格视图（`Dictionary`、`Operational Constraints` 等 `DiagramTable`）

- 要写 `rowElements`（行元素）以及 `scope`（范围）或 `additionalElements`，用 `U.tableRows(diagram, 元素集合)` 和 `U.tableTag(diagram, "scope", 根模型)`。官方词典用 `scope`=根模型，官方约束表用 `additionalElements`+`rowElements`。
- 约束行元素是 `Constraint`，需有 `constrainedElement` 指向被约束元素。
- 词典的行是术语元素（带 `Definition` 注释），范围设为根模型。

### 图形类视图为空

建了图却没放元素就是空图（例如误建的重复结构图）。要么放元素，要么不建。

## 包路径（import）

- `Abstraction`、`Dependency`：`com.nomagic.uml2.ext.magicdraw.classes.mddependencies`
- `InformationFlow`、`InformationItem`：`...auxiliaryconstructs.mdinformationflows`
- `CallBehaviorAction`：`...actions.mdbasicactions`
- Activity 节点/边：`...activities.mdbasicactivities`（InitialNode、ControlFlow…）、`...mdintermediateactivities`（Fork/Join/Decision/Merge、ActivityPartition）、`...mdfundamentalactivities`（Activity、ActivityNode）
- 状态机：`...statemachines.mdbehaviorstatemachines`
- 基础类（Class、Package、Comment、Generalization、Association、Constraint、InstanceSpecification、Slot、LiteralString、OpaqueExpression）：`...classes.mdkernel`
- 依赖矩阵：`com.nomagic.magicdraw.dependencymatrix.persistence.PersistenceManager`（仅 PostMatrix 使用）
- 找不到类时：`unzip -l "<MD>/lib/md_api.jar" | grep "/类名.class"`；插件 API 看 `plugins/<插件>/*_api.jar`，用 `javap -public` 看签名。

## 常见故障

| 现象 | 原因与处理 |
|---|---|
| 进程挂住、日志为空 | 看 `magicdraw.log` 末尾；`License validation failed` = 许可问题，请用户处理 |
| `ExceptionInInitializerError … HeadlessException` | 去掉 headless 参数 |
| `Unknown diagram type: …` | 类路径里混入了插件 jar；或视图类型名拼错（见 spec-extraction 的 view-types） |
| `ClassNotFoundException: Gen` | 编译失败被吞，单独跑 javac 看错误 |
| `.ps1` 报"表达式中包含意外标记" | 文件缺少 UTF-8 BOM |
| `gen.log` 有"未找到构造型" | Profile 名写错，查 `StereotypesHelper.getAllProfiles` 的显示名 |
| 图形类视图 shapes=0 | 查"无法在图中放置"警告（元素类型不被该视图接受） |
| 矩阵/表格打开是空的 | 见上方"表格与矩阵类视图"：范围标志、行元素、匹配的关系三者缺一不可 |
| 泳道 NPE | 第二参数用空列表 |
| 误动了用户正在用的 MagicDraw | 结束进程前先看 `Get-CimInstance Win32_Process` 的启动时间与命令行，只结束自己启动的 PID |

## 清理

交付前删除探针与诊断类、`*.bak`、临时目录；保留 `src/`、`post/` 与三个 `.ps1`。
