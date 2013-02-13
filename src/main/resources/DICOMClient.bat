@echo off

@rem Get the directory that this script resides in
for %%F in (%0) do set dirname=%%~dpF

@rem set JAVA_HOME=\\robkup\TPSData\java\jre6-32
set JAVA_HOME=S:\Physics\Projects\jre6-32

set PATH=%PATH%;%JAVA_HOME%\bin

@rem set the current directory so that logs will go in the right place
cd %dirname%

if exist log goto gotlog
    mkdir log
:gotlog

java -Xmx256m -cp %dirname%@@with-dep-jar@@ -Djava.util.logging.config.file=%dirname%logging.propertiesWindows edu.umro.dicom.client.DicomClient %* > log\%DATE:~10,4%_%DATE:~4,2%_%DATE:~7,2%_%TIME:~0,2%_%TIME:~3,2%_%TIME:~6,2%_%TIME:~9,2%.log 2>&1
