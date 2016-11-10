
To build from source code:

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
        
    and then paste into into the Help.java file.





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
