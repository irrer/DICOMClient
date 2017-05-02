@echo off

@rem Show java version to aid with diagnosing problems.
java -version

echo java -version | cmd 2>&1 | find "1.7.0" >nul 2>nul
if %errorlevel% EQU 0 GOTO javaok

echo java -version | cmd 2>&1 | find "1.8" >nul 2>nul
if %errorlevel% EQU 0 GOTO javaok

echo You must have java version 7 or 8 installed, shown as 1.7, or 1.8
echo to run this program.  The java.exe program must also be in a directory
echo referenced by your PATH environment variable.
echo.
echo To see your java version, enter the following command at the DOS prompt:
echo.
echo    java -version
echo.
GOTO javabad

:javaok

set dirname=%~dp0
echo Starting DICOM+

@rem Try to get as much memory as the system will allow by first trying for lots and then less and less until one is granted.
FOR %%M IN (16384 8192 4096 2048 1536 1280 1024 768 512 384 256 128 64 32) DO echo Using memory: %%M && java -Xmx%%Mm -cp "%dirname%@@with-dep-jar@@" -Ddicomclient.config="%dirname%DicomClientConfigMil.xml" edu.umro.dicom.client.DicomClient -y %* && EXIT /B

echo Unable to start DICOM+ 

:javabad
pause
