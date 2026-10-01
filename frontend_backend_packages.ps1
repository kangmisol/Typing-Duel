$ErrorActionPreference = "Stop"

$root = "Typing-Duel-Template\src"
$files = Get-ChildItem -Path $root -Filter "*.java" -Recurse

$rootPath = (Resolve-Path "$root").Path + "\"

foreach ($file in $files) {
    if ($file.DirectoryName.StartsWith($rootPath)) {
        $relPath = $file.DirectoryName.Substring($rootPath.Length)
    } else {
        $relPath = $file.DirectoryName.Substring(((Resolve-Path "$root").Path).Length)
        if ($relPath.StartsWith("\")) { $relPath = $relPath.Substring(1) }
    }
    
    $expectedPackage = $relPath -replace "\\", "."
    
    $content = Get-Content $file.FullName -Raw
    
    if ($content -match '(?m)^package\s+[\w\.]+;') {
        $content = $content -replace '(?m)^package\s+[\w\.]+;', "package $expectedPackage;"
    } else {
        $content = "package $expectedPackage;`r`n`r`n" + $content
    }

    $content = $content -replace 'import\s+typingduel\.gui\.', 'import frontend.'
    $content = $content -replace 'import\s+typingduel\.backend\.', 'import backend.logic.'
    $content = $content -replace 'import\s+typingduel\.model\.', 'import backend.model.'
    $content = $content -replace 'import\s+typingduel\.storage\.', 'import backend.storage.'
    $content = $content -replace 'import\s+typingduel\.utils\.', 'import backend.utils.'
    
    $content = $content -replace 'import\s+com\.template\.ui\.screens\.', 'import frontend.'
    $content = $content -replace 'import\s+com\.template\.ui\.', 'import frontend.'
    $content = $content -replace 'import\s+com\.template\.model\.', 'import backend.model.'
    $content = $content -replace 'import\s+com\.template\.persistence\.', 'import backend.storage.'
    $content = $content -replace 'import\s+com\.template\.service\.', 'import backend.logic.'

    # Handle old models that got moved here directly
    $content = $content -replace 'import\s+model\.', 'import backend.model.'
    $content = $content -replace 'import\s+storage\.', 'import backend.storage.'
    
    [IO.File]::WriteAllText($file.FullName, $content)
}

Write-Host "Replaced all packages and imports to frontend/backend."
