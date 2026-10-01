$ErrorActionPreference = "Stop"

$root = "Typing-Duel-Template"

# Clean phantom
if (Test-Path "$root\src\com") {
    Remove-Item -Path "$root\src\com" -Recurse -Force
    Write-Host "Removed phantom $root\src\com"
}

# Move Main.java
if (Test-Path "$root\src\Main.java") {
    Move-Item -Path "$root\src\Main.java" -Destination "$root\src\typingduel\Main.java" -Force
    Write-Host "Moved Main.java"
}

# Create scaffold
$dirs = @(
    "$root\data\players",
    "$root\lib",
    "$root\resources\images\backgrounds",
    "$root\resources\images\sprites",
    "$root\resources\audio"
)
foreach ($dir in $dirs) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
}

# Move resources
if (Test-Path "$root\src\resources") {
    $r = "$root\src\resources"
    
    if (Test-Path "$r\char*.png") { Move-Item -Path "$r\char*.png" -Destination "$root\resources\images\sprites\" -Force }
    if (Test-Path "$r\typingDuelBackground.jpg") { Move-Item -Path "$r\typingDuelBackground.jpg" -Destination "$root\resources\images\backgrounds\" -Force }
    if (Test-Path "$r\TypingDuelLogo.png") { Move-Item -Path "$r\TypingDuelLogo.png" -Destination "$root\resources\images\" -Force }
    
    # move leftovers just in case
    Get-ChildItem -Path $r -File | ForEach-Object {
        Move-Item -Path $_.FullName -Destination "$root\resources\" -Force
    }
    
    Remove-Item -Path $r -Recurse -Force
    Write-Host "Moved resources and deleted src/resources"
}

Write-Host "Structural Cleanup Done"
