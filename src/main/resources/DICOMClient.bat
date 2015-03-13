@echo off

echo Run in University of Michigan specific environment.

@rem Show java version to aid with diagnosing problems.
java -version

echo java -version | cmd 2>&1 | find "1.6.0" >nul 2>nul
if %errorlevel% EQU 0 GOTO javaok
echo java -version | cmd 2>&1 | find "1.7.0" >nul 2>nul
if %errorlevel% EQU 0 GOTO javaok
echo java -version | cmd 2>&1 | find "1.8" >nul 2>nul
if %errorlevel% EQU 0 GOTO javaok

@rem Use java JVM that is available on the network
set JAVA_HOME=S:\Physics\Projects\jre6-32
set PATH=%JAVA_HOME%\bin;%PATH%
:javaok

set dirname=%~dp0
echo Starting DICOM+

FOR %%M IN (16384 8192 4096 2048 1536 1280 1024 768 512 384 256 128 64 32) DO echo Using memory: %%M && java -Xmx%%Mm -cp %dirname%@@with-dep-jar@@ edu.umro.dicom.client.DicomClient %* && EXIT /B

@rem The script gets here if something went wrong.  Let the user look at the screen to get information to fix the problem.
pause
