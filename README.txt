

   Copyright 2012 Regents of the University of Michigan

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

Developer: Jim Irrer  irrer@umich.edu

The DICOM project.  This project provides support for:
    - anonymizing DICOM files
    - viewing DICOM files
    - uploading DICOM files to a PACS (requires separate DICOM Service program)

Run using the *.sh or *.bat scripts on Linux or Windows respectively.


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




To build the keystore files, run the keytool command with the following options.
    
    keytool -genkey -v -alias dicomsvc -dname "CN=dicomsvc, OU=Radiation Oncology, O=University of Michigan, C=US" -keypass [password] -keystore dicomsvc.jks -storepass [password] -keyalg "RSA" -sigalg "SHA512withRSA" -keysize 2048 -validity 36500

Note: The [password] must be the same in both instances on the above keytool invocation.

The key_password is required by the server and is stored in the dicomsvcConfig.xml file.  For
this reason it is important that the this file be readable only by the service itself.




When accessing the service using a web browser, a warning is shown indicating that it is untrusted.
IE will say: 'There is a problem with this website's security certificate.'  Click the 'Continue to this website (not recommended).' to continue.
If you do not want the warning, do the following to permanently trust the web site:
 
Found at http://stackoverflow.com/questions/681695/what-do-i-need-to-do-to-get-internet-explorer-8-to-accept-a-self-signed-certific

    How to make IE8 trust a self-signed certificate in 20 irritating steps

      1 Browse to the site whose certificate you want to trust.
      2 When told "There is a problem with this website's security certificate.", choose "Continue to this website (not recommended)."
      3 Select Tools->Internet Options.
      4 Select Security->Trusted sites->Sites.
      5 Confirm the URL matches, and click "Add" then "Close".
      6 Close the "Internet Options" dialog box with either "OK" or "Cancel".
      7 Refresh the current page.
      8 When told "There is a problem with this website's security certificate.", choose "Continue to this website (not recommended)."
      9 Click on "Certificate Error" at the right of the address bar and select "View certificates".
     10 Click on "Install Certificate...", then in the wizard, click "Next".
     11 On the next page select "Place all certificates in the following store".
     12 Click "Browse", select "Trusted Root Certification Authorities", and click "OK".
     13 Back in the wizard, click "Next", then "Finish".
     14 If you get a "Security Warning" message box, click "Yes".
     15 Dismiss the message box with "OK".
     16 Select Tools->Internet Options.
     17 Select Security->Trusted sites->Sites.
     18 Select the URL you just added, click "Remove", then "Close".
     19 Now shut down all running instances of IE, and start up IE again.
     20 The site's certificate should now be trusted.

Chrome is no better, Firefox is really easy.




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


The Linux service supports the start, stop, restart, and status operations
with the /etc/init.d/dicomsvc script:

    dicomsvc start
    dicomsvc stop
    dicomsvc restart
    dicomsvc status

To install on Linux (as root):

    rpm --install dicomsvc-0.0.1-1.noarch.rpm

To un-install on Linux (as root) use the command below.  Note that this
will remove configuration files.

    sudo rpm -e dicomsvc

To check to see if the RPM is installed on Linux:

    rpmquery dicomsvc

To extract all files and spec file from the RPM file on Linux:

    rpm2cpio DICOMService-0.0.1-1.noarch.rpm | cpio  -idmv

    rpm --scripts -qp DICOMService-0.0.1-1.noarch.rpm > spec

The main files are installed in:

    /usr/local/dicomsvc

Log files are put in:

    /var/log/dicomsvc
    



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



    
The following fields should be modified to comply with 'basic anonymization' according to the 

    Digital Imaging and Communications in Medicine (DICOM)
    Supplement 55: Attribute Level Confidentiality (including De-identification)

        Table X.1-1 : Basic Application Level Confidentiality Profile Attributes

    Attribute Name                            Tag
    --------- ----                            ---
    Instance Creator UID                      0008,0014
    SOP Instance UID                          0008,0018
    Accession Number                          0008,0050
    Institution Name                          0008,0080
    Institution Address                       0008,0081
    Referring Physician’s Name                0008,0090
    Referring Physician’s Address             0008,0092
    Referring Physician’s Telephone Numbers   0008,0094
    Station Name                              0008,1010
    Study Description                         0008,1030
    Series Description                        0008,103E
    Institutional Department Name             0008,1040
    Physician(s) of Record                    0008,1048
    Performing Physicians’ Name               0008,1050
    Name of Physician(s) Reading Study        0008,1060
    Operators’ Name                           0008,1070
    Admitting Diagnoses Description           0008,1080
    Referenced SOP Instance UID               0008,1155
    Derivation Description                    0008,2111
    Patient’s Name                            0010,0010
    Patient ID                                0010,0020
    Patient’s Birth Date                      0010,0030
    Patient’s Birth Time                      0010,0032
    Patient’s Sex                             0010,0040
    Other Patient Ids                         0010,1000
    Other Patient Names                       0010,1001
    Patient’s Age                             0010,1010
    Patient’s Size                            0010,1020
    Patient’s Weight                          0010,1030
    Medical Record Locator                    0010,1090
    Ethnic Group                              0010,2160
    Occupation                                0010,2180
    Additional Patient’s History              0010,21B0
    Patient Comments                          0010,4000
    Device Serial Number                      0018,1000
    Protocol Name                             0018,1030
    Study Instance UID                        0020,000D
    Series Instance UID                       0020,000E
    Study ID                                  0020,0010
    Frame of Reference UID                    0020,0052
    Synchronization Frame of Reference UID    0020,0200
    Image Comments                            0020,4000
    Request Attributes Sequence               0040,0275
    UID                                       0040,A124
    Content Sequence                          0040,A730
    Storage Media File-set UID                0088,0140
    Referenced Frame of Reference UID         3006,0024
    Related Frame of Reference UID            3006,00C2
    
