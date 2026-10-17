# 后处理：设置依赖矩阵的行/列范围（并按需重配个别矩阵）。
# 用法: powershell -NoProfile -ExecutionPolicy Bypass -File post.ps1 -In 输入.mdzip -Out 输出.mdzip
# 必须在独立进程里做：依赖矩阵插件 jar 在类路径上会让 Gen 里的 UAF 视图类型失效。
param([Parameter(Mandatory=$true)][string]$In, [Parameter(Mandatory=$true)][string]$Out)
$MD = if ($env:MD_HOME) { $env:MD_HOME } else { "D:\Program Files\MagicDraw" }; $GEN = $PSScriptRoot
New-Item -ItemType Directory -Force "$GEN\post_out" | Out-Null
& javac --release 11 -encoding UTF-8 -proc:none -cp "$MD\lib\*;$MD\plugins\com.nomagic.magicdraw.dependencymatrix\*" -d "$GEN\post_out" "$GEN\post\PostMatrix.java" 2>&1 | Out-String | Write-Host
$log = Join-Path $GEN "post.log"; Remove-Item $log -ErrorAction SilentlyContinue
$cp = "$MD\lib\classpath.jar;$MD\lib\*;$MD\plugins\com.nomagic.magicdraw.dependencymatrix\*;$GEN\post_out"
$a = @("-Xmx3g","-Desi.system.config=data/application.conf","-DLOCALCONFIG=true","--illegal-access=permit","-Dfile.encoding=UTF-8","-Dpost.in=$In","-Dpost.out=$Out","-Dpost.log=$log","-cp",$cp,"PostMatrix")
Push-Location $MD; & "$MD\jre\bin\java.exe" @a *> (Join-Path $GEN "post.stdout"); Pop-Location
if (Test-Path $log) { Get-Content $log -Encoding UTF8 } else { "无日志，见 post.stdout" }
