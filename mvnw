#!/bin/sh
# Maven Wrapper startup script (minimal)

set -e

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_PROPERTIES="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"
WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$WRAPPER_PROPERTIES" ]; then
  echo "Missing $WRAPPER_PROPERTIES"
  exit 1
fi

if [ -z "$JAVA_HOME" ]; then
  JAVA_EXE="$(command -v java || true)"
else
  JAVA_EXE="$JAVA_HOME/bin/java"
fi

if [ ! -x "$JAVA_EXE" ]; then
  echo "Java executable not found. Set JAVA_HOME or install Java."
  exit 1
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  WRAPPER_URL=$(grep '^wrapperUrl=' "$WRAPPER_PROPERTIES" | cut -d'=' -f2-)
  if [ -z "$WRAPPER_URL" ]; then
    WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
  fi

  echo "Downloading Maven Wrapper jar from: $WRAPPER_URL"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
  else
    echo "Neither curl nor wget found. Please install one of them."
    exit 1
  fi
fi

exec "$JAVA_EXE" \
  ${MAVEN_OPTS} \
  -Dmaven.multiModuleProjectDirectory="$BASE_DIR" \
  -classpath "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
