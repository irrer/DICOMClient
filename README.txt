
Licensed under the <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache License, Version 2.0</a>

Developer: Jim Irrer  irrer@umich.edu

Most recent documentation:

    http://htmlpreview.github.io/?https://github.com/irrer/DICOMClient/blob/master/docs/user_manual/user_manual_1.0.61/output/index.html

The DICOM project.  This project provides support for:
    - anonymizing DICOM files
    - viewing DICOM files
    - editing DICOM files
    - uploading DICOM files to a PACS

To run:

     DICOM+.bat        : Windows 32 or 64 bit
     
     DICOM+MIL.bat     : Windows 32 or 64 bit, with special options set for the VA (US Veterans Affairs)
    
     DICOMClient.sh    : Linux

From the command line to build everything and put the jars into the repository:

    mvn -DskipTests=true --settings "D:\pf\Maven\m2\settings.xml" install
        
From the command line to run automatic tests:

    mvn --settings "D:\pf\Maven\m2\settings.xml" test

    
This will generate all of the required resources including the jar file with
all dependencies and the RPM file for Linux installation.  The generated files
are put in the target directory.

A copy of the zipped binaries will also be put in the packages directory.

To install the client, put the files:

    dicomsvc-*-jar-with-dependencies.jar
    DICOMUpload.bat

into the desired directory and run the bat file.

The product version is driven by the <version> tag in the pom.xml file.
The server and client share the same version.  As a developer, if you make
changes to the code that warrant a version change, then change this
value before building a new package.


To change the version of the documentation:
   1. Copy all the files (except output dir) from an old
      version (e.g. docs/user_manual/user_manual_1.0.53) to
      your new version (e.g. docs/user_manual/user_manual_1.0.61)

   2. Make the modifications to the input files as necessary.

   3. Change the doc.version in the pom.xml file to the new version number.

   4. Update the doc version in the README.md file.

   5. Run maven --> package to rebuild everything.

The DicomDump program will print the contents of a DICOM file and the corresponding binary.  To run it:
   java -cp dicomclient-1.0.61-jar-with-dependencies.jar edu.umro.dicom.client.DicomDump MyFile.dcm

Files of interest:

src/main/resources/log4j.properties
    log4j properties file
    
src/main/resources/Install
    directory contains the installation files used to generate the
    RPM file for Linux installation.  Within that directory: 
    
    dicomsvc : shell script installed into /etc/rc.d/init.d and implements the start / stop / restart / status commands

    description : Description that shows up in the RPM via rpmquery --info dicomsvc

    common : defines common values for the other scripts.  It is inserted in them by the BuildRpm program.
    
    post : shell script that is run by the rpm installer after copying files (%post part of specfile)
    
    postun : shell script run by the rpm installer after un-installation (after removing files) (%postun part of specfile)
    
    pre : shell script run by the rpm installer before copying files (%pre part of specfile)
    
    preun : shell script run by the rpm installer before un-installation (before removing files) (%preun part of specfile)
    
    
src/main/resources/DICOMUploadHelp.html
    Contains the Dicom Client help text that is edited and then put into the edu.umro.dicom.client.Help class.
    Using vim, process this file with:
    
        :%s/.*/"&<br>\\n" +/
        
    and then paste into the Help.java file.




For anonymizing files, study IDs (and probably series IDs) should be modified consistently
    in order to be accepted by software that expects them to be so.  By 'consistent', we mean
    that cross-references between series should still be operable, even though they have changed.

    In an earlier version of the software, a series was imported to a PACS (ConQuest), and then
    a copy of the series was anonymized.  The anonymized series was uploaded (pushed) to the PACS,
    but it failed because the PatientID had changed but the StudyInstanceUID was still the same, which
    meant that the StudyInstanceUID was being used for more than one patient, so that PACS rejected
    it.  The message in the log file was (reformatted for clarity): 

    ***Refused to enter inconsistent link PatientID into DICOMStudies:
        PatientID = '11112345'
        StudyInsta = '1.2.826.0.1.3680043.2.135.733423.48611003.7.1325114853.44.83',
        Old='99999999',
        Refused='11112345'




It is possible to preload anonymizing UIDs, which makes it possible to perform anonymizations in
separate sessions.  This done using '-p preload.xml' on the command line.  The file may have any name,
but must be an XML file in the format shown in the example below:
    
    <AnonymizePreload>
        <PatientID orig='9999999' anon='anon1'>
            <UID orig='5666.6233.32082.19731.14352.14025' anon='21572.23911.6483.20150.14501.31008'/>
            <UID orig='25280.12659.3820.14299.11201.16397' anon='19798.27395.15548.28802.30793.1830'/>
            <UID orig='21129.16168.27817.360.32720.16562' anon='8691.684.1404.13190.19050.11391'/>
            <UID orig='17914.15411.19083.9518.6769.11889' anon='3602.23430.15348.18708.20977.25694'/>
            <UID orig='27568.11759.17608.10574.31253.3475' anon='15829.6891.26732.10644.13925.19393'/>
            <UID orig='8661.20635.32296.3619.7849.2783' anon='21819.21533.17136.9236.19335.12494'/>
            <UID orig='11095.6833.25741.6636.31220.16809' anon='25332.11209.6463.13080.9805.2453'/>
            <UID orig='6977.27659.29806.4780.29680.19044' anon='29550.31385.26327.21091.32716.20876'/>
            <UID orig='16782.28623.13571.30981.29253.15744' anon='17385.13039.7101.19874.31946.21878'/>
            <UID orig='25530.30337.6575.13787.28429.24007' anon='23843.25138.21668.2651.25863.22145'/>
        </PatientID>
        <PatientID orig='8888888' anon='anon2'>
            <UID orig='20964.24528.20686.13055.13590.28218' anon='15816.5802.2199.1506.28560.2140'/>
            <UID orig='22975.10644.18081.6026.29155.31189' anon='6896.8911.18540.16587.30293.27529'/>
            <UID orig='31561.1182.14691.7866.23950.20229' anon='32476.10855.1162.1908.22868.15862'/>
            <UID orig='8230.15084.4967.22446.28639.13543' anon='22720.23384.7581.21964.17898.14317'/>
            <UID orig='20308.6799.25516.14155.14848.25189' anon='25551.25400.3839.2512.29695.32091'/>
            <UID orig='32587.19713.8826.3931.16048.11411' anon='5984.20133.13703.25502.21530.7886'/>
            <UID orig='32739.15023.22446.7263.23477.31084' anon='10762.11480.16194.13409.21110.130'/>
            <UID orig='2294.2997.15253.13982.18532.23134' anon='21195.19210.1743.12441.10225.31914'/>
            <UID orig='3467.12411.29503.24953.3451.17661' anon='21511.10872.27757.4539.5704.2658'/>
            <UID orig='18222.13718.7029.22317.29634.26146' anon='27767.13526.26375.2090.15286.19254'/>
        </PatientID>
    </AnonymizePreload>


    <
     <b>Copyright 2016 Regents of the University of Michigan</b>
 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 </body>
 </html>