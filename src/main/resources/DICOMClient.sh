#!/bin/bash

# Get the directory that this script resides in
export dirname=$( dirname $0 )

java -Xmx200m -cp ${dirname}/@@with-dep-jar@@ -Djava.util.logging.config.file=${dirname}/logging.propertiesLinux edu.umro.dicom.client.DicomClient ${*}
