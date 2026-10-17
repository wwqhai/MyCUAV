# 通用启动器：以 MagicDraw 自带 JRE 运行无界面批处理类（继承 CommandLine）。
# 用法: powershell -NoProfile -ExecutionPolicy Bypass -File run.ps1 -Class <类名> [-In <工程>] [-Out <输出>] [-Log <日志名>]
# 为什么用 PowerShell 而不是 .bat：含中文的目录下 .bat 会因代码页问题失败。本文件需保存为 UTF-8 带 BOM。
# 只把 MagicDraw 的 lib 放进类路径。依赖矩阵等插件 jar 不能放进来，否则 UAF 视图类型注册失败（Unknown diagram type）。
param(
    [Parameter(Mandatory = $true)][string]$Class,
    [string]$In = "",
    [string]$Out = "",
    [string]$Log = "run.log",
    [string]$Only = "",
    [string]$Template = "UAF template.mdzip"
)
$MD = if ($env:MD_HOME) { $env:MD_HOME } else { "D:\Program Files\MagicDraw" }   # MagicDraw 安装目录，可用环境变量 MD_HOME 覆盖
$GEN = $PSScriptRoot
$logPath = Join-Path $GEN $Log
Remove-Item $logPath -ErrorAction SilentlyContinue
$cp = "$MD\lib\classpath.jar;$MD\lib\*;$GEN\out"
$jargs = @("-Xmx3g", "-Desi.system.config=data/application.conf", "-DLOCALCONFIG=true",
    "--illegal-access=permit", "-Dfile.encoding=UTF-8",
    "-Dgen.in=$MD\templates\UAF\$Template", "-Dgen.out=$Out", "-Dgen.log=$logPath", "-Dgen.only=$Only",
    "-Dver.in=$In", "-Dver.log=$logPath", "-cp", $cp, $Class)
Push-Location $MD
& "$MD\jre\bin\java.exe" @jargs *> (Join-Path $GEN "run.stdout")
Pop-Location
if (Test-Path $logPath) { Get-Content $logPath -Encoding UTF8 } else { "未生成日志，见 run.stdout 与 MagicDraw 日志 %LOCALAPPDATA%\.magicdraw\2022x\magicdraw.log" }
