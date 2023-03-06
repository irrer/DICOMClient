@echo off

@rem Show java version to aid with diagnosing problems.
java -version

set dirname=%~dp0
echo Starting DICOM+

@rem Try to get as much memory as the system will allow by first trying for lots and then less and less until one is granted.
FOR %%M IN (16384 8192 4096 2048 1536 1280 1024 768 512 384 256 128 64 32) DO echo Using memory: %%M && java -Xmx%%Mm -cp "%dirname%@@with-dep-jar@@" edu.umro.dicom.client.DicomClient %* && EXIT /B

echo Unable to start DICOM+ 
