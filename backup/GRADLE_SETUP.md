# Gradle Setup Help

ForgeGradle 5.1.40 (used for Minecraft 1.20.1) does **not support Gradle 9.x**.

You are currently running Gradle 9.2.1, which is too new.

## Solution: Downgrade Gradle to 8.4.1

### Option 1: Use Chocolatey (if installed)
```powershell
choco install gradle --version=8.4.1 --force
```

### Option 2: Manual Download
1. Download Gradle 8.4.1 from https://gradle.org/releases/
2. Extract to `C:\Program Files\Gradle\gradle-8.4.1`
3. Update your PATH to point to the new version

### Option 3: Use SDKMAN (if on WSL or Git Bash)
```bash
sdk install gradle 8.4.1
```

## After downgrading, run:
```powershell
cd C:\dev\projects\BlackHoleMinecraftMod
gradle --version   # should show 8.4.1
gradle wrapper --gradle-version 8.4.1
.\gradlew.bat --no-daemon build
```

## Or, skip wrapper and build directly:
```powershell
gradle --no-daemon build
```
