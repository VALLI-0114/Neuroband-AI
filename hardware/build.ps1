# Rebuilds both NeuroBand boards from neuroband_pcb.py: place -> autoroute -> fill -> DRC + fab files.
# Needs KiCad 10 and Java 17+. Run from anywhere:  powershell -ExecutionPolicy Bypass -File hardware\build.ps1
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
$py = "C:\Program Files\KiCad\10.0\bin\python.exe"
$jar = "tools\freerouting-1.9.0.jar"
if (-not (Test-Path $jar)) {
    New-Item -ItemType Directory -Force tools | Out-Null
    Invoke-WebRequest "https://github.com/freerouting/freerouting/releases/download/v1.9.0/freerouting-1.9.0.jar" -OutFile $jar
}
foreach ($b in "main", "ppg") {
    & $py -u neuroband_pcb.py $b place; if ($LASTEXITCODE) { throw "$b place failed" }
    Remove-Item "$b\neuroband_$b.ses" -ErrorAction SilentlyContinue
    & java -jar $jar -de "$b\neuroband_$b.dsn" -do "$b\neuroband_$b.ses" -mp 100
    & $py -u neuroband_pcb.py $b finish; if ($LASTEXITCODE) { throw "$b finish failed" }
    & $py -u neuroband_pcb.py $b fab; if ($LASTEXITCODE) { throw "$b fab failed" }
    Select-String -Path "$b\fab\drc.rpt" -Pattern "Found"
}
