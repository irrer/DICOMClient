@echo off

@rem Get the directory that this script resides in
for %%F in (%0) do set dirname=%%~dpF

set JAVA_HOME=\\robkup\TPSData\java\jre6-32
set PATH=%JAVA_HOME%\bin;%PATH%

java -Xmx256m -cp %dirname%@@with-dep-jar@@ edu.umro.dicom.client.DicomClient %*
