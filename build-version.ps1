
$version = Read-Host "Enter Task Tracker version (example: 1.0.1)"

if ($version -notmatch '^\d+\.\d+\.\d+$') {
    Write-Host "Invalid version format. Use X.Y.Z"
    exit 1
}

Write-Host "Updating project version to $version"

mvn versions:set "-DnewVersion=$version" "-DgenerateBackupPoms=false"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Version update failed!"
    exit 1
}

Write-Host "Building application..."

mvn package

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!"
    exit 1
}

Write-Host "Build successful!"
Write-Host "New JAR: target\task-tracker-$version.jar"
