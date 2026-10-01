$ErrorActionPreference = "Stop"

$root = "Typing-Duel-Template\src\typingduel"
$files = Get-ChildItem -Path $root -Filter "*.java" -Recurse

foreach ($file in $files) {
    $relPath = $file.DirectoryName.Substring((Resolve-Path "$root\..").Path.Length + 1)
    $expectedPackage = $relPath -replace "\\", "."
    
    $content = Get-Content $file.FullName -Raw
    
    if ($content -match '(?m)^package\s+[\w\.]+;') {
        $content = $content -replace '(?m)^package\s+[\w\.]+;', "package $expectedPackage;"
    } else {
        $content = "package $expectedPackage;`r`n`r`n" + $content
    }

    $content = $content -replace 'import\s+com\.template\.model\.', 'import typingduel.model.'
    $content = $content -replace 'import\s+com\.template\.persistence\.', 'import typingduel.storage.'
    $content = $content -replace 'import\s+com\.template\.service\.', 'import typingduel.backend.'
    $content = $content -replace 'import\s+com\.template\.ui\.screens\.', 'import typingduel.gui.'
    $content = $content -replace 'import\s+com\.template\.ui\.', 'import typingduel.gui.'
    $content = $content -replace 'import\s+typingduel\.service\.', 'import typingduel.backend.'
    $content = $content -replace 'import\s+typingduel\.util\.', 'import typingduel.utils.'
    
    [IO.File]::WriteAllText($file.FullName, $content)
}

Write-Host "Replaced all packages and imports."
