@echo off

@rem Show java version to aid with diagnosing problems.
java -version

echo java -version | cmd 2>&1 | find "build 1.8." >nul 2>nul
if %errorlevel% EQU 0 GOTO javaok

echo java -version | cmd 2>&1 | find "build 1.7." >nul 2>nul
if %errorlevel% EQU 0 GOTO javaok

echo java -version | cmd 2>&1 | find "build 1.6." >nul 2>nul
if %errorlevel% EQU 0 GOTO java6

echo You must have java version 7 or 8 installed, shown as 1.7 or 1.8
echo to run this program.  The java.exe program must also be in a directory
echo referenced by your PATH environment variable.
echo.
echo To see your java version, enter the following command at the DOS prompt:
echo.
echo    java -version
echo.
GOTO fail

:javaok

set dirname=%~dp0
echo Starting DICOM+

@rem Try to get as much memory as the system will allow by first trying for lots and then less and less until one is granted.
FOR %%M IN (16384 8192 4096 2048 1536 1280 1024 768 512 384 256 128 64 32) DO (
    echo Using memory: %%M
    java -Xmx%%Mm -cp "%dirname%@@with-dep-jar@@" edu.umro.dicom.client.DicomClient %*
    if %errorlevel% EQU 0 GOTO succeed
    if %errorlevel% NEQ 1 GOTO fail )

echo Unable to start DICOM+ 

:java6
echo The java you have installed is version 6.  You either need to install a newer version,
echo or use an older version of this software.  The most recent version that supports Java 6
echo is 1.0.37, which can be downloaded from:
echo "    " https://github.com/irrer/DICOMClient/blob/master/prebuilt_packages/dicomclient-1.0.37_Install.zip?raw=true
echo To show the version of java you are using, enter this command from a DOS window:
echo
echo    java -version
echo

:fail
pause

:succeed
