#!/usr/bin/env sh
set -e
THIS_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$THIS_DIR"
DEFAULT_JVM_OPTS=""

if [ -z "$(command -v java)" ]; then
  echo "ERROR: Java not found on PATH. Install Java 17+ and try again."
  exit 1
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$CLASSPATH" ]; then
  echo "Gradle wrapper JAR not found at $CLASSPATH"
  echo "If you have Gradle installed, run: gradle wrapper"
  exit 1
fi

exec java $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
