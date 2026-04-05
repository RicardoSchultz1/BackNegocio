@ECHO OFF
SETLOCAL EnableDelayedExpansion

SET BASE_DIR=%~dp0
IF "%BASE_DIR:~-1%"=="\" SET BASE_DIR=%BASE_DIR:~0,-1%

SET WRAPPER_PROPERTIES=%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties
SET WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar

IF NOT EXIST "%WRAPPER_PROPERTIES%" (
  ECHO Missing %WRAPPER_PROPERTIES%
  EXIT /B 1
)

IF "%JAVA_HOME%"=="" (
  SET JAVA_EXE=java
) ELSE (
  SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

SET JAVA_FOUND=0
IF EXIST "%JAVA_EXE%" SET JAVA_FOUND=1

IF "%JAVA_FOUND%"=="0" (
  WHERE java >NUL 2>&1
  IF %ERRORLEVEL% EQU 0 (
    SET JAVA_EXE=java
    SET JAVA_FOUND=1
  )
)

IF "%JAVA_FOUND%"=="0" (
  IF EXIST "%ProgramFiles%\Java" (
    FOR /D %%D IN ("%ProgramFiles%\Java\jdk*") DO (
      IF EXIST "%%D\bin\java.exe" (
        SET JAVA_EXE=%%D\bin\java.exe
        SET JAVA_FOUND=1
      )
    )
  )
)

IF "%JAVA_FOUND%"=="0" (
  ECHO Java executable not found. Set JAVA_HOME or install Java.
  EXIT /B 1
)

IF NOT EXIST "%WRAPPER_JAR%" (
  SET WRAPPER_URL=
  FOR /F "usebackq tokens=1,* delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
    IF "%%A"=="wrapperUrl" SET WRAPPER_URL=%%B
  )

  IF "!WRAPPER_URL!"=="" SET WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

  ECHO Downloading Maven Wrapper jar from: !WRAPPER_URL!
  POWERSHELL -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '!WRAPPER_URL!' -OutFile '%WRAPPER_JAR%'"
  IF %ERRORLEVEL% NEQ 0 EXIT /B 1
)

"%JAVA_EXE%" %MAVEN_OPTS% -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
