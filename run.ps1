if (!(Test-Path "lib")) {
    New-Item -ItemType Directory -Force -Path "lib" | Out-Null
}
if (!(Test-Path "lib\sqlite-jdbc-3.36.0.3.jar")) {
    Write-Host "Downloading sqlite-jdbc..."
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar" -OutFile "lib\sqlite-jdbc-3.36.0.3.jar"
}
if (!(Test-Path "out")) {
    New-Item -ItemType Directory -Force -Path "out" | Out-Null
}

Write-Host "Compiling..."
javac -encoding UTF-8 -cp "lib\sqlite-jdbc-3.36.0.3.jar" -d out src\main\java\atm\*.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "Running ATM Application..."
    java -cp "out;lib\sqlite-jdbc-3.36.0.3.jar" atm.AtmApp
} else {
    Write-Host "Compilation failed."
}
