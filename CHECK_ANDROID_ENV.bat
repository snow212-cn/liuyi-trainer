@echo off
setlocal
title Liuyi Trainer Android Environment Check

echo.
echo ===== Liuyi Trainer Android 环境检查 =====
echo.

echo [1/4] 检查 Java
where java >nul 2>nul
if errorlevel 1 (
  echo 未找到 Java
) else (
  java -version
)
where javac >nul 2>nul
if errorlevel 1 (
  echo 未找到 javac，当前只有 JRE 或尚未安装 JDK
) else (
  echo 已找到 javac
)
echo.

echo [2/4] 检查 Gradle Wrapper
if exist "%~dp0gradlew.bat" (
  echo 已找到 gradlew.bat
) else (
  echo 未找到 gradlew.bat
)

if exist "%~dp0gradle\wrapper\gradle-wrapper.jar" (
  echo 已找到 gradle-wrapper.jar
) else (
  echo 未找到 gradle-wrapper.jar
)
echo.

echo [3/4] 检查 Android SDK
if defined ANDROID_HOME (
  echo ANDROID_HOME=%ANDROID_HOME%
) else (
  echo ANDROID_HOME 未设置
)

if defined ANDROID_SDK_ROOT (
  echo ANDROID_SDK_ROOT=%ANDROID_SDK_ROOT%
) else (
  echo ANDROID_SDK_ROOT 未设置
)
echo.

echo [4/4] 当前结论
where javac >nul 2>nul
if errorlevel 1 (
  echo 当前主要缺口是 JDK，所以 Gradle 还不能进行正式编译。
) else if not defined ANDROID_HOME if not defined ANDROID_SDK_ROOT (
  echo 当前主要缺口是 Android SDK，所以还不能正式构建 APK。
) else (
  echo 已检测到 JDK 与 Android SDK 关键条件，可继续尝试正式构建。
)
echo.
echo 你也可以直接查看：
echo docs\process\current-status.md
echo docs\process\handoff.md
echo.
pause
endlocal
