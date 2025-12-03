@echo off
SETLOCAL
set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set DEFAULT_JVM_OPTS=

for /f "delims=" %%i in ('where java 2^>nul') do set JAVA_EXE=%%i
if not defined JAVA_EXE (
  echo ERROR: Java not found on PATH. Install Java 17+ and try again.
  exit /b 1
)

set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
if not exist "%CLASSPATH%" (
  echo Gradle wrapper JAR not found at %CLASSPATH%.
  echo If you have Gradle installed, run: gradle wrapper
  exit /b 1
)

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
ENDLOCAL
