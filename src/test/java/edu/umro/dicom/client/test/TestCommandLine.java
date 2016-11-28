package edu.umro.dicom.client.test;

/*
 * Copyright 2013 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import edu.umro.dicom.client.Util;
import edu.umro.util.Log;
import edu.umro.util.RunCommand;
import edu.umro.util.UMROException;
import edu.umro.util.Utility;
import edu.umro.util.XML;
import static org.junit.Assert.assertTrue;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Automatic test for command line functionality.
 * 
 * @author irrer
 *
 */
public class TestCommandLine {

    /** Source directory for data files. */
    private final File SRC_DIR = new File("src/test/resources/dicom/99999999");

    /** Reference directory containing files of previously successful test runs used to compare newly generated files. */
    private final File REFERENCE_DIR = new File("src/test/resources/dicom/output");

    /** Destination directory for test results. All files in this directory and the directory itself are temporary. */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File baseDestDir() {
        return temporaryFolder.getRoot();
    }

    private String srcPath(String fileName) {
        return (new File(SRC_DIR, fileName)).getAbsolutePath();
    }

    private String refPath(String fileName) {
        return (new File(REFERENCE_DIR, fileName)).getAbsolutePath();
    }

    private String destPath(File destDir, String fileName) {
        destDir.mkdirs();
        return (new File(destDir, fileName)).getAbsolutePath();
    }
    
    private boolean compareXmlFiles(File a, File b) throws UMROException {
        Document aDoc = XML.parseToDocument(Utility.readFile(a));
        Document bDoc = XML.parseToDocument(Utility.readFile(b));
        String aTxt = XML.domToString(aDoc).replaceAll("[ \r\t\n][ \r\t\n]*", "");
        String bTxt = XML.domToString(bDoc).replaceAll("[ \r\t\n][ \r\t\n]*", "");
        boolean same = aTxt.equals(bTxt);
        return same;
    }
    
    private boolean compareTxtFiles(File a, File b) throws UMROException {
        String aTxt = Utility.readFile(a).replaceAll("<unknown>[^\n]*\n", "ignore unknown VRs\n");
        String bTxt = Utility.readFile(b).replaceAll("<unknown>[^\n]*\n", "ignore unknown VRs\n");
        return aTxt.equals(bTxt);
    }

    private boolean compareFiles(File destDir, String fileName) {
        File tst = new File(destDir, fileName);
        File ref = new File(refPath(fileName));

        boolean same = false;
        try {
            if (fileName.toLowerCase().endsWith(".xml")) {
                same = compareXmlFiles(tst, ref);
            }
            else
                if (fileName.toLowerCase().endsWith(".txt")) {
                    same = compareTxtFiles(tst, ref);
                }
                else {
                    same = Utility.compareFiles(tst, ref);
                }
            System.out.println("compared    ref (reference): " + ref.getAbsolutePath() + "    tst (generated): " + tst.getAbsolutePath() + "   same: " + same);
            return same;
        }
        catch (Exception e) {
            System.out.println("Unexpected exception during comparison of files: " + Log.fmtEx(e));
            return false;
        }
    }

    private boolean compareAllFilesWithSuffixes(File destDir, String fileName) {
        if (fileName.endsWith(Util.DICOM_SUFFIX)) fileName = fileName.substring(0, fileName.length() - Util.DICOM_SUFFIX.length());
        for (String suf : new String[] { ".DCM", ".XML", ".TXT", ".PNG" }) {
            File destFile = new File(destPath(destDir, fileName + suf));
            if (destFile.exists()) 
                if (!compareFiles(destDir, fileName + suf)) {
                    return false;
                }
        }
        return true;
    }

    private static int dirIndex = 0;

    private synchronized File getUniqueDestDir() {
        return new File(baseDestDir(), "" + (dirIndex++));
    }

    /**
     * Get the latest generated jar file with dependencies.
     */
    private String jarWithDependencies() {
        File target = new File("target");
        TreeSet<String>jarList = new TreeSet<String>();
        for (File jar : target.listFiles()) {
            String name = jar.getName();
            if (name.matches("dicomclient-.*-jar-with-dependencies.jar")) jarList.add(name);
        }
        return "target/" + jarList.last();
    }
    
    /**
     * Run the main program as a command line.
     * 
     * @param args
     *            Command line arguments.
     * 
     * @return
     */
    private int runMain(String... args) {
        
        String[] baseArgs = { "java", "-Xmx256m", "-cp", jarWithDependencies(),
                "-D" + Util.TESTING_PROPERTY + "=" + Util.TESTING_PROPERTY,
                // "-Djava.util.logging.config.file=src\\test\\resources\\test\\logging.propertiesWindows",
                "edu.umro.dicom.client.DicomClient", "-c" };

        ArrayList<String> argList = new ArrayList<String>();
        for (String a : baseArgs)
            argList.add(a);
        if (args != null) for (String a : args)
            argList.add(a);
        String[] allArgs = new String[argList.size()];
        int i = 0;
        for (String a : argList)
            allArgs[i++] = a;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuffer fullCommand = new StringBuffer();
        for (String s: allArgs) fullCommand.append(s + " ");
        System.out.println("running command: " + fullCommand.toString());

        try {
            int exitCode = RunCommand.runArgs(allArgs, baos, baos);
            return exitCode;
        }
        catch (IOException e) {
            assertTrue("Unexpected IOException: " + Log.fmtEx(e), false);
        }
        catch (InterruptedException e) {
            assertTrue("Unexpected InterruptedException: " + Log.fmtEx(e), false);
        }
        return Integer.MIN_VALUE;
    }

    @BeforeClass
    public static void beforeClass() {
        File file = new File("target/testOutput");
        Utility.deleteFileTree(file);
        file.mkdirs();
        System.getProperties().put("java.io.tmpdir", file.getAbsolutePath());
        System.out.println("temporary files in (java.io.tmpdir): " + file.getAbsolutePath());
    }

    @AfterClass
    public static void afterClass() {
    }

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void commandLineModeNoOptions() {
        int code = runMain();
        assertTrue("command line mode with no options.  Code: " + code, code == 0);
    }

    @Test
    public void invalidOption() {
        int code = runMain("-9");
        assertTrue("bad command line option should fail.  Code: " + code, code != 0);
    }

    @Test
    public synchronized void commandLineRTPlanAnonymization() {
        File destDir = getUniqueDestDir();
        String inFile = srcPath("99999999_RTPLAN.DCM");
        String outFile = "1234_RTPLAN" + Util.DICOM_SUFFIX;
        int code = runMain("-P", "1234", "-o", destPath(destDir, outFile), "-z", inFile);
        assertTrue("command line mode RTPLAN anonymization.  Code: " + code, code == 0);
        assertTrue("Files are equal", compareAllFilesWithSuffixes(destDir, outFile));
    }

    @Test
    public synchronized void commandLineMultipleFilesWithMinusO() {
        File destDir = getUniqueDestDir();
        String inFile1 = srcPath("99999999_RTIMAGE_0001.DCM");
        String inFile2 = srcPath("99999999_RTIMAGE_0002.DCM");
        int code = runMain("-P", "1234", "-o", destDir.getAbsolutePath(), "-z", inFile1, inFile2);
        assertTrue("command line mode with -o and multiple files.  Code: " + code, code != 0);
        assertTrue("no files generated", !destDir.exists());
    }

    @Test
    public synchronized void commandLineMultipleFilesWithMinusD() {
        File destDir = getUniqueDestDir();
        String inFile1 = srcPath("99999999_RTIMAGE_0001.DCM");
        String inFile2 = srcPath("99999999_RTIMAGE_0002.DCM");
        String outFile1 = "1234_RTIMAGE_0001.DCM";
        String outFile2 = "1234_RTIMAGE_0002.DCM";
        int code = runMain("-P", "1234", "-d", destDir.getAbsolutePath(), "-z", inFile1, inFile2);
        assertTrue("command line mode with -d and multiple files.  Code: " + code, code == 0);
        assertTrue("Files are equal MinusD 1", compareAllFilesWithSuffixes(destDir, outFile1));
        assertTrue("Files are equal MinusD 2", compareAllFilesWithSuffixes(destDir, outFile2));
    }

    @Test
    public synchronized void commandLineSingleCT() {
        File destDir = getUniqueDestDir();
        String inFile = srcPath("99999999_CT_2_0001.DCM");
        String outFile = "1234_CT_2_0001";
        int code = runMain("-P", "1234", "-o", destPath(destDir, outFile + Util.DICOM_SUFFIX), "-z", inFile);
        assertTrue("command line mode with -o single CT.  Code: " + code, code == 0);
        assertTrue("Files are equal SingleCT", compareAllFilesWithSuffixes(destDir, outFile));
        try {
            Document doc = XML.parseToDocument(Utility.readFile(new File(destPath(destDir, outFile + Util.XML_SUFFIX))));
            NodeList nodeList = XML.getMultipleNodes(doc, "DicomObject/FileMetaInformationGroupLength");
            assertTrue("XML doc can be parsed", nodeList.getLength() == 1);
        }
        catch (Exception e) {
            assertTrue("XML doc is parsable", false);
        }
    }

    /*
    @Test
    public void xcommandLineMultipleFilesWithMinusO() {
        int code = runMain("-P", "1234", "-o", DEST_DIR, "-z", "src/test/resources/dicom/99999999/99999999_RTIMAGE_0001.DCM", "src/test/resources/dicom/99999999/99999999_RTIMAGE_0002.DCM");
        assertTrue("command line mode with -o and multiple files fails", code != 0);
    }

    @Test
    public void commandLineRTImageAnonymization() {
        int code = runMain("-P", "1234", "-d", DEST_DIR, "-z", "src/test/resources/dicom/99999999/99999999_RTIMAGE_0001.DCM", "src/test/resources/dicom/99999999/99999999_RTIMAGE_0002.DCM");
        assertTrue("command line mode with multiple RTIMAGE files", code == 0);
    }
    */

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            TestCommandLine tcl = new TestCommandLine();
            tcl.commandLineSingleCT();
        }
        catch (Exception e) {
            System.out.println("Badness: " + e);
            e.printStackTrace();
        }
    }

}
