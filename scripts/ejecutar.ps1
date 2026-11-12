# Ejecutar desde la carpeta principal, en la terminal PowerShell de VS Code:
# .\scripts\ejecutar.ps1
$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force -Path 'out' | Out-Null
$fuentes = @(Get-ChildItem -Path 'src/main/java' -Filter '*.java' -Recurse | ForEach-Object { $_.FullName })
if ($fuentes.Count -eq 0) { throw 'No se encontraron fuentes Java.' }
& javac -encoding UTF-8 -d 'out' $fuentes
if ($LASTEXITCODE -ne 0) { throw 'Compilación fallida.' }
& java -cp 'out' ec.edu.logistpulse.App
