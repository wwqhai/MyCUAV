# 重点区域反无人机 UAF 建模项目

## 文件
- `CUAV提示词示例.md`：场景输入。
- `重点区域反无人机作战场景设计分析报告.md`：UAF 1.2 分析报告（草案 V0.1）。
- `重点区域反无人机.mdzip`：由生成器产出的 MagicDraw 2022x（UAF 1.2）工程。
- `生成器/`：模型生成器源码（B1 概述/能力，B2 作战执行者/信息交换，B3 作战活动/流程，B4 服务/人员/资源，B5 需求/实际资源/约束/状态/时序）。

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
- 报告与模型保持一致：能力 C1~C9，活动 OA1~OA17，信息交换 IE1~IE13，服务 S1~S10，需求 R1~R14。
- `D.java` 的 `dropAuto()` 用于清理创建行为类视图时自动产生的空元素，新增活动图/状态图/顺序图时需调用。
- `能力与需求追溯` 视图名不能改，`PostMatrix` 按此名称重配矩阵。
