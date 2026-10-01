$ErrorActionPreference = "Stop"

$root = "Typing-Duel-Template"
$src = "$root\src"

if (Test-Path "$src\typingduel\gui") {
    Move-Item "$src\typingduel\gui" "$src\frontend" -Force
}

New-Item -ItemType Directory -Force "$src\backend" | Out-Null

if (Test-Path "$src\typingduel\backend") {
    Move-Item "$src\typingduel\backend" "$src\backend\logic" -Force
}
if (Test-Path "$src\typingduel\model") {
    Move-Item "$src\typingduel\model" "$src\backend\model" -Force
}
if (Test-Path "$src\typingduel\storage") {
    Move-Item "$src\typingduel\storage" "$src\backend\storage" -Force
}
if (Test-Path "$src\typingduel\utils") {
    Move-Item "$src\typingduel\utils" "$src\backend\utils" -Force
}
if (Test-Path "$src\typingduel\Main.java") {
    Move-Item "$src\typingduel\Main.java" "$src\backend\Main.java" -Force
}

if (Test-Path "$src\typingduel") {
    Remove-Item "$src\typingduel" -Recurse -Force
}

if (Test-Path "src\model") {
    Get-ChildItem -Path "src\model" -File | ForEach-Object {
        Move-Item -Path $_.FullName -Destination "$src\backend\model\" -Force
    }
    Remove-Item "src" -Recurse -Force
}

if (Test-Path "$root\legacy") {
    Rename-Item "$root\legacy" "discarded_archive"
}
if (Test-Path "$root\data\legacy") {
    Rename-Item "$root\data\legacy" "discarded_archive"
}

$clutter = @(
    ".idea",
    "$root\.idea",
    "$root\out",
    "$root\lib"
)

foreach ($c in $clutter) {
    if (Test-Path $c) {
        Remove-Item $c -Recurse -Force
    }
}

Write-Host "Restructuring Step 1 Complete."
