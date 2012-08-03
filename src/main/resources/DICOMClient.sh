#!/bin/bash

# Get the directory that this script resides in
export dirname=$( dirname $0 )

java \
    -Xmx200m                                                              \
    -cp ${dirname}/@@with-dep-jar@@                                       \
    -Ddicomclient.config=${dirname}/DicomClientConfig.xml                 \
    -Djava.util.logging.config.file=${dirname}/logging.propertiesLinux    \
    -Djavax.net.ssl.trustStore=${dirname}/dicomsvc.jks                    \
    edu.umro.dicom.client.DicomClient ${*}
