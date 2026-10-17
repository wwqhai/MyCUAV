# 编译并运行生成器（Gen）。用法: powershell -NoProfile -ExecutionPolicy Bypass -File go.ps1
# 输出 out.mdzip，日志 gen.log。生成后还要运行 post.ps1 设置依赖矩阵范围。
$GEN = $PSScriptRoot
$MD = if ($env:MD_HOME) { $env:MD_HOME } else { "D:\Program Files\MagicDraw" }
New-Item -ItemType Directory -Force "$GEN\out" | Out-Null
Remove-Item "$GEN\out\*.class" -ErrorAction SilentlyContinue
& javac --release 11 -encoding UTF-8 -proc:none -cp "$MD\lib\*" -d "$GEN\out" (Get-ChildItem "$GEN\src\*.java").FullName 2>&1 | Out-String | Write-Host
& "$GEN\run.ps1" -Class Gen -Out "$GEN\out.mdzip" -Log gen.log
