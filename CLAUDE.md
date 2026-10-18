# 重点区域反无人机 UAF 建模项目

## 文件
- `CUAV提示词示例.md`：场景输入。
- `重点区域反无人机作战场景设计分析报告.md`：UAF 1.2 分析报告（草案 V0.2）。
- `重点区域反无人机.mdzip`：由生成器产出的 MagicDraw 2022x（UAF 1.2）工程。
- `D0-核心思路.md` ~ `D7-…md`：任务工程（ME）的设计说明，每个文件开头写明状态。
- `生成器/`：模型生成器源码（B1 概述/能力，B2 作战执行者/信息交换，B3 作战活动/流程，B4 服务/人员/资源，B5 需求/实际资源/约束/状态/时序，B6 任务工程链：OA→MT 追溯/MET 资源流程/端到端连接/度量）。

## 重新生成（在 `生成器/` 下，每步约 1~3 分钟）
```
powershell -NoProfile -ExecutionPolicy Bypass -File go.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File post.ps1 -In "<绝对路径>\out.mdzip" -Out "<绝对路径>\重点区域反无人机.mdzip"
powershell -NoProfile -ExecutionPolicy Bypass -File run.ps1 -Class Verify -In "<绝对路径>\重点区域反无人机.mdzip" -Log ver.log
```
- MagicDraw 不在 `D:\Program Files\MagicDraw` 时先设环境变量 `MD_HOME`。
- `.ps1` 必须保存为 UTF-8 带 BOM。
- 改报告里的数量、假设后，同步改对应的 B 块常量表再重跑。

## 约定
- 报告与模型保持一致（V0.2）：能力 C1~C11，执行者 AC1~AC12，活动 OA1~OA19，信息交换 IE1~IE19，服务 S1~S12，资源 RS1~RS11，资源交换 RF1~RF12，需求 R1~R16；场景 SC1、想定 V1~V3、目标 G1~G3、线程 TH1、任务 MT1~MT5、MET1；度量 MOE1~4、MOP1~10、TPM1~17。
- 任务工程链（OA→MT 追溯、资源泳道流程、端到端连接、度量）在 `B6.java`，设计见 `D0-核心思路.md` ~ `D7-追溯关系矩阵：战略—运营—资源.md`。
- 干扰机、拦截无人机性能参数用代号 P_JAM_*、P_INT_*（报告 6.3），不设数值。使用规则为升级链：干扰机 → 拦截无人机 → 发射枪（A14）。
- `D.java` 的 `dropAuto()` 用于清理创建行为类视图时自动产生的空元素，新增活动图/状态图/顺序图时需调用。
- 下列矩阵视图名不能改，`PostMatrix` 按名称重配行、列和准则：`能力与需求追溯`、`作战活动与任务线程追溯`、`度量与目标追溯`、`度量与作战活动追溯`、`度量与资源追溯`。新增矩阵时在 `PostMatrix.CUSTOM` 登记。
- St-Tx 场景图在模型里的名称是 `场景与目标分解（St-Tx）`。
- 含中文的脚本写成 UTF-8 文件再运行，不要在 bash 里用 heredoc 传给 Python。
