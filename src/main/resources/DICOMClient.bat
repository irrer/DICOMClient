@echo off

@rem Get the directory that this script resides in
for %%F in (%0) do set dirname=%%~dpF

@rem Try to use the local copy of java because it is
@rem so much faster.  Version 6 or 7 are ok.
echo java -version | cmd 2>&1 | find "1.6.0" >nul 2>nul
if %errorlevel% EQU 0 GOTO javaok
echo java -version | cmd 2>&1 | find "1.7.0" >nul 2>nul
if %errorlevel% EQU 0 GOTO javaok

@rem Use java JVM that is available on the network
set JAVA_HOME=S:\Physics\Projects\jre6-32
set PATH=%JAVA_HOME%\bin;%PATH%

:javaok

@rem set the current directory so that logs will go in the right place
@rem and the jks file is accessible
@rem first set the drive (equivalent to S: or whatever drive)
%dirname:~0,2%
@rem then set the directory
cd %dirname%

if exist log goto gotlog
    mkdir log
:gotlog

java -Xmx4096m -cp %dirname%@@with-dep-jar@@ -Djava.util.logging.config.file=%dirname%logging.propertiesWindows edu.umro.dicom.client.DicomClient %* > log\%DATE:~10,4%_%DATE:~4,2%_%DATE:~7,2%_%TIME:~0,2%_%TIME:~3,2%_%TIME:~6,2%_%TIME:~9,2%.log 2>&1
